# Public HTTP API: one Lambda behind an API Gateway HTTP API with stage-wide
# throttling. Nothing is created until api_lambda_zip_path is set. See
# docs/PRODUCTION_DEPLOYMENT_PLAN.md and docs/PRODUCTION_HARDENING_PROGRESS.md.
#
# Cost notes (us-east-1, checked 2026-10-01):
# - API Gateway HTTP APIs cost $1.00 per million requests. This is the only
#   charge here that is not inside an always-free allowance: about $0.03 a
#   month at 30,000 requests, about $1.50 at 1.5 million (5,000 daily users).
#   Unknown paths are answered by the gateway without invoking the function.
# - Lambda: 1M requests and 400,000 GB-seconds a month are always free. At
#   1,024 MB that is about 4 million 100 ms requests.
# - CloudWatch: the log group keeps 14 days; the first 5 GB of logs a month and
#   10 alarms are free. With the notification alarm this makes two.
# - No access logs (they would add log volume and could record device tokens
#   in DELETE paths); the function logs one api_request line with the route
#   pattern instead. No custom domain, WAF, VPC, NAT or provisioned
#   concurrency, all of which cost money.
# - The database password is read from the same free Standard-tier SSM
#   parameter as the notification function, once per cold start.

locals {
  api_function_name = "on-this-day-api"
  deploy_api        = var.api_lambda_zip_path != null

  # Mirrors ApiRoutes and template.yaml. A route missing here returns 404 from
  # the gateway, so add new endpoints in all three places.
  api_routes = toset([
    "GET /v1/health",
    "GET /v1/days/today",
    "GET /v1/days/recent",
    "GET /v1/events/{eventId}",
    "POST /v1/devices",
    "DELETE /v1/devices/{token}",
    "GET /v1/quizzes/catalog",
    "POST /v1/quizzes/quick-play",
    "GET /v1/quizzes/daily",
  ])
}

resource "aws_cloudwatch_log_group" "api" {
  count = local.deploy_api ? 1 : 0

  name              = "/aws/lambda/${local.api_function_name}"
  retention_in_days = 14
  tags              = { Component = "api" }
}

resource "aws_iam_role" "api" {
  count = local.deploy_api ? 1 : 0

  name                 = local.api_function_name
  description          = "Public HTTP API function"
  assume_role_policy   = data.aws_iam_policy_document.notifications_assume.json
  permissions_boundary = local.workload_boundary_arn
  tags                 = { Component = "api" }
}

# Least privilege: its own logs and the database password parameter only. As
# with the notification role, the aws/ssm key does not restrict callers by
# itself, so account-wide ssm:GetParameter rights also need review.
data "aws_iam_policy_document" "api" {
  statement {
    sid       = "OwnLogs"
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = ["arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${local.api_function_name}:*"]
  }

  statement {
    sid       = "ReadDatabasePassword"
    actions   = ["ssm:GetParameter"]
    resources = [local.db_password_parameter_arn]
  }

  statement {
    sid       = "DecryptDatabasePasswordViaSsm"
    actions   = ["kms:Decrypt"]
    resources = ["*"]

    condition {
      test     = "StringEquals"
      variable = "kms:ViaService"
      values   = ["ssm.${var.aws_region}.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "kms:EncryptionContext:PARAMETER_ARN"
      values   = [local.db_password_parameter_arn]
    }
  }
}

resource "aws_iam_role_policy" "api" {
  count = local.deploy_api ? 1 : 0

  name   = local.api_function_name
  role   = aws_iam_role.api[0].id
  policy = data.aws_iam_policy_document.api.json
}

resource "aws_lambda_function" "api" {
  count = local.deploy_api ? 1 : 0

  function_name    = local.api_function_name
  description      = "On This Day public HTTP API"
  role             = aws_iam_role.api[0].arn
  runtime          = "java21"
  architectures    = ["arm64"]
  handler          = "com.onthisday.platform.lambda.ApiGatewayHttpHandler::handleRequest"
  filename         = var.api_lambda_zip_path
  source_code_hash = filebase64sha256(var.api_lambda_zip_path)
  # More memory also means more CPU, which shortens Java cold starts; idle
  # memory costs nothing.
  memory_size = 1024
  # Below the HTTP API's 30-second integration limit.
  timeout = 15
  # No reserved concurrency: new accounts may have only 10 concurrent
  # executions, none reservable. Stage throttling bounds the load instead.

  environment {
    variables = {
      DB_JDBC_URL                = var.db_jdbc_url
      DB_USER                    = var.db_user
      DB_PASSWORD_SSM_PARAMETER  = var.db_password_ssm_parameter_name
      DB_SSL_MODE                = "verify-full"
      DB_CONNECT_TIMEOUT_SECONDS = "5"
      DB_SOCKET_TIMEOUT_SECONDS  = "10"
      # Faster cold starts for a short-lived function; free.
      JAVA_TOOL_OPTIONS = "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
    }
  }

  tags       = { Component = "api" }
  depends_on = [aws_cloudwatch_log_group.api, aws_iam_role_policy.api]

  lifecycle {
    precondition {
      condition     = var.db_jdbc_url != null && var.db_user != null
      error_message = "Set db_jdbc_url and db_user before deploying the API function."
    }
  }
}

resource "aws_apigatewayv2_api" "api" {
  count = local.deploy_api ? 1 : 0

  name          = local.api_function_name
  description   = "On This Day public API"
  protocol_type = "HTTP"
  # No CORS: the only client is the mobile app.
  tags = { Component = "api" }
}

resource "aws_apigatewayv2_integration" "api" {
  count = local.deploy_api ? 1 : 0

  api_id                 = aws_apigatewayv2_api.api[0].id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.api[0].invoke_arn
  payload_format_version = "2.0"
  timeout_milliseconds   = 20000
}

resource "aws_apigatewayv2_route" "api" {
  for_each = local.deploy_api ? local.api_routes : toset([])

  api_id    = aws_apigatewayv2_api.api[0].id
  route_key = each.value
  target    = "integrations/${aws_apigatewayv2_integration.api[0].id}"
}

resource "aws_apigatewayv2_stage" "api" {
  count = local.deploy_api ? 1 : 0

  api_id      = aws_apigatewayv2_api.api[0].id
  name        = "$default"
  auto_deploy = true

  # Requests above this get 429 from the gateway before reaching Lambda or the
  # database; it is the API's abuse limit, since it has no user login.
  default_route_settings {
    throttling_rate_limit  = var.api_throttle_rate_limit
    throttling_burst_limit = var.api_throttle_burst_limit
  }

  tags = { Component = "api" }
}

resource "aws_lambda_permission" "api_gateway" {
  count = local.deploy_api ? 1 : 0

  statement_id  = "AllowHttpApiInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.api[0].function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.api[0].execution_arn}/*/*"
}

resource "aws_cloudwatch_metric_alarm" "api_errors" {
  count = local.deploy_api ? 1 : 0

  alarm_name          = "${local.api_function_name}-errors"
  alarm_description   = "The API function failed requests (database, configuration, or code error). Check its logs."
  namespace           = "AWS/Lambda"
  metric_name         = "Errors"
  dimensions          = { FunctionName = aws_lambda_function.api[0].function_name }
  statistic           = "Sum"
  period              = 3600
  evaluation_periods  = 1
  threshold           = 5
  comparison_operator = "GreaterThanOrEqualToThreshold"
  treat_missing_data  = "notBreaching"
  alarm_actions       = aws_sns_topic.alerts[*].arn
  ok_actions          = aws_sns_topic.alerts[*].arn
  tags                = { Component = "api" }
}

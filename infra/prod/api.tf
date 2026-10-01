# Public HTTP API: one Lambda behind a free function URL, reachable only
# through its own CloudFront distribution on the Free flat-rate plan. Nothing
# is created until api_lambda_zip_path is set. See
# docs/PRODUCTION_DEPLOYMENT_PLAN.md and docs/API_SECURITY.md.
#
# Security model (the app has no user login, so every route is public):
# - The function URL uses AWS_IAM auth and only this distribution may invoke
#   it, via origin access control (SigV4). Direct calls get 403 from Lambda
#   without running, or billing, the function.
# - Abuse limits live at the edge: the Free plan's AWS WAF web ACL takes a
#   per-IP rate-based rule (added by the owner in the console), and content
#   GETs are cached for 60 seconds, so repeated reads rarely reach Lambda or
#   the database. Handlers validate every input and cap list sizes.
# - api_reserved_concurrency caps simultaneous executions once the account's
#   Lambda quota allows reserving (new accounts start at 10 and cannot).
#
# Cost notes (us-east-1, checked 2026-10-01):
# - Function URLs are free; there is no API Gateway charge.
# - CloudFront Free flat-rate plan: $0/month, 1M requests and 100 GB a month,
#   and no overage charges even under attack; blocked requests do not count.
#   At most three Free plans per account; the image distribution uses one.
#   Until the owner subscribes the distribution it is pay-as-you-go, which is
#   inside CloudFront's always-free 10M requests and 1 TB for beta traffic.
# - Lambda: 1M requests and 400,000 GB-seconds a month are always free. At
#   1,024 MB that is about 4 million 100 ms requests. Cached responses and
#   requests WAF blocks never invoke it.
# - CloudWatch: the log group keeps 14 days; the first 5 GB of logs a month and
#   10 alarms are free. With the notification alarm this makes two.
# - No CloudFront standard logs (they could record device tokens in DELETE
#   paths); the function logs one api_request line with the route pattern
#   instead. No custom domain, VPC, NAT or provisioned concurrency, all of
#   which cost money.
# - The database password is read from the same free Standard-tier SSM
#   parameter as the notification function, once per cold start.

locals {
  api_function_name = "on-this-day-api"
  deploy_api        = var.api_lambda_zip_path != null

  # Every query parameter a handler reads (TodayContentHandler,
  # RecentDaysHandler, DailyQuizHandler). Others are dropped at the edge, so
  # add new ones here as well as in the handler.
  api_query_parameters = ["timezone", "days", "questionCount"]
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
  # Below CloudFront's 30-second origin read timeout.
  timeout = 15
  # -1 leaves the function unreserved: new accounts may have only 10
  # concurrent executions, none reservable. Set api_reserved_concurrency once
  # the quota is raised to put a hard ceiling on parallel work.
  reserved_concurrent_executions = coalesce(var.api_reserved_concurrency, -1)

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

# The function URL accepts only SigV4-signed requests (AWS_IAM), and the only
# principal allowed to sign them is the API distribution below. Requests sent
# straight to the URL are refused by Lambda before the function runs, so they
# are not billed.
resource "aws_lambda_function_url" "api" {
  count = local.deploy_api ? 1 : 0

  function_name      = aws_lambda_function.api[0].function_name
  authorization_type = "AWS_IAM"
  invoke_mode        = "BUFFERED"
}

# Both statements are needed: Lambda checks InvokeFunctionUrl and
# InvokeFunction for function URL calls.
resource "aws_lambda_permission" "api_cloudfront_url" {
  count = local.deploy_api ? 1 : 0

  statement_id           = "AllowApiDistributionInvokeFunctionUrl"
  action                 = "lambda:InvokeFunctionUrl"
  function_name          = aws_lambda_function.api[0].function_name
  principal              = "cloudfront.amazonaws.com"
  source_arn             = aws_cloudfront_distribution.api[0].arn
  function_url_auth_type = "AWS_IAM"
}

resource "aws_lambda_permission" "api_cloudfront_invoke" {
  count = local.deploy_api ? 1 : 0

  statement_id  = "AllowApiDistributionInvokeFunction"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.api[0].function_name
  principal     = "cloudfront.amazonaws.com"
  source_arn    = aws_cloudfront_distribution.api[0].arn
}

resource "aws_cloudfront_origin_access_control" "api" {
  count = local.deploy_api ? 1 : 0

  name                              = local.api_function_name
  description                       = "CloudFront signs every request to the API function URL"
  origin_access_control_origin_type = "lambda"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# The origin decides what is cacheable: the function sends Cache-Control only
# on successful content GETs, and everything else (device writes, Quick Play,
# health, errors) has no max-age and is not cached. Only the query parameters
# the API reads are in the cache key, so junk parameters cannot bust the cache.
resource "aws_cloudfront_cache_policy" "api" {
  count = local.deploy_api ? 1 : 0

  name        = "${local.api_function_name}-origin-controlled"
  comment     = "Honour the API's Cache-Control; key on the API's query parameters only"
  min_ttl     = 0
  default_ttl = 0
  max_ttl     = 300

  parameters_in_cache_key_and_forwarded_to_origin {
    enable_accept_encoding_gzip   = true
    enable_accept_encoding_brotli = true

    cookies_config {
      cookie_behavior = "none"
    }

    headers_config {
      header_behavior = "none"
    }

    query_strings_config {
      query_string_behavior = "whitelist"

      query_strings {
        items = local.api_query_parameters
      }
    }
  }
}

# Forwards the viewer's headers (Content-Type and the body hash OAC needs on
# POST and DELETE) but not Host, which must be the function URL's own host
# for the signature to verify.
data "aws_cloudfront_origin_request_policy" "all_viewer_except_host" {
  name = "Managed-AllViewerExceptHostHeader"
}

resource "aws_cloudfront_distribution" "api" {
  count = local.deploy_api ? 1 : 0

  enabled         = true
  is_ipv6_enabled = true
  http_version    = "http2and3"
  comment         = "On This Day public API (function URL origin)"
  price_class     = "PriceClass_All"
  tags            = { Component = "api" }

  origin {
    # https://<id>.lambda-url.us-east-1.on.aws/ -> <id>.lambda-url.us-east-1.on.aws
    domain_name              = trimsuffix(trimprefix(aws_lambda_function_url.api[0].function_url, "https://"), "/")
    origin_id                = "api-function-url"
    origin_access_control_id = aws_cloudfront_origin_access_control.api[0].id

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "https-only"
      origin_ssl_protocols   = ["TLSv1.2"]
      # Above the function timeout so CloudFront never gives up first.
      origin_read_timeout = 30
    }
  }

  default_cache_behavior {
    target_origin_id         = "api-function-url"
    allowed_methods          = ["GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"]
    cached_methods           = ["GET", "HEAD"]
    cache_policy_id          = aws_cloudfront_cache_policy.api[0].id
    origin_request_policy_id = data.aws_cloudfront_origin_request_policy.all_viewer_except_host.id
    viewer_protocol_policy   = "https-only"
    compress                 = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
    minimum_protocol_version       = "TLSv1"
  }

  # The owner subscribes this distribution to the CloudFront Free flat-rate
  # plan in the console, which attaches the plan's AWS WAF web ACL (add the
  # per-IP rate-based rule there). Later applies must keep that association.
  lifecycle {
    ignore_changes = [web_acl_id]
  }
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

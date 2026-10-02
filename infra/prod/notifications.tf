# Scheduled daily notification: one SSM SecureString for the Firebase key, and
# (only once a build artifact is supplied) a separate Lambda, its EventBridge
# Scheduler schedule, logs and an error alarm. See docs/NOTIFICATIONS.md.
#
# Cost notes (us-east-1, checked 2026-10-01):
# - Standard-tier SSM parameters have no storage or API charge. The value must
#   stay under 4 KB; the Firebase service-account JSON is about 2.3 KB.
# - The parameter uses the AWS managed aws/ssm key: no monthly key fee, and
#   decrypts count toward the 20,000 free KMS requests a month. The function
#   reads it once per cold start, at most ~2,900 times a month.
# - A customer-managed KMS key would restrict decryption further but costs
#   about $1 a month, so it is a later option, not the default.

locals {
  notifications_prefix        = "on-this-day-notifications"
  firebase_parameter_name     = "/on-this-day/prod/firebase-service-account"
  notifications_function_name = "on-this-day-notifications"
  deploy_notifications        = var.notifications_lambda_zip_path != null
  firebase_parameter_arn      = "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter${local.firebase_parameter_name}"
  db_password_parameter_arn   = "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter${var.db_password_ssm_parameter_name}"
  workload_boundary_arn       = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:policy/on-this-day-workload-boundary"
}

data "aws_caller_identity" "current" {}

# The key is written through a write-only argument, so it never enters
# Terraform state or plan files. It is sent to AWS only when
# firebase_credentials_version changes; later plans without the key pass a
# placeholder that is never written.
resource "aws_ssm_parameter" "firebase_service_account" {
  count = var.firebase_credentials_version > 0 ? 1 : 0

  name        = local.firebase_parameter_name
  description = "Firebase service-account JSON for the scheduled notification function"
  type        = "SecureString"
  tier        = "Standard"
  # Omitting key_id selects the AWS managed aws/ssm key (no monthly fee).

  value_wo         = var.firebase_service_account_json != null ? var.firebase_service_account_json : "not-provided-in-this-run"
  value_wo_version = var.firebase_credentials_version

  tags = { Component = "notifications" }
}

resource "aws_cloudwatch_log_group" "notifications" {
  count = local.deploy_notifications ? 1 : 0

  name              = "/aws/lambda/${local.notifications_function_name}"
  retention_in_days = 14
  tags              = { Component = "notifications" }
}

data "aws_iam_policy_document" "notifications_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "notifications" {
  count = local.deploy_notifications ? 1 : 0

  name                 = local.notifications_prefix
  description          = "Scheduled daily notification function"
  assume_role_policy   = data.aws_iam_policy_document.notifications_assume.json
  permissions_boundary = local.workload_boundary_arn
  tags                 = { Component = "notifications" }
}

# Least privilege: write its own logs, read exactly two parameters, and
# decrypt only through SSM for those parameters. Anyone else in the account
# with broad ssm:GetParameter rights could also read them, because the aws/ssm
# key does not restrict callers by itself; review account IAM accordingly.
data "aws_iam_policy_document" "notifications" {
  statement {
    sid       = "OwnLogs"
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = ["arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${local.notifications_function_name}:*"]
  }

  statement {
    sid       = "ReadOwnParameters"
    actions   = ["ssm:GetParameter"]
    resources = [local.firebase_parameter_arn, local.db_password_parameter_arn]
  }

  statement {
    sid       = "DecryptOwnParametersViaSsm"
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
      values   = [local.firebase_parameter_arn, local.db_password_parameter_arn]
    }
  }
}

resource "aws_iam_role_policy" "notifications" {
  count = local.deploy_notifications ? 1 : 0

  name   = local.notifications_prefix
  role   = aws_iam_role.notifications[0].id
  policy = data.aws_iam_policy_document.notifications.json
}

resource "aws_lambda_function" "notifications" {
  count = local.deploy_notifications ? 1 : 0

  function_name    = local.notifications_function_name
  description      = "Sends the daily featured-event notification at 10:00 local time"
  role             = aws_iam_role.notifications[0].arn
  runtime          = "java21"
  architectures    = ["arm64"]
  handler          = "com.onthisday.platform.notifications.scheduled.ScheduledNotificationHandler::handleRequest"
  filename         = var.notifications_lambda_zip_path
  source_code_hash = filebase64sha256(var.notifications_lambda_zip_path)
  memory_size      = 512
  # The claim lease in NotificationSchedule is 15 minutes and must stay longer
  # than this timeout, so an expired claim always belongs to a stopped run.
  timeout = 300
  # No reserved concurrency: new accounts may have only 10 concurrent
  # executions in total, none of which can be reserved. Overlapping runs are
  # made safe by the per-device claim instead.

  environment {
    variables = {
      DB_JDBC_URL                        = local.db_jdbc_url
      DB_USER                            = local.db_user
      DB_PASSWORD_SSM_PARAMETER          = var.db_password_ssm_parameter_name
      DB_CONNECT_TIMEOUT_SECONDS         = "5"
      DB_SOCKET_TIMEOUT_SECONDS          = "10"
      FIREBASE_PROJECT_ID                = var.firebase_project_id
      FIREBASE_CREDENTIALS_SSM_PARAMETER = local.firebase_parameter_name
      NOTIFICATIONS_DRY_RUN              = var.notifications_dry_run ? "true" : "false"
    }
  }

  tags       = { Component = "notifications" }
  depends_on = [aws_cloudwatch_log_group.notifications, aws_iam_role_policy.notifications]

  lifecycle {
    precondition {
      condition     = local.db_jdbc_url != null && local.db_user != null && var.firebase_project_id != null
      error_message = "Set supabase_project_ref (or db_jdbc_url and db_user) and firebase_project_id before deploying the notification function."
    }
  }
}

# A failed run is not retried by Lambda; the next scheduled run picks up
# anything left, so a retry would only add a second overlapping run.
resource "aws_lambda_function_event_invoke_config" "notifications" {
  count = local.deploy_notifications ? 1 : 0

  function_name          = aws_lambda_function.notifications[0].function_name
  maximum_retry_attempts = 0
}

data "aws_iam_policy_document" "scheduler_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["scheduler.amazonaws.com"]
    }
    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [data.aws_caller_identity.current.account_id]
    }
  }
}

resource "aws_iam_role" "notifications_scheduler" {
  count = local.deploy_notifications ? 1 : 0

  name                 = "${local.notifications_prefix}-scheduler"
  description          = "Lets EventBridge Scheduler invoke the notification function"
  assume_role_policy   = data.aws_iam_policy_document.scheduler_assume.json
  permissions_boundary = local.workload_boundary_arn
  tags                 = { Component = "notifications" }
}

resource "aws_iam_role_policy" "notifications_scheduler" {
  count = local.deploy_notifications ? 1 : 0

  name = "${local.notifications_prefix}-scheduler"
  role = aws_iam_role.notifications_scheduler[0].id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = "lambda:InvokeFunction"
      Resource = aws_lambda_function.notifications[0].arn
    }]
  })
}

# Every 15 minutes in UTC. Each run sends to devices whose local time is
# between 10:00 and 12:00 and that have not received that local date's
# notification, so delivery is scheduled for 10:00 local and normally lands
# within the first run after it. Created DISABLED until launch checks pass.
resource "aws_scheduler_schedule" "notifications" {
  count = local.deploy_notifications ? 1 : 0

  name                         = "${local.notifications_prefix}-every-15-minutes"
  description                  = "Daily notification, scheduled for 10:00 device-local time"
  schedule_expression          = "cron(0/15 * * * ? *)"
  schedule_expression_timezone = "UTC"
  state                        = var.notifications_schedule_enabled ? "ENABLED" : "DISABLED"

  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = aws_lambda_function.notifications[0].arn
    role_arn = aws_iam_role.notifications_scheduler[0].arn
    input    = jsonencode({})

    retry_policy {
      maximum_retry_attempts = 0
    }
  }
}

resource "aws_cloudwatch_metric_alarm" "notifications_errors" {
  count = local.deploy_notifications ? 1 : 0

  alarm_name          = "${local.notifications_prefix}-errors"
  alarm_description   = "A scheduled notification run failed (database, Firebase configuration, or code error). Check the run summary in the function's logs."
  namespace           = "AWS/Lambda"
  metric_name         = "Errors"
  dimensions          = { FunctionName = aws_lambda_function.notifications[0].function_name }
  statistic           = "Sum"
  period              = 3600
  evaluation_periods  = 1
  threshold           = 1
  comparison_operator = "GreaterThanOrEqualToThreshold"
  treat_missing_data  = "notBreaching"
  alarm_actions       = aws_sns_topic.alerts[*].arn
  ok_actions          = aws_sns_topic.alerts[*].arn
  tags                = { Component = "notifications" }
}

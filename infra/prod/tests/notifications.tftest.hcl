# Offline plan checks with a mocked AWS provider: no credentials, no state.
# Run: terraform -chdir=infra/prod init -backend=false && terraform -chdir=infra/prod test

mock_provider "aws" {
  mock_data "aws_caller_identity" { defaults = { account_id = "764574955085" } }
  mock_data "aws_iam_policy_document" { defaults = { json = "{}" } }
  mock_data "aws_cloudfront_cache_policy" { defaults = { id = "x" } }
}
variables { media_bucket_name = "on-this-day-media-764574955085" }

run "default_creates_no_notification_resources" {
  command = plan
  assert {
    condition     = length(aws_ssm_parameter.firebase_service_account) == 0 && length(aws_lambda_function.notifications) == 0 && length(aws_budgets_budget.monthly) == 0
    error_message = "defaults must create nothing new"
  }
}

run "parameter_only_with_key" {
  command = plan
  variables {
    firebase_credentials_version  = 1
    firebase_service_account_json = "{\"type\":\"service_account\"}"
  }
  assert {
    condition     = aws_ssm_parameter.firebase_service_account[0].tier == "Standard" && aws_ssm_parameter.firebase_service_account[0].type == "SecureString" && length(aws_lambda_function.notifications) == 0
    error_message = "parameter only"
  }
}

run "later_plan_without_key" {
  command = plan
  variables { firebase_credentials_version = 1 }
  assert {
    condition     = length(aws_ssm_parameter.firebase_service_account) == 1
    error_message = "keeps parameter"
  }
}

run "too_big_key_rejected" {
  command = plan
  variables {
    firebase_credentials_version  = 1
    firebase_service_account_json = join("", [for i in range(1000) : "xxxxx"])
  }
  expect_failures = [var.firebase_service_account_json]
}

run "full_deploy_disabled_schedule" {
  command = plan
  variables {
    notifications_lambda_zip_path = "tests/fake-lambda.zip"
    db_jdbc_url                   = "jdbc:postgresql://x/y"
    db_user                       = "runtime"
    firebase_project_id           = "proj"
    alert_email                   = "owner@example.com"
  }
  assert {
    condition     = aws_scheduler_schedule.notifications[0].state == "DISABLED" && aws_lambda_function.notifications[0].environment[0].variables["NOTIFICATIONS_DRY_RUN"] == "true" && aws_scheduler_schedule.notifications[0].schedule_expression == "cron(0/15 * * * ? *)"
    error_message = "schedule disabled and dry-run by default"
  }
}

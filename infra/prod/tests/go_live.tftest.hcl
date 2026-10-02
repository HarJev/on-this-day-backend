# Offline plan check for the go-live variable set in terraform.tfvars.example
# (docs/GO_LIVE.md): API, notifications and alerts in one apply, with the
# existing Firebase parameter kept, the push schedule still off, and the
# existing Supabase project imported with SSL enforced.
# Run: terraform -chdir=infra/prod init -backend=false && terraform -chdir=infra/prod test

mock_provider "aws" {
  mock_data "aws_caller_identity" { defaults = { account_id = "764574955085" } }
  mock_data "aws_iam_policy_document" { defaults = { json = "{}" } }
  mock_data "aws_cloudfront_cache_policy" { defaults = { id = "x" } }
  mock_data "aws_cloudfront_origin_request_policy" { defaults = { id = "all-viewer-except-host" } }
  mock_resource "aws_lambda_function_url" {
    override_during = plan
    defaults        = { function_url = "https://abc123.lambda-url.us-east-1.on.aws/" }
  }
  mock_resource "aws_cloudfront_distribution" {
    override_during = plan
    defaults = {
      arn         = "arn:aws:cloudfront::764574955085:distribution/EAPI"
      domain_name = "dapi.cloudfront.net"
    }
  }
  mock_resource "aws_sns_topic" {
    override_during = plan
    defaults        = { arn = "arn:aws:sns:us-east-1:764574955085:on-this-day-alerts" }
  }
}

mock_provider "supabase" {
  mock_data "supabase_pooler" {
    defaults = { url = { transaction = "postgresql://postgres.abcdefghijklmnop:[YOUR-PASSWORD]@aws-0-us-east-1.pooler.supabase.com:6543/postgres" } }
  }
}

variables {
  media_bucket_name             = "on-this-day-media-764574955085"
  firebase_credentials_version  = 1
  api_lambda_zip_path           = "tests/fake-lambda.zip"
  notifications_lambda_zip_path = "tests/fake-lambda.zip"
  supabase_project_ref          = "abcdefghijklmnop"
  supabase_organization_id      = "exampleorg"
  firebase_project_id           = "on-this-day-98e6b"
  alert_email                   = "owner@example.com"
}

run "go_live_set" {
  command = plan

  # The project is imported, which mock providers cannot do.
  override_resource {
    target = supabase_project.prod
    values = { id = "abcdefghijklmnop", name = "on-this-day", region = "us-east-1", organization_id = "exampleorg" }
  }

  assert {
    condition     = length(aws_ssm_parameter.firebase_service_account) == 1
    error_message = "firebase_credentials_version = 1 keeps the existing Firebase key parameter"
  }

  assert {
    condition     = length(aws_lambda_function.api) == 1 && length(aws_cloudfront_distribution.api) == 1 && length(aws_lambda_function.notifications) == 1
    error_message = "one apply deploys the API, its distribution and the notification function"
  }

  assert {
    condition     = aws_scheduler_schedule.notifications[0].state == "DISABLED" && aws_lambda_function.notifications[0].environment[0].variables["NOTIFICATIONS_DRY_RUN"] == "true"
    error_message = "the push schedule stays disabled and dry-run until the owner flips it"
  }

  assert {
    condition = alltrue([
      for f in [aws_lambda_function.api[0], aws_lambda_function.notifications[0]] :
      f.environment[0].variables["DB_USER"] == "otd_runtime.abcdefghijklmnop" && f.environment[0].variables["DB_PASSWORD_SSM_PARAMETER"] == "/on-this-day/prod/db-password"
    ])
    error_message = "both functions use the runtime role and read its password from SSM"
  }

  assert {
    condition     = length(aws_budgets_budget.monthly) == 1 && aws_cloudwatch_metric_alarm.api_errors[0].alarm_actions == toset(["arn:aws:sns:us-east-1:764574955085:on-this-day-alerts"]) && aws_cloudwatch_metric_alarm.notifications_errors[0].alarm_actions == toset(["arn:aws:sns:us-east-1:764574955085:on-this-day-alerts"])
    error_message = "the budget exists and both error alarms email the owner"
  }

  assert {
    condition = alltrue([
      for f in [aws_lambda_function.api[0], aws_lambda_function.notifications[0]] :
      f.environment[0].variables["DB_JDBC_URL"] == "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres?prepareThreshold=0"
    ])
    error_message = "both functions use the Supabase transaction pooler, derived from the project"
  }

  assert {
    condition     = supabase_settings.prod[0].ssl_enforcement == true && supabase_project.prod[0].name == "on-this-day" && supabase_project.prod[0].region == "us-east-1"
    error_message = "the existing project is managed with SSL enforced"
  }

  assert {
    condition     = output.api_base_url == "https://dapi.cloudfront.net"
    error_message = "the app's base URL is the CloudFront address"
  }
}

# Offline plan checks for api.tf with a mocked AWS provider: no credentials, no state.
# Run: terraform -chdir=infra/prod init -backend=false && terraform -chdir=infra/prod test

mock_provider "aws" {
  mock_data "aws_caller_identity" { defaults = { account_id = "764574955085" } }
  mock_data "aws_iam_policy_document" { defaults = { json = "{}" } }
  mock_data "aws_cloudfront_cache_policy" { defaults = { id = "x" } }
}
variables { media_bucket_name = "on-this-day-media-764574955085" }

run "default_creates_no_api" {
  command = plan
  assert {
    condition     = length(aws_lambda_function.api) == 0 && length(aws_apigatewayv2_api.api) == 0 && length(aws_apigatewayv2_route.api) == 0
    error_message = "the API must not exist until a build is supplied"
  }
}

run "api_needs_database_settings" {
  command = plan
  variables { api_lambda_zip_path = "tests/fake-lambda.zip" }
  expect_failures = [aws_lambda_function.api]
}

run "full_api_deploy" {
  command = plan
  variables {
    api_lambda_zip_path = "tests/fake-lambda.zip"
    db_jdbc_url         = "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres"
    db_user             = "runtime"
  }

  assert {
    condition     = aws_apigatewayv2_stage.api[0].default_route_settings[0].throttling_rate_limit == 10 && aws_apigatewayv2_stage.api[0].default_route_settings[0].throttling_burst_limit == 20
    error_message = "throttled to 10 requests a second, burst 20, by default"
  }

  assert {
    condition     = length(aws_apigatewayv2_route.api) == 9 && contains(keys(aws_apigatewayv2_route.api), "DELETE /v1/devices/{token}")
    error_message = "every app route, and only those"
  }

  assert {
    condition     = aws_lambda_function.api[0].environment[0].variables["DB_PASSWORD_SSM_PARAMETER"] == "/on-this-day/prod/db-password" && aws_lambda_function.api[0].environment[0].variables["DB_SSL_MODE"] == "verify-full" && !contains(keys(aws_lambda_function.api[0].environment[0].variables), "DB_PASSWORD")
    error_message = "password from SSM only, with verified TLS"
  }

  assert {
    condition     = aws_apigatewayv2_integration.api[0].payload_format_version == "2.0" && aws_lambda_function.api[0].timeout < 30 && aws_cloudwatch_log_group.api[0].retention_in_days == 14
    error_message = "payload v2, under the gateway timeout, 14-day logs"
  }

  assert {
    condition     = length(aws_lambda_function.notifications) == 0
    error_message = "deploying the API must not deploy the notification function"
  }
}

run "throttle_cannot_be_opened_wide" {
  command = plan
  variables { api_throttle_rate_limit = 1000 }
  expect_failures = [var.api_throttle_rate_limit]
}

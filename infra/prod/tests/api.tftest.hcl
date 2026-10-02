# Offline plan checks for api.tf with a mocked AWS provider: no credentials, no state.
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
}
variables { media_bucket_name = "on-this-day-media-764574955085" }

# The boundary policy is imported, which mock providers cannot do.
override_resource {
  target = aws_iam_policy.workload_boundary
  values = { arn = "arn:aws:iam::764574955085:policy/on-this-day-workload-boundary" }
}

run "default_creates_no_api" {
  command = plan
  assert {
    condition     = length(aws_lambda_function.api) == 0 && length(aws_lambda_function_url.api) == 0 && length(aws_cloudfront_distribution.api) == 0
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
    condition     = aws_lambda_function_url.api[0].authorization_type == "AWS_IAM"
    error_message = "the function URL must refuse unsigned callers"
  }

  assert {
    condition = alltrue([
      for p in [aws_lambda_permission.api_cloudfront_url[0], aws_lambda_permission.api_cloudfront_invoke[0]] :
      p.principal == "cloudfront.amazonaws.com" && p.source_arn == aws_cloudfront_distribution.api[0].arn
    ]) && aws_lambda_permission.api_cloudfront_url[0].action == "lambda:InvokeFunctionUrl" && aws_lambda_permission.api_cloudfront_invoke[0].action == "lambda:InvokeFunction"
    error_message = "only the API distribution may invoke the function, through both required permissions"
  }

  assert {
    condition     = aws_cloudfront_origin_access_control.api[0].origin_access_control_origin_type == "lambda" && aws_cloudfront_origin_access_control.api[0].signing_behavior == "always"
    error_message = "CloudFront signs every origin request"
  }

  assert {
    condition     = one(aws_cloudfront_distribution.api[0].origin).domain_name == "abc123.lambda-url.us-east-1.on.aws" && one(one(aws_cloudfront_distribution.api[0].origin).custom_origin_config).origin_protocol_policy == "https-only"
    error_message = "origin is the bare function URL host over HTTPS"
  }

  assert {
    condition     = one(aws_cloudfront_distribution.api[0].default_cache_behavior).viewer_protocol_policy == "https-only" && contains(one(aws_cloudfront_distribution.api[0].default_cache_behavior).allowed_methods, "DELETE") && data.aws_cloudfront_origin_request_policy.all_viewer_except_host.name == "Managed-AllViewerExceptHostHeader"
    error_message = "HTTPS only, every app method, and the Host header is not forwarded"
  }

  assert {
    condition     = data.aws_cloudfront_cache_policy.use_origin_cache_control_query_strings.id == "4cc15a8a-d715-48a4-82b8-cc0b614638fe"
    error_message = "a managed policy (Free plan allows no custom ones) that caches only what the API marks cacheable"
  }

  assert {
    condition     = output.api_base_url == "https://dapi.cloudfront.net"
    error_message = "the app's base URL is the CloudFront address, not the function URL"
  }

  assert {
    condition     = aws_lambda_function.api[0].reserved_concurrent_executions == -1
    error_message = "unreserved by default: new accounts cannot reserve concurrency"
  }

  assert {
    condition     = aws_lambda_function.api[0].environment[0].variables["DB_PASSWORD_SSM_PARAMETER"] == "/on-this-day/prod/db-password" && aws_lambda_function.api[0].environment[0].variables["DB_SSL_MODE"] == "verify-full" && !contains(keys(aws_lambda_function.api[0].environment[0].variables), "DB_PASSWORD")
    error_message = "password from SSM only, with verified TLS"
  }

  assert {
    condition     = aws_lambda_function.api[0].timeout < one(one(aws_cloudfront_distribution.api[0].origin).custom_origin_config).origin_read_timeout && aws_cloudwatch_log_group.api[0].retention_in_days == 14
    error_message = "the function times out before CloudFront does; 14-day logs"
  }

  assert {
    condition     = length(aws_lambda_function.notifications) == 0
    error_message = "deploying the API must not deploy the notification function"
  }
}

run "reserved_concurrency_when_set" {
  command = plan
  variables {
    api_lambda_zip_path      = "tests/fake-lambda.zip"
    db_jdbc_url              = "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres"
    db_user                  = "runtime"
    api_reserved_concurrency = 5
  }

  assert {
    condition     = aws_lambda_function.api[0].reserved_concurrent_executions == 5
    error_message = "the owner's concurrency cap is applied"
  }
}

run "concurrency_cap_stays_bounded" {
  command = plan
  variables { api_reserved_concurrency = 0 }
  expect_failures = [var.api_reserved_concurrency]
}

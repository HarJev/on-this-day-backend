output "media_cloudfront_domain_name" {
  description = "Owned HTTPS image origin. Do not use it in curated JSON until post-apply verification succeeds."
  value       = aws_cloudfront_distribution.media.domain_name
}

output "media_publisher_policy_arn" {
  description = "Attach only to a separately reviewed publishing principal."
  value       = aws_iam_policy.media_publisher.arn
}

output "pricing_plan_guardrail_policy_arn" {
  description = "Deny policy that must be attached to every approved automation role."
  value       = aws_iam_policy.deny_paid_pricing_plan_activation.arn
}

output "firebase_parameter_name" {
  description = "SSM SecureString the notification function reads its Firebase key from."
  value       = local.firebase_parameter_name
}

output "notifications_function_name" {
  description = "Scheduled notification function, when deployed."
  value       = one(aws_lambda_function.notifications[*].function_name)
}

output "api_base_url" {
  description = "Base URL for the app's release build (ON_THIS_DAY_API_BASE_URL), when deployed."
  value       = one([for d in aws_cloudfront_distribution.api : "https://${d.domain_name}"])
}

output "api_distribution_id" {
  description = "CloudFront distribution to subscribe to the Free plan in the console, when deployed."
  value       = one(aws_cloudfront_distribution.api[*].id)
}

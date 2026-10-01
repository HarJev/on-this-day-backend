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

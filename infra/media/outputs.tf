output "cloudfront_domain_name" {
  description = "Future HTTPS image origin. Do not use it in curated JSON until post-apply verification succeeds."
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

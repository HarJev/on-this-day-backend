output "deployer_user_name" {
  description = "Create this user's access key in the IAM console, then run: aws configure --profile on-this-day"
  value       = aws_iam_user.deployer.name
}

output "deployer_policy_arn" {
  value = aws_iam_policy.deployer.arn
}

output "workload_boundary_arn" {
  description = "Permissions boundary required on every role created by infra/prod"
  value       = aws_iam_policy.workload_boundary.arn
}

output "deployer_user_arn" {
  value = aws_iam_user.deployer.arn
}

output "console_sign_in_url" {
  value = "https://${local.account_id}.signin.aws.amazon.com/console"
}

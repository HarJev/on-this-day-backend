# Owner alerts by email: a monthly cost budget and the notification error
# alarm. Created only when alert_email is set. SNS email delivery is free up to
# 1,000 emails a month, and AWS Budgets monitoring (no budget actions) has no
# charge. The owner confirms the SNS subscription from the email it sends.

resource "aws_sns_topic" "alerts" {
  count = var.alert_email != null ? 1 : 0

  name = "on-this-day-alerts"
  tags = { Component = "alerts" }
}

resource "aws_sns_topic_subscription" "alerts_email" {
  count = var.alert_email != null ? 1 : 0

  topic_arn = aws_sns_topic.alerts[0].arn
  protocol  = "email"
  endpoint  = var.alert_email
}

resource "aws_budgets_budget" "monthly" {
  count = var.alert_email != null ? 1 : 0

  name         = "on-this-day-monthly"
  budget_type  = "COST"
  limit_amount = var.monthly_budget_usd
  limit_unit   = "USD"
  time_unit    = "MONTHLY"

  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 100
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = [var.alert_email]
  }

  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 100
    threshold_type             = "PERCENTAGE"
    notification_type          = "FORECASTED"
    subscriber_email_addresses = [var.alert_email]
  }
}

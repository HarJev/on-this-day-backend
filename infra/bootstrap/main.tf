# Day-to-day Terraform identity so the root user is not used for deployments.
# The owner adds a console password and MFA by hand, then runs
# `aws login --profile on-this-day` for short-lived CLI credentials. No access
# keys exist, and passwords never enter Terraform state.

data "aws_caller_identity" "current" {}

locals {
  account_id = data.aws_caller_identity.current.account_id
  prefix     = "on-this-day"

  boundary_arn = "arn:aws:iam::${local.account_id}:policy/${local.prefix}-workload-boundary"
  deployer_arn = "arn:aws:iam::${local.account_id}:user/${local.prefix}-terraform"
}

resource "aws_iam_user" "deployer" {
  name = "${local.prefix}-terraform"
}

resource "aws_iam_policy" "deployer" {
  name        = "${local.prefix}-terraform"
  description = "Manage On This Day infrastructure only"
  policy      = data.aws_iam_policy_document.deployer.json
}

resource "aws_iam_user_policy_attachment" "deployer" {
  user       = aws_iam_user.deployer.name
  policy_arn = aws_iam_policy.deployer.arn
}

# Every role the deployer creates must carry this boundary, so a workload role
# can never be used to gain more than these permissions.
resource "aws_iam_policy" "workload_boundary" {
  name        = "${local.prefix}-workload-boundary"
  description = "Maximum permissions for any On This Day workload role"
  policy      = data.aws_iam_policy_document.workload_boundary.json
}

data "aws_iam_policy_document" "deployer" {
  statement {
    sid    = "ReadForPlanning"
    effect = "Allow"
    actions = [
      "acm:Describe*", "acm:List*",
      "apigateway:GET",
      "budgets:View*",
      "ce:Get*",
      "cloudfront:Get*", "cloudfront:List*",
      "cloudwatch:Describe*", "cloudwatch:Get*", "cloudwatch:List*",
      "iam:Get*", "iam:List*",
      "lambda:Get*", "lambda:List*",
      "logs:Describe*", "logs:List*",
      "events:Describe*", "events:List*", "scheduler:Get*", "scheduler:List*",
      "sns:Get*", "sns:List*",
      "pricingplanmanager:Get*", "pricingplanmanager:List*",
      "s3:GetBucket*", "s3:ListAllMyBuckets", "s3:GetAccount*",
      "secretsmanager:Describe*", "secretsmanager:List*",
      "sts:GetCallerIdentity",
      "wafv2:Get*", "wafv2:List*", "wafv2:Describe*",
    ]
    resources = ["*"]
  }

  statement {
    sid       = "ProjectBuckets"
    effect    = "Allow"
    actions   = ["s3:*"]
    resources = ["arn:aws:s3:::${local.prefix}-*", "arn:aws:s3:::${local.prefix}-*/*"]
  }

  # CloudFront and API Gateway resources cannot be scoped by name. WAF stays
  # read-only (ReadForPlanning): the Free plan supplies its own web ACL, and
  # creating one here would be a paid resource.
  statement {
    sid       = "EdgeAndApi"
    effect    = "Allow"
    actions   = ["cloudfront:*", "pricingplanmanager:*", "apigateway:*"]
    resources = ["*"]
  }

  statement {
    sid     = "ProjectFunctionsLogsAlarms"
    effect  = "Allow"
    actions = ["lambda:*", "logs:*", "cloudwatch:*Alarm*", "cloudwatch:TagResource", "cloudwatch:UntagResource", "budgets:*", "events:*", "scheduler:*", "sns:*"]
    resources = [
      "arn:aws:lambda:*:${local.account_id}:function:${local.prefix}-*",
      "arn:aws:logs:*:${local.account_id}:log-group:/aws/lambda/${local.prefix}-*",
      "arn:aws:logs:*:${local.account_id}:log-group:/aws/apigateway/${local.prefix}-*",
      "arn:aws:cloudwatch:*:${local.account_id}:alarm:${local.prefix}-*",
      "arn:aws:budgets::${local.account_id}:budget/${local.prefix}-*",
      "arn:aws:events:*:${local.account_id}:rule/${local.prefix}-*",
      "arn:aws:scheduler:*:${local.account_id}:schedule/*/${local.prefix}-*",
      "arn:aws:scheduler:*:${local.account_id}:schedule-group/${local.prefix}-*",
      "arn:aws:sns:*:${local.account_id}:${local.prefix}-*",
    ]
  }

  # Secret containers and references only; values are set by hand.
  statement {
    sid       = "ProjectSecretMetadata"
    effect    = "Allow"
    actions   = ["secretsmanager:CreateSecret", "secretsmanager:DeleteSecret", "secretsmanager:UpdateSecret", "secretsmanager:TagResource", "secretsmanager:UntagResource", "secretsmanager:*ResourcePolicy"]
    resources = ["arn:aws:secretsmanager:*:${local.account_id}:secret:${local.prefix}/*"]
  }

  # SSM parameters under /on-this-day/ only (added by hand 2026-10 for the
  # notification key; see docs/NOTIFICATIONS.md). infra/prod pins
  # tier = "Standard"; the Advanced tier is billed per parameter.
  statement {
    sid       = "ProjectParameters"
    effect    = "Allow"
    actions   = ["ssm:PutParameter", "ssm:DeleteParameter", "ssm:GetParameter", "ssm:GetParameters", "ssm:AddTagsToResource", "ssm:RemoveTagsFromResource", "ssm:ListTagsForResource"]
    resources = ["arn:aws:ssm:*:${local.account_id}:parameter/${local.prefix}/*"]
  }

  statement {
    sid       = "DescribeParameters"
    effect    = "Allow"
    actions   = ["ssm:DescribeParameters"]
    resources = ["*"]
  }

  statement {
    sid       = "ProjectPolicies"
    effect    = "Allow"
    actions   = ["iam:CreatePolicy", "iam:CreatePolicyVersion", "iam:DeletePolicy", "iam:DeletePolicyVersion", "iam:TagPolicy", "iam:UntagPolicy"]
    resources = ["arn:aws:iam::${local.account_id}:policy/${local.prefix}-*"]
  }

  statement {
    sid       = "ProjectRolesWithBoundary"
    effect    = "Allow"
    actions   = ["iam:CreateRole", "iam:PutRolePermissionsBoundary", "iam:AttachRolePolicy", "iam:DetachRolePolicy", "iam:PutRolePolicy", "iam:DeleteRolePolicy"]
    resources = ["arn:aws:iam::${local.account_id}:role/${local.prefix}-*"]

    condition {
      test     = "StringEquals"
      variable = "iam:PermissionsBoundary"
      values   = [local.boundary_arn]
    }
  }

  statement {
    sid       = "ProjectRoleUpkeep"
    effect    = "Allow"
    actions   = ["iam:DeleteRole", "iam:UpdateRole", "iam:UpdateAssumeRolePolicy", "iam:TagRole", "iam:UntagRole"]
    resources = ["arn:aws:iam::${local.account_id}:role/${local.prefix}-*"]
  }

  statement {
    sid       = "PassProjectRolesToServices"
    effect    = "Allow"
    actions   = ["iam:PassRole"]
    resources = ["arn:aws:iam::${local.account_id}:role/${local.prefix}-*"]

    condition {
      test     = "StringEquals"
      variable = "iam:PassedToService"
      values   = ["lambda.amazonaws.com", "apigateway.amazonaws.com", "scheduler.amazonaws.com"]
    }
  }

  statement {
    sid       = "ServiceLinkedRoles"
    effect    = "Allow"
    actions   = ["iam:CreateServiceLinkedRole"]
    resources = ["*"]

    condition {
      test     = "StringEquals"
      variable = "iam:AWSServiceName"
      values   = ["ops.apigateway.amazonaws.com", "wafv2.amazonaws.com", "lambda.amazonaws.com"]
    }
  }

  # Required by `aws login` to exchange the console session for short-lived
  # CLI credentials (see the SignInLocalDevelopmentAccess managed policy).
  statement {
    sid       = "CliSignIn"
    effect    = "Allow"
    actions   = ["signin:AuthorizeOAuth2Access", "signin:CreateOAuth2Token"]
    resources = ["*"]
  }

  # Lets the deployer manage its own password and MFA in the console.
  statement {
    sid       = "OwnConsoleCredentials"
    effect    = "Allow"
    actions   = ["iam:ChangePassword", "iam:GetUser", "iam:*MFADevice", "iam:ListMFADevices"]
    resources = [local.deployer_arn, "arn:aws:iam::${local.account_id}:mfa/*"]
  }

  statement {
    sid    = "Guardrails"
    effect = "Deny"
    actions = [
      "pricingplanmanager:ApprovePaidSubscription",
      "secretsmanager:GetSecretValue",
      "iam:CreateUser", "iam:DeleteUser", "iam:CreateAccessKey", "iam:CreateLoginProfile",
      "iam:AttachUserPolicy", "iam:PutUserPolicy", "iam:AddUserToGroup",
      "iam:DeleteRolePermissionsBoundary",
      "organizations:*", "account:*", "aws-portal:Modify*", "billing:Put*", "billing:Update*",
    ]
    resources = ["*"]
  }

  # Regional services stay in the approved region; global services are exempt.
  statement {
    sid    = "OnlyApprovedRegion"
    effect = "Deny"
    not_actions = [
      "iam:*", "sts:*", "cloudfront:*", "wafv2:*", "pricingplanmanager:*",
      "budgets:*", "ce:*", "s3:ListAllMyBuckets", "s3:GetAccount*", "acm:*",
    ]
    resources = ["*"]

    condition {
      test     = "StringNotEquals"
      variable = "aws:RequestedRegion"
      values   = ["us-east-1"]
    }
  }

  statement {
    sid       = "ProtectOwnPermissions"
    effect    = "Deny"
    actions   = ["iam:CreatePolicyVersion", "iam:DeletePolicy", "iam:DeletePolicyVersion", "iam:SetDefaultPolicyVersion"]
    resources = [aws_iam_policy.workload_boundary.arn, "arn:aws:iam::${local.account_id}:policy/${local.prefix}-terraform"]
  }
}

data "aws_iam_policy_document" "workload_boundary" {
  statement {
    sid    = "WorkloadServices"
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents",
      "cloudwatch:PutMetricData",
      "xray:PutTraceSegments", "xray:PutTelemetryRecords",
      "secretsmanager:GetSecretValue",
      "ssm:GetParameter",
      "kms:Decrypt",
      "s3:GetObject", "s3:PutObject", "s3:ListBucket", "s3:AbortMultipartUpload",
      "lambda:InvokeFunction",
    ]
    resources = ["*"]
  }

  statement {
    sid           = "ProjectDataOnly"
    effect        = "Deny"
    actions       = ["secretsmanager:GetSecretValue"]
    not_resources = ["arn:aws:secretsmanager:*:${local.account_id}:secret:${local.prefix}/*"]
  }

  statement {
    sid           = "ProjectParametersOnly"
    effect        = "Deny"
    actions       = ["ssm:GetParameter"]
    not_resources = ["arn:aws:ssm:*:${local.account_id}:parameter/${local.prefix}/*"]
  }

  # Decrypt only on behalf of SSM (SecureString parameters), never directly.
  statement {
    sid       = "DecryptOnlyViaSsm"
    effect    = "Deny"
    actions   = ["kms:Decrypt"]
    resources = ["*"]

    condition {
      test     = "StringNotLike"
      variable = "kms:ViaService"
      values   = ["ssm.*.amazonaws.com"]
    }
  }

  statement {
    sid           = "ProjectBucketsOnly"
    effect        = "Deny"
    actions       = ["s3:*"]
    not_resources = ["arn:aws:s3:::${local.prefix}-*", "arn:aws:s3:::${local.prefix}-*/*"]
  }
}

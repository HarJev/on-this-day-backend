# The deployer identity that runs infra/prod. Apply this root only with an
# owner/admin identity; the deployer itself cannot change anything here.

data "aws_caller_identity" "current" {}

locals {
  account_id    = data.aws_caller_identity.current.account_id
  name_prefix   = "on-this-day"
  region        = "us-east-1"
  deployer_name = "${local.name_prefix}-terraform"

  deployer_policy_arn = "arn:aws:iam::${local.account_id}:policy/${local.deployer_name}-deployer"
  boundary_arn        = "arn:aws:iam::${local.account_id}:policy/${local.name_prefix}-workload-boundary"

  common_tags = {
    Application = "on-this-day"
    Component   = "bootstrap"
    ManagedBy   = "terraform"
  }
}

resource "aws_iam_user" "deployer" {
  name = local.deployer_name
  tags = local.common_tags
}

# Every role the deployer creates must carry this boundary, so a workload role
# (and code running as it) can never exceed these permissions, even if the
# deployer attaches a broad policy to it.
data "aws_iam_policy_document" "workload_boundary" {
  statement {
    sid       = "Observability"
    effect    = "Allow"
    actions   = ["logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents", "cloudwatch:PutMetricData", "xray:PutTraceSegments", "xray:PutTelemetryRecords"]
    resources = ["*"]
  }

  statement {
    sid       = "ProjectBuckets"
    effect    = "Allow"
    actions   = ["s3:GetObject", "s3:PutObject", "s3:ListBucket", "s3:AbortMultipartUpload"]
    resources = ["arn:aws:s3:::${local.name_prefix}-*", "arn:aws:s3:::${local.name_prefix}-*/*"]
  }

  statement {
    sid     = "ProjectSecretsAndParameters"
    effect  = "Allow"
    actions = ["secretsmanager:GetSecretValue", "secretsmanager:DescribeSecret", "ssm:GetParameter", "ssm:GetParameters"]
    resources = [
      "arn:aws:secretsmanager:${local.region}:${local.account_id}:secret:${local.name_prefix}/*",
      "arn:aws:ssm:${local.region}:${local.account_id}:parameter/${local.name_prefix}/*",
    ]
  }

  statement {
    sid     = "ProjectInvocationAndAlerts"
    effect  = "Allow"
    actions = ["lambda:InvokeFunction", "sns:Publish"]
    resources = [
      "arn:aws:lambda:${local.region}:${local.account_id}:function:${local.name_prefix}-*",
      "arn:aws:sns:${local.region}:${local.account_id}:${local.name_prefix}-*",
    ]
  }
}

resource "aws_iam_policy" "workload_boundary" {
  name        = "${local.name_prefix}-workload-boundary"
  description = "Permissions ceiling for every On This Day workload role"
  policy      = data.aws_iam_policy_document.workload_boundary.json
  tags        = local.common_tags
}

data "aws_iam_policy_document" "deployer" {
  statement {
    sid       = "ProjectBuckets"
    effect    = "Allow"
    actions   = ["s3:*"]
    resources = ["arn:aws:s3:::${local.name_prefix}-*", "arn:aws:s3:::${local.name_prefix}-*/*"]
  }

  statement {
    sid       = "ListBuckets"
    effect    = "Allow"
    actions   = ["s3:ListAllMyBuckets", "s3:GetBucketLocation"]
    resources = ["*"]
  }

  statement {
    sid       = "GlobalServices"
    effect    = "Allow"
    actions   = ["cloudfront:*", "budgets:*"]
    resources = ["*"]
  }

  statement {
    sid    = "RegionalServices"
    effect = "Allow"
    actions = [
      "lambda:*",
      "apigateway:*",
      "events:*",
      "scheduler:*",
      "logs:*",
      "cloudwatch:*",
      "sns:*",
    ]
    resources = ["*"]
  }

  statement {
    sid       = "ReadIam"
    effect    = "Allow"
    actions   = ["iam:Get*", "iam:List*"]
    resources = ["*"]
  }

  statement {
    sid    = "ManageProjectRolesWithinBoundary"
    effect = "Allow"
    actions = [
      "iam:CreateRole",
      "iam:PutRolePermissionsBoundary",
      "iam:AttachRolePolicy",
      "iam:DetachRolePolicy",
      "iam:PutRolePolicy",
      "iam:DeleteRolePolicy",
    ]
    resources = ["arn:aws:iam::${local.account_id}:role/${local.name_prefix}-*"]

    condition {
      test     = "StringEquals"
      variable = "iam:PermissionsBoundary"
      values   = [local.boundary_arn]
    }
  }

  statement {
    sid    = "ManageProjectRoles"
    effect = "Allow"
    actions = [
      "iam:DeleteRole",
      "iam:UpdateRole",
      "iam:UpdateRoleDescription",
      "iam:UpdateAssumeRolePolicy",
      "iam:TagRole",
      "iam:UntagRole",
    ]
    resources = ["arn:aws:iam::${local.account_id}:role/${local.name_prefix}-*"]
  }

  statement {
    sid    = "ManageProjectPolicies"
    effect = "Allow"
    actions = [
      "iam:CreatePolicy",
      "iam:DeletePolicy",
      "iam:CreatePolicyVersion",
      "iam:DeletePolicyVersion",
      "iam:SetDefaultPolicyVersion",
      "iam:TagPolicy",
      "iam:UntagPolicy",
    ]
    resources = ["arn:aws:iam::${local.account_id}:policy/${local.name_prefix}-*"]
  }

  statement {
    sid       = "PassProjectRolesToWorkloads"
    effect    = "Allow"
    actions   = ["iam:PassRole"]
    resources = ["arn:aws:iam::${local.account_id}:role/${local.name_prefix}-*"]

    condition {
      test     = "StringEquals"
      variable = "iam:PassedToService"
      values   = ["lambda.amazonaws.com", "scheduler.amazonaws.com"]
    }
  }

  statement {
    sid    = "ManageProjectSecretMetadata"
    effect = "Allow"
    actions = [
      "secretsmanager:CreateSecret",
      "secretsmanager:DescribeSecret",
      "secretsmanager:UpdateSecret",
      "secretsmanager:DeleteSecret",
      "secretsmanager:TagResource",
      "secretsmanager:UntagResource",
      "secretsmanager:GetResourcePolicy",
      "secretsmanager:PutResourcePolicy",
      "secretsmanager:DeleteResourcePolicy",
    ]
    resources = ["arn:aws:secretsmanager:${local.region}:${local.account_id}:secret:${local.name_prefix}/*"]
  }

  statement {
    sid       = "ListSecrets"
    effect    = "Allow"
    actions   = ["secretsmanager:ListSecrets"]
    resources = ["*"]
  }

  # Guardrails. Explicit denies win over every allow above.

  statement {
    sid       = "DenyPaidCloudFrontPlans"
    effect    = "Deny"
    actions   = ["pricingplanmanager:ApprovePaidSubscription"]
    resources = ["*"]
  }

  statement {
    sid       = "DenySecretValues"
    effect    = "Deny"
    actions   = ["secretsmanager:GetSecretValue", "secretsmanager:PutSecretValue", "secretsmanager:BatchGetSecretValue"]
    resources = ["*"]
  }

  statement {
    sid    = "DenyIdentityAndCredentialChanges"
    effect = "Deny"
    actions = [
      "iam:*User*",
      "iam:*AccessKey*",
      "iam:*LoginProfile*",
      "iam:*Group*",
      "iam:*MFA*",
      "iam:*SSH*",
      "iam:*ServiceSpecificCredential*",
      "iam:*SigningCertificate*",
      "iam:DeleteRolePermissionsBoundary",
      "sts:AssumeRole",
    ]
    resources = ["*"]
  }

  statement {
    sid    = "DenyEditingGuardrailPolicies"
    effect = "Deny"
    actions = [
      "iam:CreatePolicyVersion",
      "iam:DeletePolicy",
      "iam:DeletePolicyVersion",
      "iam:SetDefaultPolicyVersion",
      "iam:TagPolicy",
      "iam:UntagPolicy",
    ]
    resources = [local.deployer_policy_arn, local.boundary_arn]
  }

  statement {
    sid    = "DenyAccountAndBilling"
    effect = "Deny"
    actions = [
      "organizations:*",
      "account:*",
      "aws-portal:*",
      "billing:*",
      "payments:*",
      "tax:*",
      "invoicing:*",
      "purchase-orders:*",
    ]
    resources = ["*"]
  }

  statement {
    sid    = "DenyRegionalCallsOutsideUsEast1"
    effect = "Deny"
    not_actions = [
      "iam:*",
      "sts:*",
      "cloudfront:*",
      "budgets:*",
      "s3:ListAllMyBuckets",
      "pricingplanmanager:*",
      "support:*",
    ]
    resources = ["*"]

    condition {
      test     = "StringNotEquals"
      variable = "aws:RequestedRegion"
      values   = [local.region]
    }
  }
}

resource "aws_iam_policy" "deployer" {
  name        = "${local.deployer_name}-deployer"
  description = "Scoped permissions for the On This Day Terraform deployer"
  policy      = data.aws_iam_policy_document.deployer.json
  tags        = local.common_tags
}

resource "aws_iam_user_policy_attachment" "deployer" {
  user       = aws_iam_user.deployer.name
  policy_arn = aws_iam_policy.deployer.arn
}

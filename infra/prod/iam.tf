# The permissions boundary every workload role carries. It was created by hand
# with the bootstrap root step; the deployer's own policy no longer denies it
# edits (owner change, 2026-10-02), so it is managed here from now on. A change
# is a PR plus the owner's `terraform apply`. The deployer's own policy
# (`on-this-day-terraform`) stays outside Terraform on purpose: a deployer that
# could edit that could grant itself anything.
import {
  to = aws_iam_policy.workload_boundary
  id = local.workload_boundary_arn
}

resource "aws_iam_policy" "workload_boundary" {
  name        = "on-this-day-workload-boundary"
  description = "Maximum permissions for any On This Day workload role"
  policy      = data.aws_iam_policy_document.workload_boundary.json
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
    not_resources = ["arn:aws:secretsmanager:*:${data.aws_caller_identity.current.account_id}:secret:on-this-day/*"]
  }

  statement {
    sid           = "ProjectParametersOnly"
    effect        = "Deny"
    actions       = ["ssm:GetParameter"]
    not_resources = ["arn:aws:ssm:*:${data.aws_caller_identity.current.account_id}:parameter/on-this-day/*"]
  }

  # Decrypt only on behalf of SSM (SecureString parameters) or Lambda (a
  # function's own environment variables), never directly. Lambda's decrypt of
  # environment variables carries no kms:ViaService value, so the Null test
  # lets it through only when it carries Lambda's encryption context.
  statement {
    sid       = "DecryptOnlyViaSsm"
    effect    = "Deny"
    actions   = ["kms:Decrypt"]
    resources = ["*"]

    condition {
      test     = "StringNotLike"
      variable = "kms:ViaService"
      values   = ["ssm.*.amazonaws.com", "lambda.*.amazonaws.com"]
    }

    condition {
      test     = "Null"
      variable = "kms:EncryptionContext:aws:lambda:FunctionArn"
      values   = ["true"]
    }
  }

  statement {
    sid           = "ProjectBucketsOnly"
    effect        = "Deny"
    actions       = ["s3:*"]
    not_resources = ["arn:aws:s3:::on-this-day-*", "arn:aws:s3:::on-this-day-*/*"]
  }
}

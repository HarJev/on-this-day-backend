locals {
  common_tags = merge(
    {
      Component = "owned-image-delivery"
    },
    var.tags,
  )
}

resource "aws_s3_bucket" "media" {
  bucket        = var.media_bucket_name
  force_destroy = false
  tags          = local.common_tags
}

resource "aws_s3_bucket_public_access_block" "media" {
  bucket                  = aws_s3_bucket.media.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_ownership_controls" "media" {
  bucket = aws_s3_bucket.media.id

  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "media" {
  bucket = aws_s3_bucket.media.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_versioning" "media" {
  bucket = aws_s3_bucket.media.id

  versioning_configuration {
    status = "Enabled"
  }
}

# Objects use immutable checksum keys, so noncurrent versions only exist after
# an accidental overwrite or delete. Keep them briefly for recovery, then let
# them expire so storage stays inside the free plan's S3 credit.
resource "aws_s3_bucket_lifecycle_configuration" "media" {
  bucket = aws_s3_bucket.media.id

  rule {
    id     = "expire-noncurrent-and-abandoned-uploads"
    status = "Enabled"

    filter {}

    noncurrent_version_expiration {
      noncurrent_days = 30
    }

    abort_incomplete_multipart_upload {
      days_after_initiation = 1
    }
  }

  depends_on = [aws_s3_bucket_versioning.media]
}

resource "aws_cloudfront_origin_access_control" "media" {
  name                              = "on-this-day-owned-images"
  description                       = "CloudFront read access for private On This Day media"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

data "aws_cloudfront_cache_policy" "caching_optimized" {
  name = "Managed-CachingOptimized"
}

resource "aws_cloudfront_distribution" "media" {
  enabled             = true
  is_ipv6_enabled     = true
  http_version        = "http2and3"
  comment             = "On This Day immutable reviewed image renditions"
  default_root_object = null
  price_class         = "PriceClass_All"
  tags                = local.common_tags

  origin {
    domain_name              = aws_s3_bucket.media.bucket_regional_domain_name
    origin_id                = "private-s3-owned-images"
    origin_access_control_id = aws_cloudfront_origin_access_control.media.id
  }

  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD"]
    cached_methods         = ["GET", "HEAD"]
    cache_policy_id        = data.aws_cloudfront_cache_policy.caching_optimized.id
    target_origin_id       = "private-s3-owned-images"
    viewer_protocol_policy = "redirect-to-https"
    compress               = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  # The default *.cloudfront.net certificate always uses CloudFront's TLSv1
  # security policy; a stricter minimum needs a custom domain and certificate.
  viewer_certificate {
    cloudfront_default_certificate = true
    minimum_protocol_version       = "TLSv1"
  }

  # The CloudFront Free flat-rate plan is subscribed manually by the owner in
  # the console, which associates the plan's own AWS WAF web ACL. Terraform
  # must not remove that association on later applies.
  lifecycle {
    ignore_changes = [web_acl_id]
  }
}

data "aws_iam_policy_document" "media_bucket" {
  statement {
    sid    = "AllowCloudFrontReadOnly"
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }

    actions = ["s3:GetObject"]
    resources = [
      "${aws_s3_bucket.media.arn}/quiz-images/*",
      "${aws_s3_bucket.media.arn}/event-images/*",
    ]

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values   = [aws_cloudfront_distribution.media.arn]
    }
  }
}

resource "aws_s3_bucket_policy" "media" {
  bucket = aws_s3_bucket.media.id
  policy = data.aws_iam_policy_document.media_bucket.json
}

data "aws_iam_policy_document" "media_publisher" {
  statement {
    sid       = "ListOnlyTheImmutableImagePrefix"
    effect    = "Allow"
    actions   = ["s3:ListBucket"]
    resources = [aws_s3_bucket.media.arn]

    condition {
      test     = "StringLike"
      variable = "s3:prefix"
      values   = ["quiz-images/*", "event-images/*"]
    }
  }

  statement {
    sid     = "PublishOnlyEncryptedImmutableImageObjects"
    effect  = "Allow"
    actions = ["s3:PutObject", "s3:AbortMultipartUpload"]
    resources = [
      "${aws_s3_bucket.media.arn}/quiz-images/*",
      "${aws_s3_bucket.media.arn}/event-images/*",
    ]

    condition {
      test     = "StringEquals"
      variable = "s3:x-amz-server-side-encryption"
      values   = ["AES256"]
    }
  }
}

resource "aws_iam_policy" "media_publisher" {
  name        = "on-this-day-owned-image-publisher"
  description = "Write-only immutable reviewed image rendition publisher policy"
  policy      = data.aws_iam_policy_document.media_publisher.json
  tags        = local.common_tags
}

resource "aws_iam_role_policy_attachment" "media_publisher" {
  for_each = var.media_publisher_role_names

  role       = each.value
  policy_arn = aws_iam_policy.media_publisher.arn
}

data "aws_iam_policy_document" "deny_paid_pricing_plan_activation" {
  statement {
    sid       = "DenyPaidCloudFrontPricingPlanActivation"
    effect    = "Deny"
    actions   = ["pricingplanmanager:ApprovePaidSubscription"]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "deny_paid_pricing_plan_activation" {
  name        = "on-this-day-deny-paid-cloudfront-plan-activation"
  description = "Prevents automation from activating a paid CloudFront pricing plan"
  policy      = data.aws_iam_policy_document.deny_paid_pricing_plan_activation.json
  tags        = local.common_tags
}

resource "aws_iam_role_policy_attachment" "deployment_pricing_guardrail" {
  for_each = var.deployment_role_names

  role       = each.value
  policy_arn = aws_iam_policy.deny_paid_pricing_plan_activation.arn
}
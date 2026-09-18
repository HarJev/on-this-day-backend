variable "aws_region" {
  description = "The approved media-delivery AWS region. L3 deliberately targets us-east-1."
  type        = string
  default     = "us-east-1"

  validation {
    condition     = var.aws_region == "us-east-1"
    error_message = "L3 is approved only for us-east-1. Review a region change separately."
  }
}

variable "media_bucket_name" {
  description = "Globally unique private S3 bucket name for immutable reviewed image renditions."
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$", var.media_bucket_name))
    error_message = "media_bucket_name must be a DNS-compatible S3 bucket name."
  }
}

variable "deployment_role_names" {
  description = "Existing deployment or agent IAM role names that must be denied paid-plan activation."
  type        = set(string)
  default     = []
}

variable "media_publisher_role_names" {
  description = "Existing IAM role names permitted to upload validated immutable media objects."
  type        = set(string)
  default     = []
}

variable "tags" {
  description = "Additional nonsecret tags for all created resources."
  type        = map(string)
  default     = {}
}

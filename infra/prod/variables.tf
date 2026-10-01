variable "aws_region" {
  description = "Production AWS region. us-east-1 is the approved initial region."
  type        = string
  default     = "us-east-1"

  validation {
    condition     = var.aws_region == "us-east-1"
    error_message = "Only us-east-1 is approved. Review a region change separately."
  }
}

variable "media_bucket_name" {
  description = "Globally unique private S3 bucket name for immutable reviewed image renditions."
  type        = string

  validation {
    condition     = can(regex("^on-this-day-[a-z0-9.-]{1,49}[a-z0-9]$", var.media_bucket_name))
    error_message = "media_bucket_name must be a DNS-compatible S3 bucket name starting with on-this-day-."
  }
}

variable "media_publisher_role_names" {
  description = "Existing IAM role names permitted to upload validated immutable media objects."
  type        = set(string)
  default     = []
}

variable "deployment_role_names" {
  description = "Existing deployment or agent IAM role names that must be denied paid-plan activation."
  type        = set(string)
  default     = []
}

variable "tags" {
  description = "Additional nonsecret tags for media resources."
  type        = map(string)
  default     = {}
}

# --- Scheduled notifications (notifications.tf) ---

variable "firebase_credentials_version" {
  description = "0 = no Firebase parameter. Set to 1 (and bump to rotate) on the apply that writes the key."
  type        = number
  default     = 0
}

variable "firebase_service_account_json" {
  description = "Firebase service-account JSON, passed only on the apply that writes it (TF_VAR_firebase_service_account_json). Never stored in state."
  type        = string
  default     = null
  sensitive   = true
  ephemeral   = true

  validation {
    condition     = var.firebase_service_account_json == null ? true : length(var.firebase_service_account_json) < 4096
    error_message = "The key must be under 4 KB to fit a free Standard-tier parameter."
  }
}

variable "notifications_lambda_zip_path" {
  description = "Path to the built backend ZIP. Leave null to keep the notification function undeployed."
  type        = string
  default     = null
}

variable "notifications_schedule_enabled" {
  description = "Enable the 15-minute schedule. Keep false until the real-device send, budget and alarm checks pass."
  type        = bool
  default     = false
}

variable "notifications_dry_run" {
  description = "Run the deployed function in dry-run mode (lists who is due, sends nothing)."
  type        = bool
  default     = true
}

variable "firebase_project_id" {
  description = "Firebase project ID that owns the app's FCM registration tokens."
  type        = string
  default     = null
}

variable "db_jdbc_url" {
  description = "Production JDBC URL (no password). Required to deploy the notification function."
  type        = string
  default     = null
}

variable "db_user" {
  description = "Production runtime database user."
  type        = string
  default     = null
}

variable "db_password_ssm_parameter_name" {
  description = "SSM SecureString holding the runtime database password. Created by hand at database launch, not here."
  type        = string
  default     = "/on-this-day/prod/db-password"
}

# --- Alerts (alerts.tf) ---

variable "alert_email" {
  description = "Owner email for the budget and alarm. Leave null to create neither."
  type        = string
  default     = null
}

variable "monthly_budget_usd" {
  description = "Monthly AWS cost budget; the owner is emailed when actual or forecast spend exceeds it."
  type        = string
  default     = "1"
}

terraform {
  required_version = ">= 1.8.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }

  # Local state until the owner approves a remote S3 state bucket. State and
  # plan files are ignored by git.
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Application = "on-this-day"
      Environment = "prod"
      ManagedBy   = "terraform"
    }
  }
}

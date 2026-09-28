terraform {
  required_version = ">= 1.10.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }

  # State lives in HCP Terraform (free tier) with local execution: plans and
  # applies run on the operator's machine with their own AWS credentials, and
  # HCP only stores versioned, locked state. Log in with `terraform login`.
  cloud {
    organization = "har-jev-org"

    workspaces {
      name = "on-this-day-prod"
    }
  }
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

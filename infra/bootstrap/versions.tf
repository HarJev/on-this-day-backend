terraform {
  required_version = ">= 1.8.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }

  # Applied once by the account owner. Local state, ignored by git.
}

provider "aws" {
  region = "us-east-1"

  default_tags {
    tags = {
      Application = "on-this-day"
      Component   = "bootstrap"
      ManagedBy   = "terraform"
    }
  }
}

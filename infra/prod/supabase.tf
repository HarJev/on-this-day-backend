# Production database: one Supabase project on the Free plan, used only as
# PostgreSQL (no Supabase Auth, Storage or Data API). Nothing here is managed
# until supabase_project_ref is set. See docs/GO_LIVE.md.
#
# - The project already exists, so it is imported (the import block below),
#   never created here, and prevent_destroy stops any plan that would delete
#   it. Name, region and organization cannot be changed through the API.
# - The provider requires database_password, but it is not a write-only
#   argument, so a real value would be stored in state. The value below is a
#   placeholder that is never sent: changes to it are ignored. The real
#   password is reset in the dashboard and kept in SSM
#   (/on-this-day/prod/db-admin-password), as for the runtime role.
# - instance_size is left unset and ignored: changing it buys paid compute.
# - The Supabase access token comes from TF_VAR_supabase_access_token, an
#   ephemeral variable that never enters state or plan files.
#
# Cost: the organization is on the Free plan ($0, two active projects). SSL
# enforcement and the pooler lookup are free.

variable "supabase_project_ref" {
  description = "Ref of the existing production Supabase project. Leave null to manage nothing in Supabase."
  type        = string
  default     = null
}

variable "supabase_organization_id" {
  description = "Supabase organization slug that owns the project (Organization settings, or the dashboard URL)."
  type        = string
  default     = null
}

variable "supabase_access_token" {
  description = "Supabase personal access token, passed as TF_VAR_supabase_access_token on every plan and apply that manages Supabase. Never stored."
  type        = string
  default     = null
  sensitive   = true
  ephemeral   = true
}

provider "supabase" {
  # The provider is configured on every run, even when it manages nothing, and
  # refuses to start without a token. A placeholder keeps AWS-only plans
  # working; it is never used because no Supabase resource exists then.
  access_token = var.supabase_access_token != null ? var.supabase_access_token : (local.manage_supabase ? null : "supabase-not-managed")
}

locals {
  manage_supabase = var.supabase_project_ref != null

  # The pooler's connection strings look like
  # postgresql://postgres.<ref>:[YOUR-PASSWORD]@<host>:6543/postgres.
  supabase_pooler_urls = local.manage_supabase ? values(data.supabase_pooler.prod[0].url) : []
  supabase_pooler_host = try(regex("@([^:/]+):", local.supabase_pooler_urls[0])[0], null)

  # Lambdas use the transaction pooler (port 6543), which does not support
  # server-side prepared statements, as the runtime role. Explicit db_jdbc_url
  # and db_user values still win.
  db_jdbc_url = try(coalesce(var.db_jdbc_url, local.supabase_pooler_host == null ? null : "jdbc:postgresql://${local.supabase_pooler_host}:6543/postgres?prepareThreshold=0"), null)
  db_user     = try(coalesce(var.db_user, local.manage_supabase ? "otd_runtime.${var.supabase_project_ref}" : null), null)
}

import {
  for_each = local.manage_supabase ? toset([var.supabase_project_ref]) : toset([])
  to       = supabase_project.prod[0]
  id       = each.value
}

resource "supabase_project" "prod" {
  count = local.manage_supabase ? 1 : 0

  organization_id   = var.supabase_organization_id
  name              = "on-this-day"
  region            = var.aws_region
  database_password = "placeholder-never-sent-see-supabase-tf"

  lifecycle {
    prevent_destroy = true
    ignore_changes  = [database_password, instance_size]

    precondition {
      condition     = var.supabase_organization_id != null
      error_message = "Set supabase_organization_id together with supabase_project_ref."
    }
  }
}

# Only SSL enforcement is managed; every other settings category stays as it
# is in the dashboard. Settings have no delete API, so removing this resource
# leaves the project unchanged.
resource "supabase_settings" "prod" {
  count = local.manage_supabase ? 1 : 0

  project_ref     = supabase_project.prod[0].id
  ssl_enforcement = true
}

data "supabase_pooler" "prod" {
  count = local.manage_supabase ? 1 : 0

  project_ref = supabase_project.prod[0].id
}

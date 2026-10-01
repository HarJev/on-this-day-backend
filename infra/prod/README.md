# Owned Image Delivery IaC

This module is an **unapplied** description of the approved L3 delivery shape:

- `us-east-1` S3 Standard storage;
- a private, versioned bucket with Block Public Access and SSE-S3;
- one pay-as-you-go CloudFront distribution using Origin Access Control;
- immutable `quiz-images/<question-id>/<sha256>.<extension>` objects;
- a separately attachable publisher policy limited to that prefix; and
- a deny policy for `pricingplanmanager:ApprovePaidSubscription`, attached to
  every role supplied in `deployment_role_names`.

- a lifecycle rule that expires noncurrent versions after 30 days and aborts
  incomplete uploads after one day.

It deliberately does **not** subscribe to a CloudFront flat-rate plan, upload
objects, create a custom domain, or edit curated image URLs. `terraform apply`
still requires a separate owner approval after a reviewed plan and cost budget.

Before an eventual plan, provide a unique `media_bucket_name` and every
automation/deployment role that will use this module:

```sh
terraform -chdir=infra/prod init -backend=false
terraform -chdir=infra/prod plan \
  -var='media_bucket_name=<approved-unique-name>' \
  -var='media_publisher_role_names=["<approved-publisher-role>"]' \
  -var='deployment_role_names=["<approved-deployment-role>"]'
```

The command above is shown for future owner review only. Do not run it against
an AWS account without the required resource and spend approvals.

## Staying on the $0 Free Plan

The owner approved this module only on the CloudFront **Free** flat-rate plan
($0/month, 1M requests, 100 GB transfer, 5 GB S3 storage credit, no overage
charges; at most three per account). Free plans activate without
`ApprovePaidSubscription`. The plan requires an AWS WAF web ACL, which the
plan provides; Terraform ignores `web_acl_id` so later applies keep it.

1. Review `terraform plan` output with the owner, then apply only after
   explicit approval. Nothing billable exists at this point: an empty bucket
   and an idle pay-as-you-go distribution cost nothing.
2. Before any object is uploaded, the owner opens the distribution in the
   CloudFront console and subscribes it to the **Free** plan. Never pick Pro,
   Business, or Premium.
3. Confirm the distribution shows the Free plan and its web ACL, then publish
   objects. Use SSE-S3 only; SSE-KMS would add KMS charges.

Terraform state lives in the HCP Terraform workspace `on-this-day-prod`
(see `infra/README.md`).

## Scheduled Notifications

`notifications.tf` and `alerts.tf` add the daily notification resources. With
default variables they create nothing:

- `firebase_credentials_version >= 1` creates only the free Standard-tier SSM
  SecureString for the Firebase key, written through a write-only argument so
  the key never enters state. Pass the key with
  `TF_VAR_firebase_service_account_json` on that apply only.
- `notifications_lambda_zip_path` deploys the function, its roles, log group,
  error alarm, and a **disabled**, dry-run 15-minute schedule.
- `alert_email` adds the email topic and the monthly budget.

Offline checks with a mocked provider (no credentials, no state):

```sh
terraform -chdir=infra/prod init -backend=false
terraform -chdir=infra/prod test
```

Launch order and cost notes: `docs/NOTIFICATIONS.md`.

## Public API

`api.tf` adds the API with default variables creating nothing. Setting
`api_lambda_zip_path` (plus `db_jdbc_url` and `db_user`) creates the
`on-this-day-api` function, its role and log group, a function URL with
`AWS_IAM` auth, a CloudFront distribution that alone may invoke it (origin
access control), an origin-controlled cache policy, and an error alarm. The
function reads the database password from `db_password_ssm_parameter_name`
and connects with `verify-full` TLS. The `api_base_url` output is the release
build's API URL (the CloudFront address).

After apply, subscribe the `api_distribution_id` distribution to the
CloudFront **Free** plan and add a per-IP rate-based rule to its web ACL.
`api_reserved_concurrency` stays unset until the account's Lambda quota allows
reserving. Security model, mobile header change, and smoke tests:
`docs/API_SECURITY.md`. Everything is inside free allowances; cost notes are
at the top of `api.tf`.

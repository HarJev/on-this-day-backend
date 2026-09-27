# Production Terraform Root

The single application root. `media.tf` holds the approved L3 owned-image
delivery shape:

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

Run it as the `on-this-day-terraform` deployer from `infra/bootstrap`, never
root. Role variables are optional; leaving them empty attaches nothing to
existing roles:

```sh
export AWS_PROFILE=on-this-day
terraform -chdir=infra/prod init
terraform -chdir=infra/prod plan -out=prod.tfplan \
  -var='media_bucket_name=on-this-day-media-<account-id>'
terraform -chdir=infra/prod apply prod.tfplan   # only after owner approval
```

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

Terraform state stays local and out of git; creating a remote state bucket is
a separate owner decision.

# Owned Image Delivery IaC

This module is an **unapplied** description of the approved L3 delivery shape:

- `us-east-1` S3 Standard storage;
- a private, versioned bucket with Block Public Access and SSE-S3;
- one pay-as-you-go CloudFront distribution using Origin Access Control;
- immutable `quiz-images/<question-id>/<sha256>.<extension>` objects;
- a separately attachable publisher policy limited to that prefix; and
- a deny policy for `pricingplanmanager:ApprovePaidSubscription`, attached to
  every role supplied in `deployment_role_names`.

It deliberately does **not** subscribe to a CloudFront flat-rate plan, upload
objects, create a custom domain, or edit curated image URLs. `terraform apply`
still requires a separate owner approval after a reviewed plan and cost budget.

Before an eventual plan, provide a unique `media_bucket_name` and every
automation/deployment role that will use this module:

```sh
terraform -chdir=infra/media init -backend=false
terraform -chdir=infra/media plan \
  -var='media_bucket_name=<approved-unique-name>' \
  -var='media_publisher_role_names=["<approved-publisher-role>"]' \
  -var='deployment_role_names=["<approved-deployment-role>"]'
```

The command above is shown for future owner review only. Do not run it against
an AWS account without the required resource and spend approvals.

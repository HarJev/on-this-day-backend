# Bootstrap

Creates the `on-this-day-terraform` IAM user that runs `infra/prod`, its scoped
policy, and the permissions boundary every workload role must carry. Apply it
rarely, with an owner/admin identity, after a reviewed plan:

```sh
terraform -chdir=infra/bootstrap init
terraform -chdir=infra/bootstrap plan -out=bootstrap.tfplan
terraform -chdir=infra/bootstrap apply bootstrap.tfplan   # owner approval only
```

Terraform creates no access key, so no secret enters state. The owner creates
the key in the IAM console and runs `aws configure --profile on-this-day`.

The deployer can manage S3 buckets and IAM roles/policies named
`on-this-day-*`, CloudFront, Budgets, and (in us-east-1) Lambda, API Gateway,
EventBridge/Scheduler, CloudWatch, and SNS. Roles it creates must carry the
`on-this-day-workload-boundary`. It is denied paid CloudFront plan activation,
secret values, user/credential changes, role assumption, editing its own
guardrail policies, account/billing changes, and regional calls outside
us-east-1.

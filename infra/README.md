# Infrastructure

All On This Day cloud infrastructure is Terraform in this directory. The
mobile app owns no AWS resources.

```
infra/
  bootstrap/  applied rarely, by the account owner: the Terraform user and its limits
  prod/       the one production root: media.tf now, API/Lambda files later
```

`prod/` is a single flat root with one file per concern, so one plan shows
every production change and a build can run
`terraform -chdir=infra/prod plan` after packaging the Lambda. Modules and
per-environment folders are deliberately deferred until a staging environment
exists. `bootstrap/` stays separate so the identity that runs `prod/` cannot
rewrite its own permissions.

State is local and ignored by git until the owner approves a remote S3 state
bucket (see `docs/PRODUCTION_DEPLOYMENT_PLAN.md`).

## Order

1. `bootstrap/`, once, with the owner's root sign-in: creates the
   `on-this-day-terraform` user, its policy, and the workload permissions
   boundary. The owner then adds a console password and MFA to that user in
   the console and signs the CLI in with `aws login --profile on-this-day`.
   Root is not used again for Terraform.
2. `prod/` with `AWS_PROFILE=on-this-day`: copy `terraform.tfvars.example` to
   `terraform.tfvars`, then `init`, `validate`, `plan`, and apply only after
   the owner approves the saved plan.

Nothing here may be applied without explicit owner approval of the plan.

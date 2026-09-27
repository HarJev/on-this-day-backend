# Infrastructure

All On This Day AWS infrastructure is Terraform in this directory. The mobile
app owns no AWS resources.

```text
infra/
  bootstrap/  # applied rarely by the owner: the deployer identity and its guardrails
  prod/       # the single application root: one file per concern (media.tf, later api.tf, ...)
```

- `bootstrap/` is separate so the identity that runs `prod/` cannot rewrite
  its own permissions. Apply it with an owner/admin identity only.
- `prod/` is the one root a build pipeline plans and applies, using the
  `on-this-day-terraform` deployer created by `bootstrap/`. Extract modules
  only if a second environment is approved.
- State is local and gitignored for now. A private, versioned S3 state bucket
  is a separate owner decision (see `docs/PRODUCTION_DEPLOYMENT_PLAN.md`).
- Every `apply` needs a reviewed plan and an explicit owner go-ahead.

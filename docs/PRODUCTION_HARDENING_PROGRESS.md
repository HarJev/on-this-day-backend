# Production Hardening Progress

Handoff doc for the tracker items "three backend hardening changes" and
"`api.tf`" (production tracker, 2026-10-01). Branch
`claude/project-thread-l0fpmh`, from main `ecd8e72`. If Claude usage runs out,
pick up from the first step that is not complete.

## Status

| Step | State |
| --- | --- |
| 1. API reads its database password from SSM | Complete |
| 2. Verified TLS (`verify-full`) for production connections | Complete in code; CA file pending (see below) |
| 3. Safe credential input for Flyway and the importers | Complete |
| 4. `infra/prod/api.tf` (Lambda, HTTP API, throttling, logs, alarm) | Complete |
| 5. Tests: unit, integration, Terraform | Complete (266 unit, 76 integration, 9 Terraform). `sam build` not run here (no SAM CLI in the cloud container) |
| 6. PR #28 opened and handed to the coordinator for review | Complete; review thread pending |

## What Changed

- `DatabaseConfig.resolve` is the one rule for deployed functions: when
  `DB_PASSWORD_SSM_PARAMETER` is set, the password comes from that SSM
  SecureString (`DB_PASSWORD` is ignored), and the connection must use
  `sslmode=verify-full` with a root certificate, defaulting to the bundled
  Supabase CA. Anything weaker fails at startup. The API
  (`RuntimeApiComposition`) and the notification function both use it.
  `ParameterReader` and `SsmParameterReader` moved to `platform/runtime`.
- `PostgresDataSourceFactory` applies `sslmode`, `sslrootcert` and the
  validating SSL factory after the JDBC URL, so a URL query string such as
  `sslmode=disable` cannot weaken them. A `classpath:` certificate is copied
  to a temp file once, because the driver reads certificates from files.
- `CommandDatabaseConfig` gives operator commands the password from SSM, then
  `DB_PASSWORD` (local), then a hidden terminal prompt. New
  `DatabaseMigrationCommand` runs Flyway that way. Both importers accept
  `[contentDir]` only and read the database from the environment; the old
  `<jdbcUrl> <user> <password>` form still works for local databases and warns.
  `ContentStatusCommand` uses the same resolution.
- `infra/prod/api.tf`, created only when `api_lambda_zip_path` is set: log
  group (14 days), role with the workload boundary and read access to the DB
  password parameter only, `on-this-day-api` Lambda (Java 21, arm64, 1 GB,
  15 s), HTTP API with the nine app routes, `$default` stage throttled to 10
  requests a second (burst 20), invoke permission, and an error alarm.
  Output `api_base_url`.

## Remaining Before Deploy (owner or launch thread)

1. **Supabase CA file.** When the production Supabase project exists, download
   its CA from Database Settings > SSL Configuration and commit it as
   `src/main/resources/certs/supabase-prod-ca-2021.crt` (a public certificate,
   not a secret). Until then a deployed function refuses to start with a clear
   message, which is the intended fail-closed behavior.
2. **SSM parameters**, created by hand in the console (SecureString, Standard
   tier, `aws/ssm` key): `/on-this-day/prod/db-password` for the runtime role
   and `/on-this-day/prod/db-admin-password` for migrations and imports.
   Typing the value in the console keeps it out of shell history.
3. **`db_jdbc_url` for the functions:** the Supavisor transaction port suits
   short Lambda connections, and it does not support server-side prepared
   statements, so use
   `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres?prepareThreshold=0`
   (check the host on the project's Connect page). Smoke-test after deploy.
4. No new IAM: the deployer policy already allows API Gateway, Lambda and log
   groups named `on-this-day-*`, and passing roles to Lambda.

## Superseded By The Function URL Decision

On 2026-10-01 the owner chose a free Lambda function URL over API Gateway.
`api.tf` now creates a function URL locked to its own CloudFront distribution
instead of the HTTP API and stage throttle described above. See
`docs/API_SECURITY.md`.

## Cost

At the time, only API Gateway was outside an always-free allowance (now removed): $1.00 per million
requests, about $0.03 a month at 30,000 requests and about $1.50 at 1.5
million. Lambda, logs, the alarm and SSM stay inside free allowances at these
volumes. Details are in the header of `infra/prod/api.tf`.

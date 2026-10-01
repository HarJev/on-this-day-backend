# Production Backend Deployment Plan

**Status:** Proposed deployment design, not deployed. No account, region, price,
or cloud resource has been verified in the owner's AWS or Supabase accounts.
Provisioning and spend require a separate approval.

This plan covers the existing daily-history, device, and quiz APIs. It follows
the launch gates in the mobile repository's
`docs/PRODUCTION_LAUNCH_WORKPLAN.md`; it does not declare the product ready for
release or replace its content and native-device gates.

**How to use this file:** execute the numbered procedure at the end in order.
The present repository does not yet contain production Terraform, a deployed
secret loader, or a scheduled notification handler. Steps that depend on those
artifacts tell the agent to build and verify them before proceeding. `STOP`
means report the evidence and wait for the owner; it is not implied approval.

## Decision

Use **Supabase Pro as PostgreSQL only** for the first production release, plus
the existing Java 21 AWS Lambda and API Gateway HTTP API. Do not use Supabase
Auth, Storage, or its Data API for the mobile application. Keep Docker Compose
PostgreSQL and SAM for local development. Keep RDS as a migration option if
network isolation, throughput, or operational needs justify its fixed cost.

Supabase's **Data API** is its automatically generated REST/GraphQL interface
to Postgres, commonly used by Supabase client SDKs and guarded by database
grants and Row Level Security. It is not our Java API Gateway API. This app
already has its own Java HTTP API and connects server-side with JDBC, so
exposing a second direct-to-database API adds risk without a product benefit.
Turning the Data API off does not turn Postgres, JDBC, Flyway, or the Java API
off. [Supabase Data API security](https://supabase.com/docs/guides/api/securing-your-api).

Why: the backend already uses portable PostgreSQL, JDBC, and Flyway. The
architecture prefers a managed database reachable over TLS without placing
Lambda in a VPC. Supabase Pro currently starts at **US$25/month** for the first
project with Micro compute and includes seven days of daily backups. This is a
planning price, not a provider commitment or a full bill. One additional
always-on project starts at **US$10/month** on the same paid organization; do
not create permanent staging before its cost is approved. Free projects can
pause after inactivity and do not meet the production availability/backup
requirements. [Supabase pricing](https://supabase.com/pricing),
[backup policy](https://supabase.com/docs/guides/platform/backups).

RDS is not rejected permanently. Its instance and allocated storage are billed
while idle, and some VPC outbound designs add a NAT gateway hourly charge.
If RDS is later selected, prepare a separate network/security/cost review;
do not assume NAT is required merely because RDS is in a VPC.
[RDS pricing](https://aws.amazon.com/rds/postgresql/pricing/),
[NAT pricing](https://aws.amazon.com/vpc/pricing/).

Do **not** switch platforms for this release without a separate proof of
concept and approval. It would replace the locally exercised Lambda/API Gateway
adapter, SAM workflow, Terraform provider/resources, logging and deployment
runbooks, and Java SnapStart. Cloud Run's Java cold start and real
Lambda-to-Supabase latency would need measurement; its scale-to-zero benefit
alone is not enough evidence to change a working stack.

## Initial Deployment Shape

- Choose one AWS commercial region and the closest available Supabase database
  region together; **us-east-1 is the initial candidate**, not a provisioned
  fact. Measure real Lambda-to-database latency before locking it in.
- Deploy one Java 21 **arm64** ZIP Lambda for all current HTTP routes behind API
  Gateway HTTP API. Preserve exact routes from local `template.yaml`; use that
  file for local SAM, not as a second owner of cloud resources.
- Publish function versions and point API Gateway at a `prod` alias. Enable
  Java SnapStart on published versions only, after validating initialization
  and restore behavior. Do not enable provisioned concurrency or a VPC for the
  API Lambda. Java SnapStart supports arm64 and has no additional SnapStart
  charge. [AWS arm64 support](https://aws.amazon.com/about-aws/whats-new/2024/07/aws-lambda-snapstart-java-functions-arm64-architecture/),
  [SnapStart limitations](https://docs.aws.amazon.com/lambda/latest/dg/snapstart.html).
- Start with the existing 1024 MiB and 30-second ceiling, then right-size from
  real cloud timings. Set initial API reserved concurrency to **5** as a
  protective limit, and adjust after load testing against the database's
  connection limit. Reserved concurrency has no standing charge.
- Use the API Gateway `execute-api` HTTPS domain for deployment smoke tests.
  Before a public mobile build, prefer an owned `api.<domain>` hostname so an
  API Gateway replacement does not require a mobile update. Domain purchase
  and Route 53 are separately approved costs; if deferred, explicitly accept
  the risk of shipping the generated API URL. Set stage/route throttles,
  particularly for anonymous device writes and quiz generation; these are
  best-effort controls, not authentication or a guaranteed cost cap.
- Use private S3 plus CloudFront Origin Access Control for reviewed quiz/event
  image renditions **only after** the image-delivery task and price approval.
  Preserve source, creator, attribution, and license metadata. The proposed
  on-device image cache needs no cloud resource. No image-proxy Lambda.
- A notification Lambda and EventBridge schedule are separate from the HTTP
  Lambda. They are implemented (`docs/NOTIFICATIONS.md`) but **not deployed**. Decide whether scheduled
  notifications are a release requirement before calling this a full launch.

## Database Connection And Security

1. Create one production Supabase project on Pro. **Disable the Data API
   before running Flyway.** Existing migrations create tables in `public`,
   which some Supabase project configurations expose through generated APIs.
   Verify that anonymous and authenticated API roles cannot read/write any
   application table. Do not put a Supabase publishable key or database
   credentials in Flutter. [Supabase API security](https://supabase.com/docs/guides/api/securing-your-api).
2. Enforce SSL on database connections. Configure pgJDBC with
   `sslmode=verify-full`, and bundle the project's downloaded CA certificate
   as a **public trust asset**, not a secret. Verify CA and hostname against
   the actual connection endpoint in deployment smoke tests. `sslmode=require`
   alone encrypts but does not verify identity.
   [Supabase SSL guidance](https://supabase.com/docs/guides/platform/ssl-enforcement),
   [pgJDBC SSL guidance](https://jdbc.postgresql.org/documentation/ssl/).
3. For the first Lambda deployment, use the Supabase **shared session pooler**
   endpoint on port `5432`, copied exactly from the project's Connect dialog.
   It is IPv4 reachable and preserves session behavior, avoiding a speculative
   change to the current JDBC/Flyway prepared-statement behavior. Do not
   construct its hostname or reuse the direct-connection username: pooled
   usernames include the project reference. The transaction pooler on port
   `6543` does not support server-side prepared statements and would need a
   separately tested driver configuration. Review pooler/DB connection metrics
   before changing modes. [Supabase connection modes](https://supabase.com/docs/guides/database/connecting-to-postgres).
4. Use an admin/migration role only in a controlled migration/import run.
   Create a distinct runtime login with the minimum table/sequence privileges
   required by current read paths, Daily assignment writes, and device
   registration writes. Test the full API using that role; never use `postgres`
   or a service-role key in the Lambda. Keep Lambda database concurrency low.
5. Supabase IP restrictions are optional only after the egress design is
   settled. A VPC-less Lambda does not give this plan a fixed outbound IP, so
   do not add an allowlist that silently blocks production. The launch
   security boundary is verified TLS, strong rotated credentials, least
   privileges, the disabled Data API, and monitored access. Revisit fixed
   egress/private networking if risk requirements change.

## Secrets And Operations

- Store the runtime database password in AWS Secrets Manager; keep only its ARN
  and nonsecret endpoint/user/timeout settings in Lambda configuration. The
  production runtime must fetch/cache the secret **after** SnapStart restore,
  refresh it on rotation or authentication failure, and never log it. The
  current runtime reads `DB_PASSWORD` from the environment, so this is a
  required code change before deployment, not something Terraform alone can
  make secure. Do not embed secret values in Terraform state, plans, source,
  Lambda environment variables, or SAM local files committed to Git.
- Keep migration/import credentials outside Lambda and Terraform state. Adapt
  the existing Maven/Flyway/import commands for environment- or file-based
  credential input so a production password is not visible in shell history or
  process arguments. Continue to validate content before import and use the
  existing idempotent importers.
- If scheduled FCM sending ships, give its Lambda a separate IAM role and
  credential path. Prefer short-lived federated Google credentials if tested;
  otherwise store and rotate the Firebase service-account secret in Secrets
  Manager. Never bundle it in the artifact. Test a real send with an opted-in
  device and confirm duplicate-send protection before enabling the schedule.
- Configure CloudWatch log retention (initially 14 days), error/5xx,
  throttling, duration, and database-connection alarms, with a small AWS
  budget alert. Avoid raw request bodies, passwords, FCM tokens, and full
  connection URLs in logs. **Current code logs the raw request path**, so
  `DELETE /v1/devices/{token}` can leak a token into Lambda logs. Sanitize it
  and avoid raw-path API Gateway access-log fields before production.
- Create a production backup/restore runbook and perform a restore drill.
  Supabase Pro's daily backups are not point-in-time recovery; up to roughly
  a day of writes may be lost without an additional paid recovery feature.
  Export separately if the owner requires longer retention or portability.

## Infrastructure Ownership And Delivery

Use Terraform for cloud resources: API Gateway HTTP API/routes/stage/integration,
Lambda function/version/alias/permissions, least-privilege IAM, log groups,
alarms, budget, Secrets Manager secret references, and approved S3/CloudFront
resources. Keep project/region/endpoint identifiers as configuration; do not
commit actual credentials. Terraform must **not** create Supabase resources
unless a later provider and account-management decision explicitly approves it.

The earlier `implementation_plan.md` calls for local Terraform state. For a
production release, **propose superseding that default** with a private,
encrypted, versioned S3 state bucket and S3 lockfile, using separate production
state from any staging state. A one-person local-state bootstrap may be used
only until the remote state bucket is approved; never commit state. The remote
bucket is separate from the media bucket. No DynamoDB lock table is required
by current Terraform's S3 lockfile option.
[Terraform S3 backend](https://developer.hashicorp.com/terraform/language/backend/s3).

Build with Java 21, run tests and `sam build`, and create one reproducible ZIP
artifact from the SAM build output for Terraform's Lambda upload. Verify the
artifact contains the handler and runtime dependencies. Do not let both SAM
and Terraform deploy the same API. Use a reviewed Terraform plan and an
explicit owner go-ahead before any apply. Promote a tested artifact/version
to the `prod` alias; retain a previous version for rollback.

## Initial Cost Guardrails

At rest, budget **approximately US$25-35/month** for one Supabase Pro project,
a small number of AWS secrets/alarms, tiny state/media storage, and optional
DNS. This is an estimate excluding requests, Lambda duration, bandwidth,
image retrievals, taxes, domain registration, and native store fees. A
second always-on Supabase project adds at least US$10/month on the current
plan. CloudFront pay-as-you-go has no distribution-hour charge; the optional
flat-rate Free plan is US$0/month only if the AWS account is eligible. Check
the actual account and service limits before provisioning. Do not opt into a
paid CDN plan, NAT gateway, RDS Proxy, provisioned concurrency, or RDS by
default. [CloudFront pricing](https://aws.amazon.com/cloudfront/pricing/),
[flat-rate eligibility](https://docs.aws.amazon.com/PricingPlanManager/latest/UserGuide/plans.html),
[Secrets Manager pricing](https://aws.amazon.com/secrets-manager/pricing/).

## Step-by-Step Execution Procedure

These steps are **instructions for future work**, not claims that the work or
commands have succeeded. Use a dedicated `codex/` branch/worktree for code and
Terraform; preserve the current uncommitted content. Before changing code,
present the scoped implementation plan for review as `AGENTS.md` requires.
Record command results and cloud resource IDs in a private deployment record,
not in source control. Never paste secrets into prompts, shell history, logs,
Terraform variables/state, or issue comments.

### 0. Owner Decisions And Cost Preflight

1. Read this file, `AGENTS.md`, `docs/ARCHITECTURE.md`, `docs/API_CONTRACT.md`,
   `docs/SETUP.md`, and the mobile launch workplan. Inventory the actual code,
   migrations, content coverage, current AWS/Supabase resources, and Git status.
2. Ask the owner to identify the AWS account, Supabase organization, billing
   currency, target audience/region, owned domain (if any), and acceptable
   monthly budget. Do not request passwords or private keys in chat.
3. Confirm the decisions: Supabase Pro first project, initial paired regions,
   temporary staging project or local-only staging, production Terraform state
   location, API hostname, image-hosting choice, backup recovery objective,
   and whether scheduled notifications are mandatory for launch.
4. Recheck the current provider prices and the account's CloudFront plan
   eligibility. Make an AWS budget alert plan covering usage as well as idle
   charges. Estimate the worst plausible monthly spend from API abuse and
   image egress; the US$25-35 idle estimate is **not** a spending cap.

**STOP:** no paid project, state bucket, domain, CDN plan, or AWS deployment
until the owner approves the specific resources and estimated spend.

### 1. DP1 - Runtime And Security Hardening (Local, No Cloud Apply)

1. Add a small production credential provider to runtime composition. Keep
   `DB_PASSWORD` for local SAM only; deployed Lambda must resolve a Secrets
   Manager secret after restore, cache briefly, and refresh on rotation/auth
   failure. Keep network connections and time-dependent credentials out of
   SnapStart initialization.
2. Require verified PostgreSQL TLS for the deployed configuration. Package
   the selected Supabase CA and test `verify-full`; keep local Compose settings
   explicit and independent. Fail closed if production TLS is missing.
3. Stop logging raw device-token path segments in the Lambda and planned API
   Gateway access logs. Review all error, SQL, and notification logging for
   token, credential, full JDBC URL, and request-body exposure.
4. Give Flyway and the two import commands a secure environment/file input
   path for production credentials. Do not pass a real password in Maven
   `-Dexec.args`, `-Dflyway.password`, or a shell command line.
5. Add tests for credential loading/rotation, TLS configuration, log
   redaction, least-privilege assumptions, and unchanged API behavior. Run:

   ```sh
   mvn -B test
   mvn -B verify -Pintegration
   sam build
   git diff --check
   ```

   If Docker/SAM is unavailable, record that step as **PENDING**, not passed.

**Exit:** review the diff and test evidence; no raw secrets/tokens in logs and
no new API behavior. No Supabase or AWS resource is required for this step.

### 2. DP2 - Disposable Supabase Compatibility Proof

1. After approval, create a **disposable** Supabase project in the chosen
   region. Prefer an available free test slot; approve any extra paid project
   first. Turn the Data API off before creating application tables and verify
   it does not respond. Enforce SSL and download the project's CA.
2. Copy the exact **session pooler** host and username from its Connect dialog.
   Test `verify-full` using a temporary, non-logged credential source. Measure
   connection acquisition and a representative query from an environment
   comparable to Lambda. Do not assume `localhost`, the direct hostname, or
   the direct username works from Lambda.
3. Create distinct migration and runtime database roles. Apply Flyway V1
   through the latest migration, then validate/import the reviewed historical
   and quiz JSON using the new secure credential path. Run the existing
   repository/ingestion integration tests separately against local Testcontainers;
   do not point destructive tests at the hosted project.
4. With the restricted runtime role, exercise Today, Event Detail, catalog,
   Quick Play, Daily assignment creation/reread, and device registration/deletion.
   Verify direct JDBC works while generated Data API access is denied. Observe
   pooler/DB connection use at the proposed concurrency of five.
5. Delete the disposable project only after saving nonsecret evidence and
   confirming no needed data lives there. Review provider backup/restore
   behavior before relying on it for production.

**STOP:** if Flyway, triggers, permissions, prepared statements, TLS hostname
verification, or connection limits fail, fix and repeat this step. Do not
work around failures by using the admin role in the API.

### 3. DP3 - Terraform And Reproducible Artifact (Offline First)

1. Implement the Terraform-managed API Gateway routes, ZIP Lambda, `prod`
   alias, Java SnapStart, reserved concurrency, IAM, throttles, log retention,
   alarms, budget, and secret **references**. Keep the local SAM template for
   development only. Do not configure a VPC, provisioned concurrency, NAT,
   RDS, or RDS Proxy by default.
2. Implement a deterministic Java 21 build/package step from `sam build`
   output to the ZIP used by Terraform. Verify handler/dependencies are
   present and the deployed artifact has a content hash; do not have SAM and
   Terraform both own the cloud API.
3. Decide the state backend from Step 0. For production, prefer a separately
   approved private/versioned/encrypted S3 bucket with S3 lockfile and a
   separate state key. Bootstrap it deliberately; never commit state, a plan
   containing secrets, or credentials. This supersedes the older local-state
   default only after the owner confirms that decision.
4. Once `infra/` exists, run the non-provisioning checks:

   ```sh
   terraform -chdir=infra fmt -check -recursive
   terraform -chdir=infra init -backend=false
   terraform -chdir=infra validate
   ```

   Run a real `terraform plan` only with the approved target account/state
   configuration. Review the plan for exact routes, destructive changes,
   secret values, unexpected hourly resources, IAM breadth, and cost.

**STOP:** a clean plan is not permission to run `terraform apply`. Hand the
exact plan and monthly estimate to the owner for separate approval.

### 4. DP4 - Image Delivery (Parallel Product Gate, Separate Spend Approval)

1. Follow the mobile launch workplan's image tasks. Prepare reviewed,
   correctly licensed renditions and a manifest retaining provenance. Prove
   cold first fetch and warm on-device cache on the available native targets.
2. Compare eligible CloudFront Free versus pay-as-you-go plus S3 and any
   DNS costs in the actual account. If approved, implement a private S3 origin
   with CloudFront Origin Access Control and immutable asset URLs. Do not
   create a public-write bucket or image-proxy Lambda.
3. Update only the imported image URLs after the owned origin passes the same
   image-preparation limits and visual checks. Re-import idempotently, verify
   API provenance, and retain source/creator/license URLs unchanged.

**STOP:** if the first uncached image fetch is unreliable, this gate is not
passed merely because the app's disk cache works on later requests.

### 5. DP5 - Production Database And API Release (Two Owner Approvals)

1. **Approval A:** create one production Supabase Pro project, with Data API
   disabled, SSL enforced, CA recorded, backup settings reviewed, and
   separate admin/runtime roles. Store the runtime password in Secrets
   Manager without putting its value into Terraform state. Use the secure
   migration/import path to run Flyway and both validated importers. Check
   row counts, source/image provenance, quiz catalog, and current-date
   availability before making the API public.
2. **Approval B:** inspect the final Terraform plan and then apply it. Deploy
   the versioned artifact/alias, HTTPS API, alarms, and budget. Start with the
   generated `execute-api` URL for a private smoke test. Only after domain
   approval, bind the stable `api.<domain>` hostname for the public mobile
   build. Keep the previous Lambda version available for code rollback.
3. With `API_BASE_URL` pointing to the deployed test endpoint, run non-destructive
   HTTP checks. Ensure the actual current date has curated content; this API
   has no arbitrary-date query parameter. Use a separately known event ID for
   the Event Detail check:

   ```sh
   curl -i "$API_BASE_URL/v1/health"
   curl -i "$API_BASE_URL/v1/days/today?timezone=America/Jamaica"
   curl -i "$API_BASE_URL/v1/quizzes/catalog"
   curl -i -X POST "$API_BASE_URL/v1/quizzes/quick-play" \
     -H 'Content-Type: application/json' -d '{"questionCount":5}'
   curl -i "$API_BASE_URL/v1/quizzes/daily?timezone=America/Jamaica&questionCount=5"
   ```

   Also check Event Detail and the 5/10/20 stable Daily prefixes. A `503`
   Today response on an uncovered current date is a **content release failure**,
   not a successful API smoke. Exercise device writes with an opted-in test
   device, then remove its registration; never print its token in evidence.
4. Measure cold/warm latency, SnapStart restore, real DB connection time,
   errors, throttling, log redaction, and alarms. Use a test build of the
   Flutter app against the deployed HTTPS endpoint before shipping a public
   binary. No credential or Supabase API key goes into that build.
5. Rehearse rollback: move the alias back to the last working Lambda version.
   Database migrations/content are **not** rolled back by moving an alias or
   running `terraform destroy`; a failed data change follows the backup/
   restore or forward-fix runbook. Perform a restore drill on a safe target.

**STOP:** do not label this deployed API launch-ready if any security, backup,
current-date content, image, or native integration check remains pending.

### 6. DP6 - Scheduled Notifications (If In Release Scope)

**Status 2026-10-01:** in release scope. Handler, delivery table, SSM key
loader, tests and Terraform (`infra/prod/notifications.tf`, schedule disabled)
are implemented; see `docs/NOTIFICATIONS.md` for the delivery rules and launch
steps. The Firebase key uses a free Standard SSM SecureString rather than
Secrets Manager.

1. Implement and test the EventBridge-triggered notification handler separately
   from the API. Confirm timezone/date semantics, retry and duplicate-send
   protection, token cleanup, FCM error classification, and dry-run behavior.
2. Configure a dedicated execution role and tested Google credential approach;
   prefer keyless federation when feasible. Do not publish a service-account
   key in Git, Lambda environment variables, or Terraform state.
3. After approval, deploy with the schedule **disabled**. Send once to a
   consenting test device, inspect redacted logs and the `eventId` deep link,
   then explicitly approve enabling the schedule. Document how to disable it
   immediately if delivery or targeting is wrong.

**STOP:** device registration plus a manual sender is not evidence that daily
scheduled notifications are live. Explicitly de-scope this feature if the
first public release does not include it.

### 7. DP7 - Public Release Signoff

1. Gather a checklist with evidence for backend deployment, 366-day/event
   coverage, reviewed quiz bank, first-fetch image delivery, database restore,
   costs/alarms, FCM decision, and iOS/Android native tests. The mobile
   workplan owns its additional device, accessibility, store, and privacy gates.
2. Mark every item **PASS**, **FAIL**, or **PENDING**. Do not convert an
   environment limitation or an untested platform into PASS.
3. Publish only after all required gates pass or the owner approves and records
   a narrower release scope. Keep the runbook, secret-rotation instructions,
   and rollback owner available after launch.

No infrastructure has been created by writing this document.

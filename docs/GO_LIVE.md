# Go Live: API, Push Function and Supabase

Owner runbook for the first production deploy on the $0 stack: Supabase Free
(Postgres only), the API Lambda behind a function URL and its own CloudFront
Free-plan distribution (`docs/API_SECURITY.md`), and the notification Lambda
with its schedule disabled and dry-run (`docs/NOTIFICATIONS.md`). Every step
is run by the owner; nothing here has been applied.

Where this disagrees with `docs/PRODUCTION_DEPLOYMENT_PLAN.md` (Supabase Pro,
the session pooler for Lambda, API Gateway), this file and the documents above
are current.

Run the shell steps on the Mac from an up-to-date `main` checkout, in one
terminal so the variables carry over.

## Cost Guardrails

Everything below is inside free allowances. Do not turn on the Supabase IPv4
add-on or any other add-on, pick only the CloudFront **Free** plan, keep SSM
parameters on the Standard tier with `alias/aws/ssm`, and stop if any console
screen shows a price.

Known limits of the free setup: a Supabase Free project pauses after about a
week without database activity (restoring it is free) and has no backups.
Content can be re-imported from Git; device registrations would be lost.

## A. Supabase

1. **Project:** one project named `on-this-day` on the Free plan in East US
   (North Virginia). Production: ref `tdtmciqzyhrxevqdvzer`, region
   `us-east-1`, organization `pbdjmyjjbhjassdpzfmq` (Free), created
   2026-10-02 through the Supabase connector. Terraform (`infra/prod/supabase.tf`) imports it rather
   than creating it, so create it in the dashboard or through the Supabase
   connector. Reset its database password under Project Settings, Database,
   and save it as the *admin* password (Terraform never holds it). Create a
   personal access token under Account, Access Tokens, for Terraform.
2. **Dashboard settings Terraform cannot make:** Integrations, Data API: turn
   **Enable Data API** off. Database, Settings, SSL Configuration: download
   the certificate to `~/Documents/on-this-day/supabase/prod-ca-2021.crt`
   (Terraform enforces SSL). From Connect, Session pooler, note the pooler
   host; the project ref is under Project Settings, General.
3. **Check verified TLS** (prompts for the admin password; prints a version
   line on success). If it fails with `certificate verify failed` or any TLS
   error, stop: the deployed functions refuse to start without verified TLS,
   so nothing later in this runbook will work until it passes.

   ```sh
   export PROJECT_REF=<project-ref>
   export POOLER_HOST=<pooler-host>
   export CA="$HOME/Documents/on-this-day/supabase/prod-ca-2021.crt"
   psql_admin() { docker run --rm -it -v "$(dirname "$CA"):/c:ro" -v "$PWD:/w:ro" -w /w postgres:16-alpine \
     psql "host=$POOLER_HOST port=5432 dbname=postgres user=postgres.$PROJECT_REF sslmode=verify-full sslrootcert=/c/$(basename "$CA")" "$@"; }
   psql_admin -c 'select version();'
   ```

4. **Commit the certificate** as
   `src/main/resources/certs/supabase-prod-ca-2021.crt` through a PR. It is a
   public certificate, not a secret. The Lambda ZIP build refuses to run
   without it.

## B. AWS Preparation

5. **Sign in:**

   ```sh
   export AWS_PROFILE=on-this-day AWS_REGION=us-east-1
   aws login --profile on-this-day
   aws sts get-caller-identity   # Arn ends in user/on-this-day-terraform
   ```

6. **Workload boundary.** Managed by `infra/prod/iam.tf` (the deployer may
   edit it since 2026-10-02), so nothing to do by hand. Its Lambda decrypt
   rule is what lets a function read its own environment variables. The
   `on-this-day-terraform` policy must have `ProjectParameters` and
   `DescribeParameters`, and its `ProtectOwnPermissions` deny must list only
   its own policy, not the boundary.
7. **Password parameters (console).** Generate the runtime password
   (`openssl rand -base64 33 | tr -d '/+=' | cut -c1-32`) and keep it in a
   password manager. Create two SecureString, Standard, `alias/aws/ssm`
   parameters by pasting the values into the console:
   `/on-this-day/prod/db-admin-password` (Supabase admin password) and
   `/on-this-day/prod/db-password` (runtime password).

## C. Database

8. **Migrate and import** through the session pooler (port 5432), events
   before quizzes:

   ```sh
   eval "$(aws configure export-credentials --profile on-this-day --format env)"
   export DB_JDBC_URL="jdbc:postgresql://$POOLER_HOST:5432/postgres"
   export DB_USER="postgres.$PROJECT_REF"
   export DB_PASSWORD_SSM_PARAMETER=/on-this-day/prod/db-admin-password
   export DB_SSL_ROOT_CERT="$CA"
   mvn -B -q compile exec:java -Dexec.mainClass=com.onthisday.platform.runtime.DatabaseMigrationCommand -Dexec.args=migrate
   mvn -B -q exec:java -Dexec.mainClass=com.onthisday.ingestion.CuratedContentImportCommand
   mvn -B -q exec:java -Dexec.mainClass=com.onthisday.ingestion.quiz.QuizContentImportCommand
   mvn -B -q exec:java -Dexec.mainClass=com.onthisday.ingestion.editorial.ContentStatusCommand \
     -Dexec.args="content content/quizzes editorial/batches build/content-status.json"
   jq '.database.inSync' build/content-status.json   # true
   ```

9. **Runtime role and Data API grants.** `infra/supabase/runtime-role.sql`
   removes Supabase's API-role grants and creates `otd_runtime` with read
   access plus the few writes the functions make. Then set its password to
   the runtime value:

   ```sh
   psql_admin -v ON_ERROR_STOP=1 -f infra/supabase/runtime-role.sql
   psql_admin -c '\password otd_runtime'
   ```

10. **Check the runtime login on the Lambda port** (transaction pooler, 6543;
    prints the event count):

    ```sh
    docker run --rm -it -v "$(dirname "$CA"):/c:ro" postgres:16-alpine psql \
      "host=$POOLER_HOST port=6543 dbname=postgres user=otd_runtime.$PROJECT_REF sslmode=verify-full sslrootcert=/c/$(basename "$CA")" \
      -c 'select count(*) from historical_event;'
    ```

## D. Deploy

11. **Build the Lambda ZIP** (after the certificate PR is merged and pulled).
    One ZIP serves both functions:

    ```sh
    mvn -B -q -Plambda-zip clean package -DskipTests
    mkdir -p ~/Documents/on-this-day/lambda
    cp target/on-this-day-lambda.zip ~/Documents/on-this-day/lambda/
    ```

12. **`infra/prod/terraform.tfvars`:** start from the existing file (or the
    backup in `~/Documents/on-this-day/terraform-state-backup/`) and add the
    values shown in `terraform.tfvars.example`. Keep
    `firebase_credentials_version = 1`, or Terraform would delete the Firebase
    key parameter. The schedule stays disabled and dry-run by default. With
    `supabase_project_ref` set, the functions' JDBC URL (transaction pooler)
    and user (`otd_runtime.<ref>`) are derived; no `db_jdbc_url` is needed.
13. **Plan.** The Supabase token is read into an ephemeral variable, so it
    never reaches state, the plan file or shell history. Keep this terminal
    for the apply; every Supabase-managing plan or apply needs it.

    ```sh
    read -rs TF_VAR_supabase_access_token && export TF_VAR_supabase_access_token
    terraform -chdir=infra/prod init
    terraform -chdir=infra/prod validate
    terraform -chdir=infra/prod plan -out=golive.tfplan
    ```

    Expect one import (the Supabase project), additions only (Supabase SSL
    setting, API, notification function and schedule, alerts topic and
    budget) and nothing to destroy. The imported project may show one
    in-place update that only clears an unset attribute. If media, the
    Firebase parameter or anything else would change or be destroyed, stop.
14. **Apply** and read the outputs (CloudFront takes several minutes):

    ```sh
    terraform -chdir=infra/prod apply golive.tfplan
    terraform -chdir=infra/prod output
    ```

15. **Alerts and push dry run:** confirm the SNS subscription email, then:

    ```sh
    aws lambda invoke --function-name on-this-day-notifications --cli-binary-format raw-in-base64-out \
      --payload '{"dryRun":true}' /tmp/notif.json && cat /tmp/notif.json
    ```

## E. Edge Protection (console, root)

16. Open the `api_distribution_id` distribution in CloudFront and subscribe it
    to the **Free** plan (the second of three; images use the first).
17. In that plan's WAF protection pack, add a custom rate-based rule: source
    IP, 300 requests per 5 minutes, all requests, Block.

## F. Smoke Test

18. Through CloudFront. POST and DELETE need `x-amz-content-sha256`, the hex
    SHA-256 of the body (empty body for DELETE):

    ```sh
    API=$(terraform -chdir=infra/prod output -raw api_base_url)
    EMPTY=e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
    curl -i "$API/v1/health"
    curl -i "$API/v1/days/today?timezone=America/Jamaica"
    curl -s -o /dev/null -D - "$API/v1/days/today?timezone=America/Jamaica" | grep -i x-cache   # Hit from cloudfront
    curl -i "$API/v1/events/battle-of-bosworth-field-1485"
    curl -i "$API/v1/quizzes/catalog"
    curl -i "$API/v1/quizzes/daily?timezone=America/Jamaica&questionCount=5"
    B='{"questionCount":5}'
    curl -i -X POST "$API/v1/quizzes/quick-play" -H 'content-type: application/json' \
      -H "x-amz-content-sha256: $(printf '%s' "$B" | shasum -a 256 | cut -d' ' -f1)" --data-raw "$B"
    D='{"token":"smoke-test-delete-me","platform":"ios","timezone":"America/Jamaica","notificationPermissionStatus":"authorized"}'
    curl -i -X POST "$API/v1/devices" -H 'content-type: application/json' \
      -H "x-amz-content-sha256: $(printf '%s' "$D" | shasum -a 256 | cut -d' ' -f1)" --data-raw "$D"
    curl -i -X DELETE "$API/v1/devices/smoke-test-delete-me" -H "x-amz-content-sha256: $EMPTY"
    curl -i -X POST "$API/v1/devices" -H 'content-type: application/json' --data-raw "$D"   # 403: no body hash
    curl -i "$(aws lambda get-function-url-config --function-name on-this-day-api --query FunctionUrl --output text)v1/health"   # 403
    ```

    On a 5xx: `aws logs tail /aws/lambda/on-this-day-api --since 10m`.
    `permission denied for table` means a grant is missing from step 9;
    `password authentication failed` means the SSM value and `\password`
    differ; a `verify-full` message means the ZIP lacks the certificate.

19. Give `api_base_url` to the mobile release build
    (`ON_THIS_DAY_API_BASE_URL`), together with the body-hash header change in
    `docs/API_SECURITY.md`. Until that app change ships, Quick Play and device
    registration from the app get 403; reads work.

Turning push on (`notifications_dry_run = false`,
`notifications_schedule_enabled = true`) is a separate step after a
real-device send; see `docs/NOTIFICATIONS.md`.

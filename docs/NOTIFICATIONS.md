# Scheduled Daily Notifications

**Status (2026-10-01):** implemented and tested locally. The SSM key parameter
may be created; the function, schedule and alarm are **not deployed**. A real
send to a phone waits on the Apple Developer account (iOS) or an Android test
device.

## What It Does

Each opted-in device gets one notification per local date about that date's
featured event. It is **scheduled for 10:00 a.m. device-local time**, not
guaranteed at 10:00: the function runs every 15 minutes, and a device whose
local time is from 10:00 up to (not including) 12:00 is due. A device normally
receives it on the first run at or after 10:00; a late run, an outage or a
retryable Firebase failure can move delivery anywhere up to 12:00. After 12:00
that local date is skipped for the device.

Tapping it opens the featured event (`data.eventId`), which the mobile app
already handles. Content is resolved by each device's own local date, so a
device in UTC+14 gets the next day's event before devices in the Americas.

## Code Map

| Piece | Location |
| --- | --- |
| Schedule and window (10:00, 12:00, 15-minute claim lease, 30-day retention) | `notifications/NotificationSchedule` |
| Run logic | `notifications/ScheduledNotificationService` |
| Delivery records and claims | `notifications/JdbcNotificationDeliveryRepository`, migration `V5__create_notification_delivery.sql` |
| IANA timezone check | `notifications/IanaTimezones` (registration and every run) |
| Lambda handler | `platform/notifications/scheduled/ScheduledNotificationHandler` |
| Firebase key loader (SSM or local file) | `platform/notifications/scheduled/FirebaseCredentialsLoader` |
| Terminal run | `platform/notifications/scheduled/ScheduledNotificationRunCommand` |
| Infrastructure | `infra/prod/notifications.tf`, `infra/prod/alerts.tf` |

The older `ManualNotificationSenderCommand` remains a developer tool that sends
to every eligible device at once, ignoring the schedule and delivery records.

## Delivery Rules

`notification_delivery` has one row per device token and local date
(primary key), with status `claimed`, `sent` or `retryable_failure`.

1. **Claim.** Before sending, a run inserts or takes over the row in one
   statement. It succeeds only if there is no row, the last attempt was
   `retryable_failure`, or an earlier claim's lease (15 minutes) has ended.
   PostgreSQL locks the row and re-checks that condition, so two overlapping
   runs can never both claim the same device and date
   (`NotificationDeliveryRepositoryIT` races eight claimers twenty times).
2. **Send and record.** Success marks `sent`, which is final for that date.
   `UNREGISTERED`/`SENDER_ID_MISMATCH` deletes the device registration (and its
   delivery rows). Throttling and 5xx mark `retryable_failure`; the next run
   before 12:00 local retries it.
3. **Configuration failure** (bad key, wrong project, permission denied) marks
   that device retryable, defers every remaining device untouched, and fails
   the invocation so the error alarm fires.
4. **Crash after send.** If the function dies after Firebase accepted a
   message but before recording `sent`, the claim expires after 15 minutes and
   a later run before 12:00 sends again. Delivery is therefore **at least
   once** inside the window, not exactly once. The repeat carries the same
   collapse key (`daily-<local date>` as Android `collapse_key` and iOS
   `apns-collapse-id`), so the phone replaces the first notification instead
   of showing two. A crash after about 11:45 local is never resent, because
   the claim outlives the window. Tested in
   `ScheduledNotificationServiceTest`.
5. **The lease must exceed the function timeout** (15 minutes vs 5), so an
   expired claim always belongs to a run that has stopped.
6. **Bad data is isolated.** A stored timezone that is not an IANA name
   (`+05:00`, `GMT+5`, a typo) skips only that device and is counted as
   `invalidTimezones`. A date without a featured event skips only devices on
   that date (`noContent`) and records nothing, so they still get it if content
   is imported before 12:00. A database error fails the run.
7. Rows older than 30 days are pruned by each run.
8. **Large batches.** Sends are sequential. A run stops claiming new devices
   45 seconds before the function's 5-minute timeout and leaves the rest
   (`deferred`) for the next run, so a big batch finishes over several runs
   instead of timing out mid-send.

Registration now accepts only exact IANA names (`America/Jamaica`, `UTC`);
offsets are rejected with `400`. The app sends the `flutter_timezone`
identifier, which is an IANA name.

## Logs And Alerts

Every run, including one with nobody due, logs one
`scheduled_notification_run_summary` line with counts (see
`OBSERVABILITY.md`), so a successful empty run is distinguishable from a run
that never happened. Tokens, keys and request paths are never logged. The
function's log group keeps 14 days.

`alerts.tf` (only when `alert_email` is set) adds an email SNS topic, a monthly
AWS budget (default US$1) and the function's error alarm. Budget monitoring
alerts have no charge. A standard CloudWatch alarm is free only while the
account stays within the 10 free alarms; check how many exist before applying.

## Credentials

- The Firebase service-account JSON lives in the SSM SecureString
  `/on-this-day/prod/firebase-service-account`, Standard tier, encrypted with
  the AWS managed `aws/ssm` key. It is loaded at runtime, cached while the
  function stays warm, and never put in an environment variable, Git, or
  Terraform state (it is written through a write-only argument).
- **Free only if:** Standard tier, value under 4 KB (a service-account JSON is
  about 2.3 KB; Terraform rejects 4 KB or more), and no customer-managed key.
  Decrypts go through KMS and count toward its 20,000 free requests a month;
  the function makes at most one per cold start, under 3,000 a month.
- **Access:** the `aws/ssm` key does not restrict decryption to this function
  by itself. Anyone in the account allowed `ssm:GetParameter` on that name can
  read it. The function's role is limited to its two parameters, and decrypts
  only through SSM for those parameter ARNs; review other IAM users and roles
  for broad `ssm:GetParameter*` rights. A customer-managed KMS key (about
  US$1/month) is the stronger later option.
- The deployed database password is likewise read from the SecureString named
  by `DB_PASSWORD_SSM_PARAMETER` (default `/on-this-day/prod/db-password`,
  created by hand at database launch). Locally, `DB_PASSWORD` still works.
- Locally, the key comes from a gitignored file named by
  `FIREBASE_CREDENTIALS_FILE` (`firebase-service-account*.json` is ignored).
- Apple's push key (`.p8`) goes into the Firebase console, not the backend.

## Running Locally

With the local database migrated and content imported (`SETUP.md`):

```sh
export DB_JDBC_URL=jdbc:postgresql://localhost:5432/on_this_day DB_USER=on_this_day DB_PASSWORD=on_this_day
# Dry run as of 10:00 in Jamaica: who is due and what they would get.
mvn -q compile exec:java \
  -Dexec.mainClass=com.onthisday.platform.notifications.scheduled.ScheduledNotificationRunCommand \
  -Dexec.args="--instant 2026-10-01T15:00:00Z"
# Real send to registered devices (needs a Firebase key file):
FIREBASE_PROJECT_ID=<project-id> FIREBASE_CREDENTIALS_FILE=$PWD/firebase-service-account.json \
  mvn -q compile exec:java \
  -Dexec.mainClass=com.onthisday.platform.notifications.scheduled.ScheduledNotificationRunCommand \
  -Dexec.args="--send"
```

Through SAM (`sam build` first): the function is `ScheduledNotificationFunction`
in `template.yaml`, dry run by default. Give it the same `DB_*` values as
`OnThisDayApiFunction` by adding a `ScheduledNotificationFunction` block to
your env-vars file, then `sam local invoke ScheduledNotificationFunction
--env-vars <file> --event <file containing {"instant":"2026-10-01T15:00:00Z"}>`.

## Launch Steps

Each step needs the owner's go-ahead.

1. **IAM (owner, console, once).** `infra/bootstrap/main.tf` records two
   additions made by hand: the `on-this-day-terraform` policy gains the
   `ProjectParameters` and `DescribeParameters` statements, and the
   `on-this-day-workload-boundary` gains `ssm:GetParameter` and `kms:Decrypt`
   with the `ProjectParametersOnly` and `DecryptOnlyViaSsm` denies.
2. **Key parameter.** Download a service-account key in the Firebase console
   (Project settings, Service accounts), check its size (`wc -c`, must be
   under 4096), then from `infra/prod`:
   `TF_VAR_firebase_service_account_json="$(cat key.json)" terraform apply
   -var firebase_credentials_version=1`. Bump the version to rotate. Delete
   the downloaded file afterwards, or keep it only in a password manager.
3. **Real-device send.** With the Apple Developer account active, upload the
   APNs key in Firebase, enable push in the app, install a development build,
   register, then run the terminal `--send` command once and check the
   notification and its deep link.
4. **Deploy disabled.** Build the ZIP, set `notifications_lambda_zip_path`,
   `db_jdbc_url`, `db_user`, `firebase_project_id` and `alert_email`, apply,
   then invoke the function once with `{"dryRun": true}` and read its summary.
5. **Enable.** Set `notifications_dry_run = false` and
   `notifications_schedule_enabled = true` only after the real send, the budget
   and the alarm checks pass. **To stop at once:** set
   `notifications_schedule_enabled = false` and apply, or disable the schedule
   in the EventBridge Scheduler console.

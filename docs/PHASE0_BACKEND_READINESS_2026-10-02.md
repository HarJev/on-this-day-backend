| Production content database | VERIFIED IN SYNC (2026-10-03) | ContentStatusCommand returned `database.inSync=true` with no event, day or question drift: 1,028 events, 218 days, 702 published questions and one retired question. |# Backend Phase 0 Readiness (updated 2026-10-03; initial report 2026-10-02)

This is the current backend handoff for closing the mobile app's Phase 0 beta
gates. It records production checks completed on 2026-10-03 separately from
remaining owner-only device and AWS resource checks. Read it before following
the older first-deploy sequence in
[GO_LIVE.md](GO_LIVE.md).

## Current Status

| Area | Status | Evidence and boundary |
| --- | --- | --- |
| Production API | VERIFIED LIVE (2026-10-03) | Read-only Today and quiz-catalog requests returned successfully from the CloudFront API. |
| Production content database | VERIFIED IN SYNC (2026-10-03) | ContentStatusCommand returned database.inSync=true with no event, day or question drift: 1,028 events, 218 days, 702 published questions and one retired question. |
| Notification implementation | READY IN REPOSITORY | Scheduled Lambda, 15-minute EventBridge schedule, idempotent delivery records, safe summary logs, 14-day retention and an error alarm are defined in code/Terraform. |
| Deployed notification resources | UNVERIFIED | Terraform defaults to notifications_schedule_enabled = false and notifications_dry_run = true; these defaults do not prove the live values. Confirm the deployed function, schedule, environment, SSM parameter and alarms in AWS before changing anything. |
| Real daily push | PENDING | Requires a verified dry run, Firebase/APNs configuration, an opted-in physical device on each platform, and a real delivery/deep-link check. The backend repo cannot complete Apple enrollment or phone checks. |
| Cost controls | CONFIGURED IN CODE | Both Lambda log groups use 14-day retention. Notification logs contain one short summary per 15-minute invocation, about 96/day. Request logs omit tokens, query strings and bodies. No custom metrics, dashboards or access logs are required for Phase 0. |

The account's actual AWS free-tier usage and other CloudWatch alarms are not
visible from this repository. Do not promise zero cost from Terraform defaults
alone; check account usage and the budget alert in the AWS console.

## Work Completed In This Pass

- Confirmed the notification service already emits bounded run summaries and
  does not log device tokens or message bodies.
- Confirmed Terraform defines 14-day retention, an error alarm, an optional
  email budget alert, and safe notification defaults.
- Corrected stale deployment wording in GO_LIVE.md, NOTIFICATIONS.md,
  OBSERVABILITY.md and API_SECURITY.md.
- Imported canonical events first, then quizzes, through the documented AWS SSM and verified-TLS path. Production content parity passed afterward.
- The October 3-9 batch introduced no image files or URL changes; 16 existing CloudFront image URLs returned HTTP 206 to a one-byte range request. The October 5 Monty Python event intentionally has no image. No S3 upload was needed.
- No schema, Terraform, Firebase or notification resource changed in this content pass.

## Owner Steps To Close Backend Phase 0

1. Production API reads and database parity passed on 2026-10-03. Repeat the
   read-only ContentStatusCommand after future content imports; require
   database.inSync: true.
2. Inspect the deployed notification Lambda and EventBridge schedule. Confirm
   schedule is disabled or dry-run until a real test is ready; inspect the
   deployed environment without printing secret values. Confirm the SSM
   Firebase service-account parameter exists and is Standard tier, and confirm
   the Lambda error alarm and budget notification are active.
3. Invoke one dry run and inspect its summary in the function response/logs.
   Confirm it does not send. Check the one summary line and ensure no token,
   payload or secret appears in logs:

   ```sh
   aws lambda invoke --function-name on-this-day-notifications \\
     --cli-binary-format raw-in-base64-out --payload '{"dryRun":true}' \\
     /tmp/on-this-day-notifications-dry-run.json
   cat /tmp/on-this-day-notifications-dry-run.json
   ```
4. After Android is signed and available, register a dedicated test device and
   perform one controlled real send. Verify foreground, background, cold-open
   and Event Detail deep link. Do not use a send command that could target
   unintended registered devices without first checking its audience.
5. After Apple Developer enrollment, configure the production App ID,
   provisioning and APNs key in Firebase, then repeat the real-device check on
   iPhone. This is an owner-only prerequisite.
6. Enable the scheduled send only after both platform checks, production
   content coverage, budget/alarms and the owner-approved go/no-go. Use a
   reviewed Terraform plan; do not toggle the schedule manually without
   recording the resulting state.
7. Check Supabase Free project status and AWS budget periodically. The free
   database may pause after inactivity and has no automatic backups; retain
   canonical content in Git and follow the documented recovery/import process.


Read-only AWS checks (after selecting the production profile/region):

```sh
aws sts get-caller-identity
aws lambda get-function-configuration --function-name on-this-day-notifications \
  --query '{State:State,Update:LastUpdateStatus,DryRun:Environment.Variables.NOTIFICATIONS_DRY_RUN}'
aws scheduler get-schedule --name on-this-day-notifications-every-15-minutes \
  --group-name default --query '{State:State,Expression:ScheduleExpression}'
aws cloudwatch describe-alarms --alarm-name-prefix on-this-day-notifications \
  --query 'MetricAlarms[].{Name:AlarmName,State:StateValue}'
aws logs tail /aws/lambda/on-this-day-notifications --since 1h |
  rg 'scheduled_notification_run_summary|scheduled_notification_no_content'
```

These are observation commands. If the function or schedule is absent, use
the reviewed Terraform plan and deployment runbook; do not assume the schedule
is safe just because the repository defaults are safe.
## Verification In The Initial Readiness Pass (2026-10-02)

- PASS: `mvn -B test` — 271 tests in the initial readiness pass.
- PASS: `terraform fmt -check infra/prod/*.tf`.
- PASS: `git diff --check` in the initial readiness pass.
- PASS (2026-10-03): production migration validation and ContentStatusCommand; database.inSync=true with no event, day or question drift after importing the October 3-9 batch.
- PASS (2026-10-03): CloudFront API catalog and Today reads; 16 existing batch image URLs responded with HTTP 206 to a one-byte range request.
- PARTIAL: `terraform -chdir=infra/prod test` — the go-live mocked plan passed.
  The API and notification suites fail before assertions because their test
  files do not mock the Supabase provider; a fake token was rejected with 401.
  No real token was used and no infrastructure was changed. AWS STS could not
  be reached, so deployed resource checks remain unverified.
## Content Follow-up

Canonical data covers 218 dates and 1,028 events, with 702 published quiz
questions and one retired question. This is not the complete 366-day calendar.
The editorial target for featured images is 100% of the closed-beta runway.
Five upcoming featured dates lack a primary image: October 24, November 22,
December 6, December 11 and December 31. Images are editorial follow-up, not a code blocker; use the
reviewed pipeline and retain image-free layouts when no suitable licensed
image exists. Recalculate this list after any content merge.

# Backend Observability

This document covers PA7 in `implementation_plan.md` and the backend half of
A7 in the mobile launch workplan. It describes what the backend logs, what it
never logs, and how to read the logs locally. Alarms, dashboards, and log
retention settings belong to the approval-gated deployment work and are not
created here.

## Signals

All application logs are single-line `key=value` messages on stdout. SAM local
prints them in the terminal, and a deployed Lambda would send them to
CloudWatch Logs.

| Signal | Log line | Fields |
| --- | --- | --- |
| Every API request | `api_request` (INFO) | `method`, `route`, `statusCode`, `outcome`, `durationMs`, `requestId` |
| Unhandled failure | `lambda_unexpected_exception` (ERROR) | `method`, `route`, `requestId`, stack trace |
| Content gap | `today_content_unavailable`, `recent_days_unavailable`, `event_detail_unavailable` (WARN) | `reason`, plus `eventId` for event detail |
| Quiz unavailable | `quiz_catalog_unavailable`, `quick_play_quiz_unavailable`, `daily_quiz_unavailable` (WARN) | `reason` |
| Database failure | `db_query_failed` (ERROR) | `operation` and non-personal parameters such as a date or collection ID |
| Notification batch | `daily_notification_send_complete` (INFO) | attempted count and success, permanent, transient, and configuration failure counts |
| Cold start | `lambda_handler_init_start` / `lambda_handler_init_end` (INFO) | `durationMs` |

`route` is the registered route pattern, such as `/v1/devices/{token}` or
`/v1/events/{eventId}`, never the raw request path. A path that matches no
route is logged as `unmatched`, so text a caller puts in a URL never reaches the
logs.

`outcome` is one of:

- `ok`: status below 400.
- `client_error`: 4xx, such as an invalid timezone or an unknown route.
- `unavailable`: 503, the documented status for missing curated content or an
  unavailable quiz.
- `server_error`: any other 5xx.

Routine database timings are logged at DEBUG and are off by default.

## Never Logged

- Device notification tokens, including the token in `DELETE /v1/devices/{token}`.
- Query strings. Timezones and quiz counts are not personal, but query strings
  are left out as a rule.
- Request and response bodies, including quiz answers and device registration
  payloads.
- Firebase credentials, database passwords, or any secret value.
- Account or advertising identifiers; the backend has neither.

Image-origin failures happen on the device when the app fetches CloudFront
images, so the backend does not see them. Measuring them belongs to the mobile
telemetry decision in A7.

## Reading Logs Locally

With SAM local running as described in `SETUP.md`, request lines appear in the
same terminal. To follow only request outcomes:

```sh
sam local start-api ... 2>&1 | grep -E "api_request|_unavailable|db_query_failed|lambda_unexpected_exception"
```

Useful checks:

- Slow requests: `api_request` lines with a large `durationMs`.
- Content gaps: `outcome=unavailable` together with a `today_content_unavailable`
  line naming the date.
- Failures: any `outcome=server_error`; an unhandled one also has a `lambda_unexpected_exception` line with the same `requestId`.

## Deployment Notes (Not Yet Applied)

The project targets $0 infrastructure spend. When deployment is approved:

- Set the Lambda log group retention explicitly (for example 14 days) so stored
  logs stay small and within the free allowance.
- Read logs with `aws logs tail` or the console log viewer. Logs Insights
  queries, metric filters, and custom metrics can each add charges, so leave
  them out unless the owner approves a cost.
- Decide on alarms separately, once it is clear they can run on free
  allowances.

# API Security

Decision (owner, 2026-10-01): serve the API from a free Lambda **function URL**
instead of API Gateway. This document records how that URL is protected.
Terraform: `infra/prod/api.tf`. Nothing here is applied or deployed yet.

## What Is Exposed

The app has no user accounts, so every route is anonymous by design.

| Route | Kind | Notes |
| --- | --- | --- |
| `GET /v1/health` | read | no database |
| `GET /v1/days/today`, `/v1/days/recent` | read | same for every caller of a timezone |
| `GET /v1/events/{eventId}` | read | public content |
| `GET /v1/quizzes/catalog`, `/v1/quizzes/daily` | read | public content |
| `POST /v1/quizzes/quick-play` | read (POST body) | random selection; not cacheable |
| `POST /v1/devices` | write | upserts a push token |
| `DELETE /v1/devices/{token}` | write | deletes by token; knowing a token is the only proof |

Nothing returns personal data. The risks are cost (a flood of invocations),
database load on the Supabase free plan, and junk device rows.

## Options Considered

| Option | Verdict |
| --- | --- |
| `AuthType NONE` + checks in the function | Every request, including a flood, runs and bills Lambda (free up to 1M requests a month, then paid). No throttle. Rejected on its own. |
| Shared key baked into the app | Anyone can extract it from the app binary. Adds no real protection over OAC below. Rejected. |
| `AWS_IAM` with app-signed requests (Cognito guest credentials) | Guest credentials are free and available to anyone, so it does not stop abuse; adds SDK weight and latency. Rejected. |
| **CloudFront + origin access control (OAC) + `AWS_IAM`** | Chosen. See below. $0 on the CloudFront Free flat-rate plan. |
| Firebase App Check | Free, proves requests come from the genuine app. Needs the paid Apple Developer account (App Attest) and app work. Deferred to public launch, mainly for device registration. |

## Chosen Design

- **Function URL locked to CloudFront.** `authorization_type = "AWS_IAM"`, and
  the resource policy allows only `cloudfront.amazonaws.com` with the API
  distribution's ARN as source (both `lambda:InvokeFunctionUrl` and
  `lambda:InvokeFunction`). CloudFront signs each origin request with SigV4.
  A direct call to the function URL gets 403 from Lambda without running the
  function, so it is not billed.
- **A dedicated distribution on the Free flat-rate plan** ($0/month, 1M
  requests, 100 GB). Flat-rate plans never charge overages, even under
  attack, and requests blocked by WAF do not count against the allowance. The
  account may hold three Free plans; the image distribution uses one. A
  separate distribution keeps the API's allowance and WAF rules apart from
  image traffic.
- **Per-IP rate limit in the plan's WAF web ACL** replaces the API Gateway
  stage throttle. Added by the owner in the console (Terraform ignores
  `web_acl_id`, as for images).
- **Edge caching for content reads.** The function adds
  `Cache-Control: public, max-age=60` to successful GETs of today, recent days,
  event detail, quiz catalog and Daily Challenge. The cache key holds only the
  query parameters the API reads (`timezone`, `days`, `questionCount`), so
  junk parameters cannot bypass it. Writes, Quick Play, health and errors are
  never given a max-age. Today and Daily may be up to a minute stale after
  local midnight.
- **Input limits in the function.** Request bodies over 16 KB get 413 before
  parsing. Push tokens over 1,024 characters are rejected (real tokens are
  64 to about 200). Existing validation covers timezones, `days` (max 14) and
  quiz parameters.
- **Reserved concurrency** (`api_reserved_concurrency`) is a hard ceiling on
  parallel executions and so on database connections. New accounts have a
  quota of 10 and cannot reserve any, so it is unset by default; set 5 once the
  quota is raised. Setting it to 0 by hand is an emergency off switch.
- **Budget alert** (`alerts.tf`, $1) and the API error alarm stay as before.

## Mobile Change Needed (not made here)

`lib/core/api/api_client.dart` in the mobile repo:

1. Use the `api_base_url` Terraform output (the `https://<id>.cloudfront.net`
   address) as `ON_THIS_DAY_API_BASE_URL` for release builds. Never ship the
   raw function URL; it refuses unsigned calls.
2. On every `POST` and `DELETE`, send `x-amz-content-sha256` set to the
   lowercase hex SHA-256 of the exact request body bytes (the empty string
   for `DELETE`). Lambda rejects OAC-signed writes without it. The `crypto`
   package is already a dependency. Encode the body once and hash and send
   those same bytes.

Local SAM keeps working without the header, so the change is safe to ship
before the cloud API exists.

## Owner Steps At Deploy

1. Apply `infra/prod` with `api_lambda_zip_path`, `db_jdbc_url` and `db_user`
   set (see `infra/prod/README.md`).
2. In the CloudFront console, open the distribution from the
   `api_distribution_id` output and subscribe it to the **Free** plan. Never
   pick a paid tier.
3. In that plan's web ACL, add a rate-based rule: block a source IP above 300
   requests in 5 minutes (about one a second, far above one app user's
   traffic). Start it in Count mode for a day if unsure.
4. Smoke-test: the CloudFront `GET /v1/health` returns 200; the raw function
   URL returns 403; a `POST /v1/devices` from a build with the header change
   returns 200.

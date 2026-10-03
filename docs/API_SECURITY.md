# API Security

Decision (owner, 2026-10-01): serve the API from a free Lambda **function URL**
instead of API Gateway. The API was read-checked on 2026-10-03; verify remaining
AWS resource state through `docs/PHASE0_BACKEND_READINESS_2026-10-02.md`. Terraform:
`infra/prod/api.tf`.

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
- **No edge caching yet.** The distribution uses the managed `CachingDisabled`
  policy. The managed `UseOriginCacheControlHeaders-QueryStrings` policy keys
  on the `Host` header, and CloudFront forwards cache-key headers to the
  origin, so the viewer's Host reached the function URL and every request got
  403 (found on the first live deploy). The function still sends
  `Cache-Control: public, max-age=60` on content GETs, ready for a cache policy
  that omits `Host`. Until then every request invokes the function; at beta
  volume that is inside the Lambda free tier, and the WAF per-IP rate rule
  bounds abuse.
- **Input limits in the function.** Request bodies over 16 KB get 413 before
  parsing. Push tokens over 1,024 characters are rejected (real tokens are
  64 to about 200). Existing validation covers timezones, `days` (max 14) and
  quiz parameters.
- **Reserved concurrency** (`api_reserved_concurrency`) is a hard ceiling on
  parallel executions and so on database connections. New accounts have a
  quota of 10 and cannot reserve any, so it is unset by default; set 5 once the
  quota is raised. Setting it to 0 by hand is an emergency off switch.
- **Budget alert** (`alerts.tf`, $1) and the API error alarm stay as before.

## Mobile Contract Status

The mobile client now uses the CloudFront API URL for release builds and keeps
the local SAM override for development. POST and DELETE requests include
`x-amz-content-sha256`, calculated over the exact transmitted body bytes. The
live API contract was smoke-tested previously; recheck only if the API origin,
edge configuration or request encoding changes.

## Deployment Bootstrap (Historical)

The following steps describe the first deployment and are retained as a
reference. The API is already live. Do not re-apply
these bootstrap steps blindly. For current status and Phase 0 checks, use
`docs/PHASE0_BACKEND_READINESS_2026-10-02.md`; verify actual AWS resources and
review a Terraform plan before any change.

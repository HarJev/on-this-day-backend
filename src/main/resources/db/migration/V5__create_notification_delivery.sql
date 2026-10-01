-- One row per device and recipient-local date for the scheduled daily
-- notification. The primary key makes a confirmed send for a date final, and
-- the claim columns let overlapping runs coordinate without sending twice.
-- Rows disappear with their device registration, and old rows are pruned by
-- the job itself.
CREATE TABLE notification_delivery (
  token TEXT NOT NULL
    REFERENCES device_registration(token) ON DELETE CASCADE,
  local_date DATE NOT NULL,
  status TEXT NOT NULL,
  event_id TEXT NOT NULL,
  attempt_count INTEGER NOT NULL DEFAULT 1,
  claimed_until TIMESTAMPTZ,
  last_error_code TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (token, local_date),
  CONSTRAINT notification_delivery_status_check
    CHECK (status IN ('claimed', 'sent', 'retryable_failure')),
  CONSTRAINT notification_delivery_claim_has_lease
    CHECK (status <> 'claimed' OR claimed_until IS NOT NULL),
  CONSTRAINT notification_delivery_attempt_count_positive CHECK (attempt_count > 0)
);

CREATE INDEX notification_delivery_by_local_date ON notification_delivery(local_date);

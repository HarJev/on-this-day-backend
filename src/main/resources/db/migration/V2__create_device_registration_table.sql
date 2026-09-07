CREATE TABLE device_registration (
  token TEXT PRIMARY KEY,
  platform TEXT NOT NULL,
  timezone TEXT NOT NULL,
  notification_permission_status TEXT NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_registered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT device_registration_token_not_blank CHECK (btrim(token) <> ''),
  CONSTRAINT device_registration_platform_check CHECK (platform IN ('ios', 'android')),
  CONSTRAINT device_registration_timezone_not_blank CHECK (btrim(timezone) <> ''),
  CONSTRAINT device_registration_notification_permission_status_check
    CHECK (notification_permission_status IN ('authorized', 'provisional', 'denied', 'not_determined'))
);

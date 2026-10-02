-- Run once against the production Supabase database as the admin (postgres)
-- user, after DatabaseMigrationCommand and both content imports. Safe to re-run.
-- Then set the password with `\password otd_runtime`, entering the value stored
-- in the SSM parameter /on-this-day/prod/db-password. See docs/GO_LIVE.md.

-- The app never uses Supabase's generated Data API; remove the default
-- grants Supabase gives its API roles on tables created in public.
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM anon, authenticated;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM anon, authenticated;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA public FROM anon, authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public REVOKE ALL ON TABLES FROM anon, authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public REVOKE ALL ON SEQUENCES FROM anon, authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public REVOKE ALL ON FUNCTIONS FROM anon, authenticated;

-- Runtime login for the API and notification functions: reads everything,
-- writes only device registrations, notification deliveries and Daily
-- Challenge assignments.
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'otd_runtime') THEN
    CREATE ROLE otd_runtime LOGIN;
  END IF;
END
$$;

GRANT USAGE ON SCHEMA public TO otd_runtime;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO otd_runtime;
REVOKE ALL ON flyway_schema_history FROM otd_runtime;
GRANT INSERT, UPDATE, DELETE ON device_registration, notification_delivery TO otd_runtime;
GRANT INSERT, UPDATE ON quiz_daily_challenge TO otd_runtime;
GRANT INSERT ON quiz_daily_question TO otd_runtime;
-- Tables added by later migrations become readable without another grant.
-- Writes are not: a new table the API or notification function writes to
-- needs its own INSERT/UPDATE/DELETE grant here, applied with the migration.
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT SELECT ON TABLES TO otd_runtime;

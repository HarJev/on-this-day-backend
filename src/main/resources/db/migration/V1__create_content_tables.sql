CREATE TABLE historical_event (
  event_id TEXT PRIMARY KEY,
  title TEXT NOT NULL,
  year_label TEXT NOT NULL,
  historical_date TEXT NOT NULL,
  date_note TEXT,
  summary TEXT NOT NULL,
  description TEXT NOT NULL,
  notification_title TEXT,
  notification_body TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT historical_event_event_id_not_blank CHECK (length(btrim(event_id)) > 0),
  CONSTRAINT historical_event_title_not_blank CHECK (length(btrim(title)) > 0),
  CONSTRAINT historical_event_year_label_not_blank CHECK (length(btrim(year_label)) > 0),
  CONSTRAINT historical_event_historical_date_not_blank CHECK (length(btrim(historical_date)) > 0),
  CONSTRAINT historical_event_date_note_not_blank CHECK (date_note IS NULL OR length(btrim(date_note)) > 0),
  CONSTRAINT historical_event_summary_not_blank CHECK (length(btrim(summary)) > 0),
  CONSTRAINT historical_event_description_not_blank CHECK (length(btrim(description)) > 0),
  CONSTRAINT historical_event_notification_title_not_blank CHECK (
    notification_title IS NULL OR length(btrim(notification_title)) > 0
  ),
  CONSTRAINT historical_event_notification_body_not_blank CHECK (
    notification_body IS NULL OR length(btrim(notification_body)) > 0
  )
);

CREATE TABLE daily_event (
  month SMALLINT NOT NULL,
  day SMALLINT NOT NULL,
  event_id TEXT NOT NULL REFERENCES historical_event(event_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  role TEXT NOT NULL,
  display_order SMALLINT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (month, day, event_id),
  CONSTRAINT daily_event_valid_month_day CHECK (
    month BETWEEN 1 AND 12
    AND day BETWEEN 1 AND CASE
      WHEN month IN (1, 3, 5, 7, 8, 10, 12) THEN 31
      WHEN month IN (4, 6, 9, 11) THEN 30
      WHEN month = 2 THEN 29
    END
  ),
  CONSTRAINT daily_event_role_supported CHECK (role IN ('featured', 'additional')),
  CONSTRAINT daily_event_display_order_positive CHECK (display_order > 0),
  CONSTRAINT daily_event_featured_order_is_one CHECK (role <> 'featured' OR display_order = 1)
);

CREATE UNIQUE INDEX daily_event_one_featured_per_day
  ON daily_event(month, day)
  WHERE role = 'featured';

CREATE UNIQUE INDEX daily_event_one_item_per_day_order
  ON daily_event(month, day, display_order);

CREATE TABLE event_source (
  source_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_id TEXT NOT NULL REFERENCES historical_event(event_id) ON UPDATE CASCADE ON DELETE CASCADE,
  display_order SMALLINT NOT NULL,
  display_name TEXT NOT NULL,
  url TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT event_source_display_order_positive CHECK (display_order > 0),
  CONSTRAINT event_source_display_name_not_blank CHECK (length(btrim(display_name)) > 0),
  CONSTRAINT event_source_url_https CHECK (url ~ '^https://.+')
);

CREATE UNIQUE INDEX event_source_one_item_per_event_order
  ON event_source(event_id, display_order);

CREATE TABLE event_image (
  image_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_id TEXT NOT NULL REFERENCES historical_event(event_id) ON UPDATE CASCADE ON DELETE CASCADE,
  display_order SMALLINT NOT NULL,
  is_primary BOOLEAN NOT NULL DEFAULT false,
  url TEXT NOT NULL,
  alt_text TEXT NOT NULL,
  source_name TEXT,
  source_url TEXT,
  creator TEXT,
  attribution TEXT,
  license_name TEXT,
  license_url TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT event_image_display_order_positive CHECK (display_order > 0),
  CONSTRAINT event_image_url_https CHECK (url ~ '^https://.+'),
  CONSTRAINT event_image_alt_text_not_blank CHECK (length(btrim(alt_text)) > 0),
  CONSTRAINT event_image_source_name_not_blank CHECK (
    source_name IS NULL OR length(btrim(source_name)) > 0
  ),
  CONSTRAINT event_image_source_url_https CHECK (
    source_url IS NULL OR source_url ~ '^https://.+'
  ),
  CONSTRAINT event_image_creator_not_blank CHECK (
    creator IS NULL OR length(btrim(creator)) > 0
  ),
  CONSTRAINT event_image_attribution_not_blank CHECK (
    attribution IS NULL OR length(btrim(attribution)) > 0
  ),
  CONSTRAINT event_image_license_name_not_blank CHECK (
    license_name IS NULL OR length(btrim(license_name)) > 0
  ),
  CONSTRAINT event_image_license_url_https CHECK (
    license_url IS NULL OR license_url ~ '^https://.+'
  )
);

CREATE UNIQUE INDEX event_image_one_item_per_event_order
  ON event_image(event_id, display_order);

CREATE UNIQUE INDEX event_image_one_primary_per_event
  ON event_image(event_id)
  WHERE is_primary;

CREATE FUNCTION assert_event_has_source()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  candidate_event_id TEXT;
  candidate_event_ids TEXT[];
BEGIN
  IF TG_TABLE_NAME = 'historical_event' THEN
    candidate_event_ids := ARRAY[NEW.event_id];
  ELSIF TG_TABLE_NAME = 'event_source' THEN
    IF TG_OP = 'INSERT' THEN
      candidate_event_ids := ARRAY[NEW.event_id];
    ELSIF TG_OP = 'UPDATE' THEN
      candidate_event_ids := ARRAY[OLD.event_id, NEW.event_id];
    ELSIF TG_OP = 'DELETE' THEN
      candidate_event_ids := ARRAY[OLD.event_id];
    END IF;
  ELSE
    candidate_event_ids := ARRAY[]::TEXT[];
  END IF;

  FOREACH candidate_event_id IN ARRAY candidate_event_ids LOOP
    IF candidate_event_id IS NULL THEN
      CONTINUE;
    END IF;

    IF EXISTS (SELECT 1 FROM historical_event WHERE event_id = candidate_event_id)
       AND NOT EXISTS (SELECT 1 FROM event_source WHERE event_id = candidate_event_id) THEN
      RAISE EXCEPTION 'historical_event % must have at least one source', candidate_event_id
        USING ERRCODE = '23514';
    END IF;
  END LOOP;

  RETURN NULL;
END;
$$;

CREATE CONSTRAINT TRIGGER historical_event_must_have_source
AFTER INSERT OR UPDATE OF event_id ON historical_event
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION assert_event_has_source();

CREATE CONSTRAINT TRIGGER event_source_preserves_required_source
AFTER INSERT OR UPDATE OF event_id OR DELETE ON event_source
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION assert_event_has_source();

CREATE FUNCTION assert_featured_event_has_notification_copy()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  IF TG_TABLE_NAME = 'daily_event'
     AND NEW.role = 'featured'
     AND EXISTS (
       SELECT 1
       FROM historical_event
       WHERE event_id = NEW.event_id
         AND notification_title IS NOT NULL
         AND notification_body IS NOT NULL
     ) THEN
    RETURN NULL;
  END IF;

  IF TG_TABLE_NAME = 'daily_event'
     AND NEW.role = 'featured' THEN
    RAISE EXCEPTION 'featured daily_event %/% % must have notification title and body',
      NEW.month, NEW.day, NEW.event_id
      USING ERRCODE = '23514';
  END IF;

  IF TG_TABLE_NAME = 'historical_event'
     AND EXISTS (
       SELECT 1
       FROM daily_event
       WHERE event_id = NEW.event_id
         AND role = 'featured'
     )
     AND (NEW.notification_title IS NULL OR NEW.notification_body IS NULL) THEN
    RAISE EXCEPTION 'featured historical_event % must have notification title and body', NEW.event_id
      USING ERRCODE = '23514';
  END IF;

  RETURN NULL;
END;
$$;

CREATE CONSTRAINT TRIGGER daily_featured_event_requires_notification_copy
AFTER INSERT OR UPDATE OF role, event_id ON daily_event
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION assert_featured_event_has_notification_copy();

CREATE CONSTRAINT TRIGGER historical_event_featured_copy_cannot_be_removed
AFTER INSERT OR UPDATE OF notification_title, notification_body ON historical_event
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION assert_featured_event_has_notification_copy();

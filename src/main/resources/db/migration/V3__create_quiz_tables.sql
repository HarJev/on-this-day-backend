CREATE TABLE quiz_question (
  question_id TEXT PRIMARY KEY,
  question_type TEXT NOT NULL,
  difficulty TEXT NOT NULL,
  publication_state TEXT NOT NULL DEFAULT 'draft',
  prompt TEXT NOT NULL,
  explanation TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT quiz_question_id_slug CHECK (question_id ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
  CONSTRAINT quiz_question_type_supported CHECK (
    question_type IN (
      'multiple_choice',
      'true_false',
      'image_identification',
      'chronological_ordering'
    )
  ),
  CONSTRAINT quiz_question_difficulty_supported CHECK (
    difficulty IN ('easy', 'medium', 'hard')
  ),
  CONSTRAINT quiz_question_publication_state_supported CHECK (
    publication_state IN ('draft', 'published', 'retired')
  ),
  CONSTRAINT quiz_question_prompt_not_blank CHECK (length(btrim(prompt)) > 0),
  CONSTRAINT quiz_question_explanation_not_blank CHECK (length(btrim(explanation)) > 0)
);

CREATE INDEX quiz_question_published_selection
  ON quiz_question(question_type, difficulty, question_id)
  WHERE publication_state = 'published';

CREATE TABLE quiz_option (
  question_id TEXT NOT NULL
    REFERENCES quiz_question(question_id) ON UPDATE CASCADE ON DELETE CASCADE,
  option_id TEXT NOT NULL,
  display_order SMALLINT NOT NULL,
  option_text TEXT NOT NULL,
  is_correct BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (question_id, option_id),
  CONSTRAINT quiz_option_id_slug CHECK (option_id ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
  CONSTRAINT quiz_option_display_order_range CHECK (display_order BETWEEN 1 AND 4),
  CONSTRAINT quiz_option_text_not_blank CHECK (length(btrim(option_text)) > 0),
  CONSTRAINT quiz_option_question_order_unique UNIQUE (question_id, display_order)
);

CREATE TABLE quiz_ordering_item (
  question_id TEXT NOT NULL
    REFERENCES quiz_question(question_id) ON UPDATE CASCADE ON DELETE CASCADE,
  item_id TEXT NOT NULL,
  item_text TEXT NOT NULL,
  correct_position SMALLINT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (question_id, item_id),
  CONSTRAINT quiz_ordering_item_id_slug CHECK (item_id ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
  CONSTRAINT quiz_ordering_item_text_not_blank CHECK (length(btrim(item_text)) > 0),
  CONSTRAINT quiz_ordering_item_position_range CHECK (correct_position BETWEEN 1 AND 4),
  CONSTRAINT quiz_ordering_item_position_unique UNIQUE (question_id, correct_position)
);

CREATE TABLE quiz_source (
  source_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  question_id TEXT NOT NULL
    REFERENCES quiz_question(question_id) ON UPDATE CASCADE ON DELETE CASCADE,
  display_order SMALLINT NOT NULL,
  display_name TEXT NOT NULL,
  url TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT quiz_source_display_order_positive CHECK (display_order > 0),
  CONSTRAINT quiz_source_display_name_not_blank CHECK (length(btrim(display_name)) > 0),
  CONSTRAINT quiz_source_url_https CHECK (url ~ '^https://.+'),
  CONSTRAINT quiz_source_question_order_unique UNIQUE (question_id, display_order)
);

CREATE TABLE quiz_image (
  question_id TEXT PRIMARY KEY
    REFERENCES quiz_question(question_id) ON UPDATE CASCADE ON DELETE CASCADE,
  url TEXT NOT NULL,
  alt_text TEXT NOT NULL,
  source_name TEXT NOT NULL,
  source_url TEXT NOT NULL,
  attribution TEXT NOT NULL,
  creator TEXT,
  license_name TEXT NOT NULL,
  license_url TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT quiz_image_url_https CHECK (url ~ '^https://.+'),
  CONSTRAINT quiz_image_alt_text_not_blank CHECK (length(btrim(alt_text)) > 0),
  CONSTRAINT quiz_image_source_name_not_blank CHECK (length(btrim(source_name)) > 0),
  CONSTRAINT quiz_image_source_url_https CHECK (source_url ~ '^https://.+'),
  CONSTRAINT quiz_image_attribution_not_blank CHECK (length(btrim(attribution)) > 0),
  CONSTRAINT quiz_image_creator_not_blank CHECK (
    creator IS NULL OR length(btrim(creator)) > 0
  ),
  CONSTRAINT quiz_image_license_name_not_blank CHECK (length(btrim(license_name)) > 0),
  CONSTRAINT quiz_image_license_url_https CHECK (license_url ~ '^https://.+')
);

CREATE TABLE quiz_collection (
  collection_id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  collection_group TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT quiz_collection_id_slug CHECK (collection_id ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
  CONSTRAINT quiz_collection_name_not_blank CHECK (length(btrim(name)) > 0),
  CONSTRAINT quiz_collection_group_supported CHECK (
    collection_group IN (
      'topic',
      'historical_period',
      'civilization',
      'conflict_or_movement'
    )
  )
);

CREATE INDEX quiz_collection_catalog_order
  ON quiz_collection(collection_group, name, collection_id);

CREATE TABLE quiz_question_collection (
  question_id TEXT NOT NULL
    REFERENCES quiz_question(question_id) ON UPDATE CASCADE ON DELETE CASCADE,
  collection_id TEXT NOT NULL
    REFERENCES quiz_collection(collection_id) ON UPDATE CASCADE ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (question_id, collection_id)
);

CREATE INDEX quiz_question_collection_by_collection
  ON quiz_question_collection(collection_id, question_id);

CREATE TABLE quiz_daily_challenge (
  challenge_date DATE PRIMARY KEY,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  completed_at TIMESTAMPTZ,
  CONSTRAINT quiz_daily_challenge_completion_order CHECK (
    completed_at IS NULL OR completed_at >= created_at
  )
);

CREATE TABLE quiz_daily_question (
  challenge_date DATE NOT NULL
    REFERENCES quiz_daily_challenge(challenge_date) ON DELETE CASCADE,
  position SMALLINT NOT NULL,
  question_id TEXT NOT NULL
    REFERENCES quiz_question(question_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (challenge_date, position),
  CONSTRAINT quiz_daily_question_position_range CHECK (position BETWEEN 1 AND 20),
  CONSTRAINT quiz_daily_question_question_unique UNIQUE (challenge_date, question_id)
);

CREATE INDEX quiz_daily_question_by_question
  ON quiz_daily_question(question_id);

CREATE FUNCTION assert_quiz_question_has_source()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  candidate_question_id TEXT;
  candidate_question_ids TEXT[];
BEGIN
  IF TG_TABLE_NAME = 'quiz_question' THEN
    IF TG_OP = 'INSERT' THEN
      candidate_question_ids := ARRAY[NEW.question_id];
    ELSE
      candidate_question_ids := ARRAY[OLD.question_id, NEW.question_id];
    END IF;
  ELSIF TG_TABLE_NAME = 'quiz_source' THEN
    IF TG_OP = 'INSERT' THEN
      candidate_question_ids := ARRAY[NEW.question_id];
    ELSIF TG_OP = 'UPDATE' THEN
      candidate_question_ids := ARRAY[OLD.question_id, NEW.question_id];
    ELSE
      candidate_question_ids := ARRAY[OLD.question_id];
    END IF;
  ELSE
    candidate_question_ids := ARRAY[]::TEXT[];
  END IF;

  FOREACH candidate_question_id IN ARRAY candidate_question_ids LOOP
    IF candidate_question_id IS NULL THEN
      CONTINUE;
    END IF;

    IF EXISTS (
      SELECT 1 FROM quiz_question WHERE question_id = candidate_question_id
    )
    AND NOT EXISTS (
      SELECT 1 FROM quiz_source WHERE question_id = candidate_question_id
    ) THEN
      RAISE EXCEPTION 'quiz_question % must have at least one source', candidate_question_id
        USING ERRCODE = '23514';
    END IF;
  END LOOP;

  RETURN NULL;
END;
$$;

CREATE CONSTRAINT TRIGGER quiz_question_must_have_source
AFTER INSERT OR UPDATE OF question_id ON quiz_question
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION assert_quiz_question_has_source();

CREATE CONSTRAINT TRIGGER quiz_source_preserves_required_source
AFTER INSERT OR UPDATE OF question_id OR DELETE ON quiz_source
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION assert_quiz_question_has_source();

CREATE FUNCTION assert_quiz_question_shape()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  candidate_question_id TEXT;
  candidate_question_ids TEXT[];
  candidate_question_type TEXT;
  option_count INTEGER;
  correct_option_count INTEGER;
  minimum_option_order INTEGER;
  maximum_option_order INTEGER;
  ordering_item_count INTEGER;
  minimum_correct_position INTEGER;
  maximum_correct_position INTEGER;
BEGIN
  IF TG_TABLE_NAME = 'quiz_question' THEN
    IF TG_OP = 'INSERT' THEN
      candidate_question_ids := ARRAY[NEW.question_id];
    ELSE
      candidate_question_ids := ARRAY[OLD.question_id, NEW.question_id];
    END IF;
  ELSIF TG_OP = 'INSERT' THEN
    candidate_question_ids := ARRAY[NEW.question_id];
  ELSIF TG_OP = 'UPDATE' THEN
    candidate_question_ids := ARRAY[OLD.question_id, NEW.question_id];
  ELSE
    candidate_question_ids := ARRAY[OLD.question_id];
  END IF;

  FOREACH candidate_question_id IN ARRAY candidate_question_ids LOOP
    IF candidate_question_id IS NULL THEN
      CONTINUE;
    END IF;

    SELECT question_type
    INTO candidate_question_type
    FROM quiz_question
    WHERE question_id = candidate_question_id;

    IF NOT FOUND THEN
      CONTINUE;
    END IF;

    SELECT
      count(*),
      count(*) FILTER (WHERE is_correct),
      min(display_order),
      max(display_order)
    INTO
      option_count,
      correct_option_count,
      minimum_option_order,
      maximum_option_order
    FROM quiz_option
    WHERE question_id = candidate_question_id;

    SELECT count(*), min(correct_position), max(correct_position)
    INTO ordering_item_count, minimum_correct_position, maximum_correct_position
    FROM quiz_ordering_item
    WHERE question_id = candidate_question_id;

    IF candidate_question_type IN (
      'multiple_choice',
      'true_false',
      'image_identification'
    ) THEN
      IF EXISTS (
        SELECT 1 FROM quiz_ordering_item WHERE question_id = candidate_question_id
      ) THEN
        RAISE EXCEPTION 'choice quiz_question % cannot have ordering items', candidate_question_id
          USING ERRCODE = '23514';
      END IF;

      IF correct_option_count <> 1 THEN
        RAISE EXCEPTION 'choice quiz_question % must have exactly one correct option',
          candidate_question_id
          USING ERRCODE = '23514';
      END IF;

      IF candidate_question_type = 'true_false' THEN
        IF option_count <> 2 OR minimum_option_order <> 1 OR maximum_option_order <> 2 THEN
          RAISE EXCEPTION 'true_false quiz_question % must have two contiguous options',
            candidate_question_id
            USING ERRCODE = '23514';
        END IF;

        IF NOT EXISTS (
          SELECT 1
          FROM quiz_option
          WHERE question_id = candidate_question_id
            AND option_id = 'true'
            AND option_text = 'True'
            AND display_order = 1
        )
        OR NOT EXISTS (
          SELECT 1
          FROM quiz_option
          WHERE question_id = candidate_question_id
            AND option_id = 'false'
            AND option_text = 'False'
            AND display_order = 2
        ) THEN
          RAISE EXCEPTION 'true_false quiz_question % must use True and False options',
            candidate_question_id
            USING ERRCODE = '23514';
        END IF;
      ELSIF option_count <> 4 OR minimum_option_order <> 1 OR maximum_option_order <> 4 THEN
        RAISE EXCEPTION 'choice quiz_question % must have four contiguous options',
          candidate_question_id
          USING ERRCODE = '23514';
      END IF;

      IF candidate_question_type = 'image_identification' THEN
        IF NOT EXISTS (
          SELECT 1 FROM quiz_image WHERE question_id = candidate_question_id
        ) THEN
          RAISE EXCEPTION 'image_identification quiz_question % must have an image',
            candidate_question_id
            USING ERRCODE = '23514';
        END IF;
      ELSIF EXISTS (
        SELECT 1 FROM quiz_image WHERE question_id = candidate_question_id
      ) THEN
        RAISE EXCEPTION 'non-image quiz_question % cannot have an image', candidate_question_id
          USING ERRCODE = '23514';
      END IF;
    ELSIF candidate_question_type = 'chronological_ordering' THEN
      IF EXISTS (
        SELECT 1 FROM quiz_option WHERE question_id = candidate_question_id
      ) THEN
        RAISE EXCEPTION 'chronological quiz_question % cannot have options', candidate_question_id
          USING ERRCODE = '23514';
      END IF;

      IF EXISTS (
        SELECT 1 FROM quiz_image WHERE question_id = candidate_question_id
      ) THEN
        RAISE EXCEPTION 'chronological quiz_question % cannot have an image', candidate_question_id
          USING ERRCODE = '23514';
      END IF;

      IF ordering_item_count <> 4
         OR minimum_correct_position <> 1
         OR maximum_correct_position <> 4 THEN
        RAISE EXCEPTION 'chronological quiz_question % must have four contiguous items',
          candidate_question_id
          USING ERRCODE = '23514';
      END IF;
    END IF;
  END LOOP;

  RETURN NULL;
END;
$$;

CREATE CONSTRAINT TRIGGER quiz_question_shape_is_valid
AFTER INSERT OR UPDATE OF question_id, question_type ON quiz_question
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION assert_quiz_question_shape();

CREATE CONSTRAINT TRIGGER quiz_option_preserves_question_shape
AFTER INSERT OR UPDATE OF question_id, option_id, display_order, option_text, is_correct OR DELETE
ON quiz_option
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION assert_quiz_question_shape();

CREATE CONSTRAINT TRIGGER quiz_ordering_item_preserves_question_shape
AFTER INSERT OR UPDATE OF question_id, item_id, item_text, correct_position OR DELETE
ON quiz_ordering_item
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION assert_quiz_question_shape();

CREATE CONSTRAINT TRIGGER quiz_image_preserves_question_shape
AFTER INSERT OR UPDATE OF question_id OR DELETE ON quiz_image
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION assert_quiz_question_shape();

CREATE FUNCTION prevent_assigned_quiz_question_identity_or_type_change()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  IF (OLD.question_id IS DISTINCT FROM NEW.question_id
      OR OLD.question_type IS DISTINCT FROM NEW.question_type)
  AND EXISTS (
    SELECT 1
    FROM quiz_daily_question daily_question
    JOIN quiz_daily_challenge challenge
      ON challenge.challenge_date = daily_question.challenge_date
    WHERE daily_question.question_id = OLD.question_id
      AND challenge.completed_at IS NOT NULL
  ) THEN
    RAISE EXCEPTION 'assigned quiz_question % identity and type are immutable', OLD.question_id
      USING ERRCODE = '23514';
  END IF;

  RETURN NEW;
END;
$$;

CREATE TRIGGER assigned_quiz_question_identity_and_type_are_immutable
BEFORE UPDATE OF question_id, question_type ON quiz_question
FOR EACH ROW
EXECUTE FUNCTION prevent_assigned_quiz_question_identity_or_type_change();

CREATE FUNCTION prevent_completed_quiz_daily_changes()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  candidate_challenge_date DATE;
  candidate_challenge_dates DATE[];
BEGIN
  IF TG_TABLE_NAME = 'quiz_daily_challenge' THEN
    IF TG_OP = 'DELETE' THEN
      IF OLD.completed_at IS NOT NULL THEN
        RAISE EXCEPTION 'completed quiz_daily_challenge % is immutable', OLD.challenge_date
          USING ERRCODE = '23514';
      END IF;
      RETURN OLD;
    END IF;

    IF OLD.completed_at IS NOT NULL THEN
      RAISE EXCEPTION 'completed quiz_daily_challenge % is immutable', OLD.challenge_date
        USING ERRCODE = '23514';
    END IF;

    IF OLD.challenge_date IS DISTINCT FROM NEW.challenge_date THEN
      RAISE EXCEPTION 'quiz_daily_challenge date is immutable'
        USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
  END IF;

  IF TG_OP = 'INSERT' THEN
    candidate_challenge_dates := ARRAY[NEW.challenge_date];
  ELSIF TG_OP = 'UPDATE' THEN
    candidate_challenge_dates := ARRAY[OLD.challenge_date, NEW.challenge_date];
  ELSE
    candidate_challenge_dates := ARRAY[OLD.challenge_date];
  END IF;

  FOREACH candidate_challenge_date IN ARRAY candidate_challenge_dates LOOP
    IF EXISTS (
      SELECT 1
      FROM quiz_daily_challenge
      WHERE challenge_date = candidate_challenge_date
        AND completed_at IS NOT NULL
    ) THEN
      RAISE EXCEPTION 'completed quiz_daily_challenge % membership is immutable',
        candidate_challenge_date
        USING ERRCODE = '23514';
    END IF;
  END LOOP;

  IF TG_OP = 'DELETE' THEN
    RETURN OLD;
  END IF;
  RETURN NEW;
END;
$$;

CREATE TRIGGER completed_quiz_daily_challenge_is_immutable
BEFORE UPDATE OR DELETE ON quiz_daily_challenge
FOR EACH ROW
EXECUTE FUNCTION prevent_completed_quiz_daily_changes();

CREATE TRIGGER completed_quiz_daily_membership_is_immutable
BEFORE INSERT OR UPDATE OR DELETE ON quiz_daily_question
FOR EACH ROW
EXECUTE FUNCTION prevent_completed_quiz_daily_changes();

CREATE FUNCTION assert_quiz_daily_challenge_complete()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  candidate_challenge_date DATE;
  candidate_challenge_dates DATE[];
  challenge_completed_at TIMESTAMPTZ;
  daily_question_count INTEGER;
BEGIN
  IF TG_OP = 'INSERT' THEN
    candidate_challenge_dates := ARRAY[NEW.challenge_date];
  ELSE
    candidate_challenge_dates := ARRAY[OLD.challenge_date, NEW.challenge_date];
  END IF;

  FOREACH candidate_challenge_date IN ARRAY candidate_challenge_dates LOOP
    SELECT completed_at
    INTO challenge_completed_at
    FROM quiz_daily_challenge
    WHERE challenge_date = candidate_challenge_date;

    IF NOT FOUND THEN
      CONTINUE;
    END IF;

    IF challenge_completed_at IS NULL THEN
      RAISE EXCEPTION 'quiz_daily_challenge % cannot remain incomplete', candidate_challenge_date
        USING ERRCODE = '23514';
    END IF;

    SELECT count(*)
    INTO daily_question_count
    FROM quiz_daily_question
    WHERE challenge_date = candidate_challenge_date;

    IF daily_question_count <> 20 THEN
      RAISE EXCEPTION 'quiz_daily_challenge % must have exactly 20 questions',
        candidate_challenge_date
        USING ERRCODE = '23514';
    END IF;

    IF EXISTS (
      SELECT 1
      FROM quiz_daily_question daily_question
      JOIN quiz_question question ON question.question_id = daily_question.question_id
      WHERE daily_question.challenge_date = candidate_challenge_date
        AND question.publication_state <> 'published'
    ) THEN
      RAISE EXCEPTION 'quiz_daily_challenge % may contain only published questions',
        candidate_challenge_date
        USING ERRCODE = '23514';
    END IF;
  END LOOP;

  RETURN NULL;
END;
$$;

CREATE CONSTRAINT TRIGGER quiz_daily_challenge_must_be_complete
AFTER INSERT OR UPDATE OF completed_at ON quiz_daily_challenge
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION assert_quiz_daily_challenge_complete();

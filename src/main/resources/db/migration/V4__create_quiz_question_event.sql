-- Reviewed editorial links between quiz questions and historical events.
-- Links are curated in quiz JSON (relatedEventIds) and never inferred at runtime.
CREATE TABLE quiz_question_event (
  question_id TEXT NOT NULL
    REFERENCES quiz_question(question_id) ON UPDATE CASCADE ON DELETE CASCADE,
  event_id TEXT NOT NULL
    REFERENCES historical_event(event_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (question_id, event_id)
);

CREATE INDEX quiz_question_event_by_event
  ON quiz_question_event(event_id, question_id);

-- Records which Daily generator produced an assignment. Existing assignments keep
-- version 1 (global balanced selection); date-linked selection writes version 2.
ALTER TABLE quiz_daily_challenge
  ADD COLUMN selection_version SMALLINT NOT NULL DEFAULT 1,
  ADD CONSTRAINT quiz_daily_challenge_selection_version_supported
    CHECK (selection_version IN (1, 2));

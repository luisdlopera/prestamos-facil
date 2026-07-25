-- ============================================================
-- V24: Create outbox_events for reliable event delivery
-- ============================================================

CREATE TABLE outbox_events (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    aggregate_id VARCHAR(255) NOT NULL,
    event_type   VARCHAR(255) NOT NULL,
    payload      JSONB        NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count  INT          NOT NULL DEFAULT 0,
    max_retries  INT          NOT NULL DEFAULT 3,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ,
    last_error   TEXT,

    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_events_status ON outbox_events(status, created_at);

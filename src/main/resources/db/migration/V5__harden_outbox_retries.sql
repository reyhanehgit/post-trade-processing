ALTER TABLE outbox_event
    ADD COLUMN retry_count INT NOT NULL DEFAULT 0;

ALTER TABLE outbox_event
    ADD COLUMN last_error VARCHAR(1000);

ALTER TABLE outbox_event
    ADD COLUMN failed_at TIMESTAMP;

ALTER TABLE outbox_event
    ADD COLUMN next_retry_at TIMESTAMP;

UPDATE outbox_event
SET next_retry_at = created_at
WHERE next_retry_at IS NULL;

CREATE INDEX idx_outbox_event_retry_due
    ON outbox_event (status, next_retry_at);


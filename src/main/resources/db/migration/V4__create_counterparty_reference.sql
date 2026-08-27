CREATE TABLE counterparty_reference (
    counterparty_id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL
);

INSERT INTO counterparty_reference (counterparty_id, name, active)
VALUES ('CP-1', 'Demo Counterparty', TRUE);


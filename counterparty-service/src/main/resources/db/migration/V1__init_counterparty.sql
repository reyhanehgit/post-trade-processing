CREATE TABLE counterparty (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true
);

INSERT INTO counterparty (id, name, active) VALUES
('CP-1', 'Demo Counterparty', true),
('CP-2', 'Test Bank', true),
('CP-3', 'Inactive Partner', false);


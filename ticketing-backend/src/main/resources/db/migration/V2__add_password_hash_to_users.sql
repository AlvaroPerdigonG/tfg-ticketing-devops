ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

UPDATE users
SET password_hash = CASE
    WHEN email = 'user@local.test' THEN '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
    WHEN email = 'agent@local.test' THEN '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
    ELSE '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
END
WHERE password_hash IS NULL OR BTRIM(password_hash) = '';

ALTER TABLE users
    ALTER COLUMN password_hash SET NOT NULL;

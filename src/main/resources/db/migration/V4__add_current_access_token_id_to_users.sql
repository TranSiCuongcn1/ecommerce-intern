ALTER TABLE users
    ADD COLUMN IF NOT EXISTS current_access_token_id VARCHAR(36);

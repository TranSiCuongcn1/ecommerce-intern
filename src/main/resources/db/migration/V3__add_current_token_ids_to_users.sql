ALTER TABLE users
    ADD COLUMN current_access_token_id VARCHAR(36),
    ADD COLUMN current_refresh_token_id VARCHAR(36);

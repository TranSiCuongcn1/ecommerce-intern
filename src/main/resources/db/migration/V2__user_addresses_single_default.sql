CREATE UNIQUE INDEX uk_user_addresses_one_default_per_user
ON user_addresses (user_id)
WHERE is_default = TRUE;

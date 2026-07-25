CREATE TABLE vouchers (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(15,2) NOT NULL,
    max_discount_amount DECIMAL(15,2),
    min_order_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    usage_limit INT,
    used_count INT NOT NULL DEFAULT 0,
    user_limit INT NOT NULL DEFAULT 1,
    start_date TIMESTAMP WITHOUT TIME ZONE,
    end_date TIMESTAMP WITHOUT TIME ZONE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE voucher_usages (
    id UUID PRIMARY KEY,
    voucher_id UUID NOT NULL REFERENCES vouchers(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    order_id UUID REFERENCES orders(id) ON DELETE SET NULL,
    used_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_vouchers_code ON vouchers(code);
CREATE INDEX idx_voucher_usages_user_voucher ON voucher_usages(user_id, voucher_id);

ALTER TABLE orders ADD COLUMN voucher_id UUID REFERENCES vouchers(id);
ALTER TABLE orders ADD COLUMN discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00;

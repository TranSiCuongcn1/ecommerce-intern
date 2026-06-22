CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    receiver_name VARCHAR(150) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    province VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    ward VARCHAR(100) NOT NULL,
    detail_address TEXT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_addresses_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(180) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(220) NOT NULL UNIQUE,
    description TEXT,
    price NUMERIC(15, 2) NOT NULL,
    image_url TEXT,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES categories (id),
    CONSTRAINT chk_products_price_non_negative
        CHECK (price >= 0)
);

CREATE TABLE warehouses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    address TEXT,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    warehouse_id UUID NOT NULL,
    quantity_on_hand INTEGER NOT NULL DEFAULT 0,
    quantity_reserved INTEGER NOT NULL DEFAULT 0,
    reorder_level INTEGER NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_inventory_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES warehouses (id)
        ON DELETE CASCADE,
    CONSTRAINT uk_inventory_product_warehouse
        UNIQUE (product_id, warehouse_id),
    CONSTRAINT chk_inventory_on_hand_non_negative
        CHECK (quantity_on_hand >= 0),
    CONSTRAINT chk_inventory_reserved_non_negative
        CHECK (quantity_reserved >= 0),
    CONSTRAINT chk_inventory_reserved_not_greater_than_on_hand
        CHECK (quantity_reserved <= quantity_on_hand),
    CONSTRAINT chk_inventory_reorder_level_non_negative
        CHECK (reorder_level >= 0)
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_items_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE CASCADE,
    CONSTRAINT uk_cart_items_user_product
        UNIQUE (user_id, product_id),
    CONSTRAINT chk_cart_items_quantity_positive
        CHECK (quantity > 0)
);

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    total_amount NUMERIC(15, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    shipping_address TEXT NOT NULL,
    receiver_name VARCHAR(150) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,    
    shipping_fee NUMERIC(15, 2) NOT NULL DEFAULT 0,
    payment_method VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT chk_orders_total_non_negative
        CHECK (total_amount >= 0)
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    product_id UUID,
    warehouse_id UUID,
    product_name VARCHAR(200) NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal NUMERIC(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_order_items_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES warehouses (id)
        ON DELETE SET NULL,
    CONSTRAINT chk_order_items_unit_price_non_negative
        CHECK (unit_price >= 0),
    CONSTRAINT chk_order_items_quantity_positive
        CHECK (quantity > 0),
    CONSTRAINT chk_order_items_subtotal_non_negative
        CHECK (subtotal >= 0)
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    provider VARCHAR(50),
    provider_transaction_id VARCHAR(150),
    status VARCHAR(30) NOT NULL,
    paid_at TIMESTAMP,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON DELETE CASCADE,
    CONSTRAINT uk_payments_provider_transaction
        UNIQUE (provider, provider_transaction_id),
    CONSTRAINT chk_payments_amount_non_negative
        CHECK (amount >= 0)
);

CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_status ON products (status);
CREATE INDEX idx_warehouses_status ON warehouses (status);
CREATE INDEX idx_inventory_product_id ON inventory (product_id);
CREATE INDEX idx_inventory_warehouse_id ON inventory (warehouse_id);
CREATE INDEX idx_cart_items_user_id ON cart_items (user_id);
CREATE INDEX idx_user_addresses_user_id ON user_addresses (user_id);
CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_status ON payments (status);

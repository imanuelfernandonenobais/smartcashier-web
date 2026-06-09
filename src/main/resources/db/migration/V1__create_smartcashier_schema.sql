CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(150) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NULL,
    role VARCHAR(20) NOT NULL,
    enabled BIT NOT NULL DEFAULT b'1',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE product_categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_product_categories_name UNIQUE (name)
);

CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES product_categories (id),
    CONSTRAINT uk_products_name UNIQUE (name)
);

CREATE TABLE product_unit_values (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    unit_type VARCHAR(20) NOT NULL,
    sale_price DECIMAL(15,2) NOT NULL,
    stock_quantity DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_unit_values_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT uk_product_unit_values_product_unit UNIQUE (product_id, unit_type)
);

CREATE TABLE customers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NULL,
    phone_number VARCHAR(50) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE suppliers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NULL,
    phone_number VARCHAR(50) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE sale_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL,
    transaction_time DATETIME NOT NULL,
    customer_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    amount_paid DECIMAL(15,2) NOT NULL,
    change_amount DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_sale_transactions_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_sale_transactions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_sale_transactions_code UNIQUE (code)
);

CREATE TABLE sale_transaction_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sale_transaction_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    unit_type VARCHAR(20) NOT NULL,
    quantity DECIMAL(15,2) NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    subtotal DECIMAL(15,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sale_transaction_items_sale FOREIGN KEY (sale_transaction_id) REFERENCES sale_transactions (id),
    CONSTRAINT fk_sale_transaction_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE purchase_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL,
    transaction_time DATETIME NOT NULL,
    supplier_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_purchase_transactions_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id),
    CONSTRAINT fk_purchase_transactions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_purchase_transactions_code UNIQUE (code)
);

CREATE TABLE purchase_transaction_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    purchase_transaction_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    unit_type VARCHAR(20) NOT NULL,
    quantity DECIMAL(15,2) NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    subtotal DECIMAL(15,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_purchase_transaction_items_purchase FOREIGN KEY (purchase_transaction_id) REFERENCES purchase_transactions (id),
    CONSTRAINT fk_purchase_transaction_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

INSERT INTO users (full_name, username, password_hash, role, enabled)
VALUES ('Administrator', 'admin', NULL, 'ADMIN', b'1');

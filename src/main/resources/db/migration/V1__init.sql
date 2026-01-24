-- CATEGORY
CREATE TABLE category (
                          id          BIGSERIAL PRIMARY KEY,
                          name        VARCHAR(100) NOT NULL,
                          parent_id   BIGINT,
                          CONSTRAINT fk_category_parent
                              FOREIGN KEY (parent_id) REFERENCES category (id)
);

-- PRODUCT
CREATE TABLE product (
                         id              BIGSERIAL PRIMARY KEY,
                         name            VARCHAR(200) NOT NULL,
                         sku             VARCHAR(100) NOT NULL UNIQUE,
                         price           NUMERIC(10,2) NOT NULL,
                         stock_quantity  INT NOT NULL DEFAULT 0,
                         is_active       BOOLEAN NOT NULL DEFAULT TRUE,
                         category_id     BIGINT,
                         created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                         updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                         CONSTRAINT fk_product_category
                             FOREIGN KEY (category_id) REFERENCES category (id)
);

-- CUSTOMER
CREATE TABLE customer (
                          id          BIGSERIAL PRIMARY KEY,
                          first_name  VARCHAR(100) NOT NULL,
                          last_name   VARCHAR(100) NOT NULL,
                          email       VARCHAR(150) NOT NULL UNIQUE,
                          phone       VARCHAR(30)
);

-- ORDERS
CREATE TABLE orders (
                        id              BIGSERIAL PRIMARY KEY,
                        customer_id     BIGINT NOT NULL,
                        status          VARCHAR(30) NOT NULL,
                        total_amount    NUMERIC(10,2) NOT NULL,
                        created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                        CONSTRAINT fk_orders_customer
                            FOREIGN KEY (customer_id) REFERENCES customer (id)
);

-- ORDER_ITEM
CREATE TABLE order_item (
                            id          BIGSERIAL PRIMARY KEY,
                            order_id    BIGINT NOT NULL,
                            product_id  BIGINT NOT NULL,
                            unit_price  NUMERIC(10,2) NOT NULL,
                            quantity    INT NOT NULL,
                            line_total  NUMERIC(10,2) NOT NULL,
                            CONSTRAINT fk_order_item_order
                                FOREIGN KEY (order_id) REFERENCES orders (id),
                            CONSTRAINT fk_order_item_product
                                FOREIGN KEY (product_id) REFERENCES product (id)
);

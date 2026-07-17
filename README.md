# Ecommerce API

Spring Boot 3 ecommerce MVP backend organized as a modular monolith. The project includes authentication, user profile addresses, catalog management, warehouse and inventory management, cart, checkout, order management, admin media upload, JWT security, Flyway migrations, and service-level unit tests.

## Tech Stack

- Java 21
- Spring Boot 3.3.5
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Redis
- MinIO
- Swagger/OpenAPI
- Docker Compose

## Project Structure

```text
ecommerce-intern/
|-- docker-compose.yml
|-- pom.xml
|-- README.md
`-- src/
    |-- main/
    |   |-- java/com/trancuong/ecommerce/
    |   |   |-- auth/        # Register, login, refresh token, logout
    |   |   |-- user/        # Current user profile and addresses
    |   |   |-- category/    # Category CRUD
    |   |   |-- product/     # Product CRUD
    |   |   |-- warehouse/   # Warehouse CRUD
    |   |   |-- inventory/   # Stock CRUD and inventory allocation
    |   |   |-- cart/        # Current customer cart
    |   |   |-- order/       # Checkout, customer orders, admin order status
    |   |   |-- media/       # Admin media upload to MinIO
    |   |   |-- security/    # JWT filter and security config
    |   |   |-- config/      # Application configuration
    |   |   `-- common/      # Shared API response and error handling
    |   `-- resources/
    |       |-- application.yml
    |       `-- db/migration/
    `-- test/                # Unit tests for core service logic
```

## Local Setup

Requirements:

- JDK 21
- Maven
- Docker Desktop

Start local services:

```bash
docker compose up -d
```

Run the application:

```bash
mvn spring-boot:run
```

Open Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Health check:

```text
GET http://localhost:8080/api/health
```

## Local Services

```text
API:        http://localhost:8080
PostgreSQL: localhost:5433/ecommerce_db
DB user:    postgres
DB pass:    123456
Redis:      localhost:6379
MinIO API:  http://localhost:9000
MinIO UI:   http://localhost:9001
MinIO user: minioadmin
MinIO pass: minioadmin
```

Local admin bootstrap:

```text
Admin email:    admin@example.com
Admin password: admin123456
```

Override with `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD`, `APP_ADMIN_FULL_NAME`, or disable with `APP_ADMIN_BOOTSTRAP_ENABLED=false`.

Main config file:

```text
src/main/resources/application.yml
```

## Database Migration

Flyway manages database schema. Migrations are stored in:

```text
src/main/resources/db/migration
```

Current migrations:

```text
V1__init_ecommerce_schema.sql
V2__user_addresses_single_default.sql
V3__add_current_token_ids_to_users.sql
V4__add_current_access_token_id_to_users.sql
```

Hibernate validates schema on startup:

```yaml
spring.jpa.hibernate.ddl-auto: validate
```

## Swagger Test Script

For the full endpoint-by-endpoint Swagger script, see:

```text
docs/swagger-test-script.md
```

When Swagger asks for authorization, paste the raw `accessToken` only. Do not prefix it with `Bearer`; Swagger adds that automatically.

Quick happy path:

1. Login admin with `admin@example.com` / `admin123456`.
2. Authorize Swagger with the admin `accessToken`.
3. Create category, product, warehouse, and inventory.
4. Register or login a customer.
5. Authorize Swagger with the customer `accessToken`.
6. Create customer address.
7. Add product to cart.
8. Checkout.
9. Verify cart is empty, inventory decreased, and order can be viewed.
10. Authorize as admin again to update order status or upload media.

## API Overview

Public APIs:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout

GET  /api/categories
GET  /api/categories/{id}

GET  /api/products
GET  /api/products/{id}

GET  /api/warehouses
GET  /api/warehouses/{id}

GET  /api/inventory
GET  /api/inventory/{id}
```

Authenticated customer APIs:

```text
GET    /api/me
GET    /api/me/addresses
POST   /api/me/addresses
PUT    /api/me/addresses/{id}
PATCH  /api/me/addresses/{id}/default
DELETE /api/me/addresses/{id}

GET    /api/cart
POST   /api/cart/items
PUT    /api/cart/items/{id}
DELETE /api/cart/items/{id}
DELETE /api/cart

POST   /api/orders/checkout
GET    /api/orders
GET    /api/orders/{id}
```

Admin APIs:

```text
POST   /api/categories
PUT    /api/categories/{id}
DELETE /api/categories/{id}

POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}

POST   /api/warehouses
PUT    /api/warehouses/{id}
DELETE /api/warehouses/{id}

POST   /api/inventory
PUT    /api/inventory/{id}
DELETE /api/inventory/{id}
POST   /api/inventory/allocate

GET    /api/admin/orders
GET    /api/admin/orders/{id}
PATCH  /api/admin/orders/{id}/status
POST   /api/admin/media/upload
```

Register creates `CUSTOMER` users only. The local admin account is bootstrapped from `app.admin.bootstrap` when enabled.

## Useful Commands

Run tests:

```bash
mvn test
```

Current unit test coverage includes auth, user profile, category, product, warehouse, inventory, cart, checkout, and admin order status.

Build jar:

```bash
mvn clean package
```

Stop services:

```bash
docker compose down
```

Reset local data:

```bash
docker compose down -v
docker compose up -d
```

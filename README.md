# Ecommerce API

Spring Boot 3 ecommerce MVP backend, organized as a modular monolith. The project includes authentication, user profile addresses, catalog management, warehouse management, and inventory allocation.

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
├── docker-compose.yml
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/trancuong/ecommerce/
        │       ├── EcommerceApiApplication.java
        │       ├── auth/        # Register, login, refresh token, logout
        │       ├── user/        # Current user profile and addresses
        │       ├── category/    # Category CRUD
        │       ├── product/     # Product CRUD
        │       ├── warehouse/   # Warehouse CRUD
        │       ├── inventory/   # Stock CRUD and inventory allocation
        │       ├── cart/        # Current customer cart
        │       ├── order/       # Checkout stub endpoint
        │       ├── media/       # Admin media upload stub endpoint
        │       ├── security/    # JWT filter and security config
        │       ├── config/      # Application configuration
        │       └── common/      # Shared API error handling
        └── resources/
            ├── application.yml
            └── db/migration/
                ├── V1__init_ecommerce_schema.sql
                └── V2__user_addresses_single_default.sql
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
MinIO:      http://localhost:9001
MinIO user: minioadmin
MinIO pass: minioadmin
```

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
```

Hibernate is set to validate only:

```yaml
spring.jpa.hibernate.ddl-auto: validate
```

So Flyway creates/updates tables, and Hibernate validates entity/schema compatibility on startup.

## Swagger Test Script

Use this flow directly in Swagger UI.

For the full endpoint-by-endpoint Swagger script, see:

```text
docs/swagger-test-script.md
```

### 1. Register user

Endpoint:

```text
POST /api/auth/register
```

Body:

```json
{
  "fullName": "Test User",
  "email": "testuser@example.com",
  "password": "password123"
}
```

Expected: `201 Created`. Copy `accessToken` from the response.

### 2. Authorize Swagger

Click `Authorize` in Swagger UI and input:

```text
Bearer <accessToken>
```

Then click `Authorize`.

### 3. Test current profile

Endpoint:

```text
GET /api/me
```

Expected: current authenticated user profile.

### 4. Create user address

Endpoint:

```text
POST /api/me/addresses
```

Body:

```json
{
  "receiverName": "Test User",
  "receiverPhone": "0900000000",
  "province": "Ho Chi Minh",
  "district": "District 1",
  "ward": "Ben Nghe",
  "detailAddress": "123 Nguyen Hue",
  "defaultAddress": true
}
```

### 5. Create category

Endpoint:

```text
POST /api/categories
```

Body:

```json
{
  "name": "Phones",
  "slug": "phones"
}
```

Copy returned `id` as `categoryId`.

### 6. Create product

Endpoint:

```text
POST /api/products
```

Body:

```json
{
  "categoryId": "<categoryId>",
  "name": "iPhone 15",
  "slug": "iphone-15",
  "description": "Apple smartphone",
  "price": 19990000,
  "imageUrl": "https://example.com/iphone-15.jpg",
  "status": "ACTIVE"
}
```

Copy returned `id` as `productId`.

### 7. Create warehouse

Endpoint:

```text
POST /api/warehouses
```

Body:

```json
{
  "code": "HCM-01",
  "name": "Ho Chi Minh Warehouse",
  "address": "District 1, Ho Chi Minh",
  "status": "ACTIVE"
}
```

Copy returned `id` as `warehouseId`.

### 8. Create inventory

Endpoint:

```text
POST /api/inventory
```

Body:

```json
{
  "productId": "<productId>",
  "warehouseId": "<warehouseId>",
  "quantityOnHand": 100,
  "quantityReserved": 0,
  "reorderLevel": 10
}
```

### 9. Allocate inventory

Endpoint:

```text
POST /api/inventory/allocate
```

Body:

```json
{
  "productId": "<productId>",
  "quantity": 2
}
```

Expected: `quantityReserved` increases and `availableQuantity` decreases.

## API Overview

Public APIs:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout

GET    /api/categories
POST   /api/categories
GET    /api/categories/{id}
PUT    /api/categories/{id}
DELETE /api/categories/{id}

GET    /api/products
POST   /api/products
GET    /api/products/{id}
PUT    /api/products/{id}
DELETE /api/products/{id}

GET    /api/warehouses
POST   /api/warehouses
GET    /api/warehouses/{id}
PUT    /api/warehouses/{id}
DELETE /api/warehouses/{id}

GET    /api/inventory
POST   /api/inventory
GET    /api/inventory/{id}
PUT    /api/inventory/{id}
DELETE /api/inventory/{id}
POST   /api/inventory/allocate
```

Authenticated APIs:

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

POST /api/orders/checkout
```

Admin API:

```text
POST /api/admin/media/upload
```

Note: register currently creates `CUSTOMER` users only. There is no public API to create an `ADMIN` user yet.

## Current TODO Endpoints

These endpoints exist but currently return placeholder responses:

```text
POST /api/orders/checkout
POST /api/admin/media/upload
```

## Useful Commands

Run tests:

```bash
mvn test
```

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

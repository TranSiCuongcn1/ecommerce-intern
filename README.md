# Ecommerce API

Spring Boot 3 ecommerce MVP backend using a modular monolith structure.

## Architecture

This project is one deployable Spring Boot application, but the code is split by business domain:

```text
auth      - register, login, JWT flow
user      - user account and role
category  - product categories
product   - product catalog
cart      - cart and cart item flow
order     - checkout and order management
media     - product image upload with MinIO
security  - Spring Security and JWT configuration
common    - shared response, exception, and config classes
```

## Local Services

Start PostgreSQL and Redis:

```bash
docker compose up -d
```

Then run the Spring Boot app:

```bash
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Health check:

```text
GET http://localhost:8080/api/health
```

## Database Migration

This project uses Flyway to manage PostgreSQL schema changes.

Migration files live in:

```text
src/main/resources/db/migration
```

The first migration is:

```text
V1__init_ecommerce_schema.sql
```

When the Spring Boot app starts, Flyway connects to PostgreSQL and runs any migration file that has not been applied yet. Flyway records migration history in the `flyway_schema_history` table.

Hibernate is configured with:

```yaml
spring.jpa.hibernate.ddl-auto: validate
```

That means Hibernate checks whether the Java entities match the database schema, but Flyway owns table creation and schema updates.

## Suggested Build Order

1. Implement auth: register, login, JWT.
2. Implement category and product CRUD.
3. Implement cart APIs.
4. Implement checkout order flow with `@Transactional`.
5. Add Redis cache for product list/detail.
6. Add MinIO upload for product images.
7. Add Swagger examples and README testing flow.

## MinIO Configuration

MinIO is configured locally via Docker Compose. Default credentials and environment variables:

```bash
MINIO_ENDPOINT=http://127.0.0.1:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET_NAME=ecommerce-products
```

The database should store only the returned image URL. The actual image file is managed by MinIO.

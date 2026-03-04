# Order Management Service

A Spring Boot microservice for managing products and orders.

## Quick Start

### Option 1: Using Maven
```bash
mvn spring-boot:run
```

### Option 2: Using Java
```bash
mvn clean package
java -jar target/order-management-service-1.0.0-SNAPSHOT.jar
```

The app runs on `http://localhost:8080`

**Credentials:** `admin` / `admin123`

## API Endpoints

### Products
- `POST /api/v1/products` - Create product
- `GET /api/v1/products/{id}` - Get product
- `GET /api/v1/products` - List all

### Orders
- `POST /api/v1/orders` - Create order (validates all product IDs exist)
- `GET /api/v1/orders/{id}` - Get order
- `GET /api/v1/orders` - List all

## Example Requests

```bash
# Create a product
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{"name": "Laptop", "description": "MacBook Pro", "price": 2499.99}'

# Create an order
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{"customerName": "John", "items": [{"productId": 1, "quantity": 2}]}'
```

## Swagger UI

http://localhost:8080/swagger-ui.html

## Running Tests

```bash
mvn test
```

## Database

Uses H2 in-memory database. Console available at `/h2-console` (JDBC URL: `jdbc:h2:mem:orderdb`)

## Schema Design

```
products (id, name, description, price, created_at, updated_at)
    |
    v
order_items (id, order_id, product_id, quantity, unit_price, subtotal)
    |
    v
orders (id, customer_name, total_price, status, created_at, updated_at)
```

Decided to store `unit_price` on order items to preserve the price at time of order (in case product prices change later).

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Data JPA
- Spring Security (Basic Auth)
- H2 Database
- SpringDoc OpenAPI

## What I'd add with more time

- Pagination on list endpoints
- Update/delete endpoints
- Stock/inventory tracking
- Better auth (JWT)

# Spring E-commerce API

A production-grade, AI-powered e-commerce backend built with **Spring Boot 3.4.2**, following clean architecture principles and modern DevOps practices.

This project demonstrates:

- Layered Architecture
- RESTful API design
- Database versioning & migrations
- AI integration (Spring AI + Ollama)
- n8n workflow automation
- Dockerized infrastructure
- Test automation & code quality enforcement

---

## Tech Stack

- **Java 21**
- **Spring Boot 3.4.2**
- **Spring Data JPA (Hibernate)**
- **PostgreSQL 15**
- **Flyway** (database migrations)
- **Spring AI + Ollama (LLaMA 3.2)**
- **n8n** (workflow automation)
- **Docker & Docker Compose**
- **Swagger / OpenAPI**
- **JUnit 5 & Mockito**
- **JaCoCo** (test coverage)
- **SonarCloud** (code quality & coverage)

---

## Architecture

The project follows a **Layered Architecture**.

```
src/main/java/com/example/springecommerceapi/
├── controller/       # REST endpoints
├── service/          # Business logic
├── repository/       # Database access (Spring Data JPA)
├── domain/           # JPA entities
├── dto/              # Request/Response models
├── exception/        # Global exception handling
├── config/           # OpenAPI configuration
├── function/         # Spring AI function definitions
└── integration/      # n8n event publisher
```

---

## Features

### Category Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/categories` | Create category (root or with parent) |
| GET | `/api/categories/{id}` | Get category by id |
| GET | `/api/categories` | List all categories |
| PUT | `/api/categories/{id}` | Update category |
| DELETE | `/api/categories/{id}` | Delete category |

Hierarchical category support (parent-child).

### Product Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/products` | Create product |
| GET | `/api/products/{id}` | Get product by id |
| GET | `/api/products` | List all products |
| PUT | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |

SKU uniqueness validation, category resolution, filtered search support.

### Customer Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/customers` | Create customer |
| GET | `/api/customers/{id}` | Get customer by id |
| GET | `/api/customers` | List all customers |
| PUT | `/api/customers/{id}` | Update customer |
| DELETE | `/api/customers/{id}` | Delete customer |

### Order Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Create order |
| GET | `/api/orders/{id}` | Get order by id |
| GET | `/api/orders` | List all orders |
| PATCH | `/api/orders/{id}/status` | Update order status |
| DELETE | `/api/orders/{id}` | Delete order |

Transactional order processing with automatic total calculation. Order statuses: `PENDING`, `SHIPPED`, `DELIVERED`, `CANCELLED`.

---

## AI Shopping Assistant

Natural language product search powered by **Spring AI + Ollama (LLaMA 3.2)**. The assistant uses a `productSearchFunction` tool to query the product catalog and return relevant recommendations in JSON format.

### Endpoint
```
POST /api/assistant/chat
```

### Request Body
```json
{
  "message": "Bana uygun fiyatlı bir kulaklık öner",
  "userId": 1,
  "email": "kullanici@mail.com",
  "source": "web"
}
```

### Response
```json
{
  "answer": "Size uygun fiyatlı kulaklık önerilerimiz...",
  "recommendedProducts": [
    {
      "id": 5,
      "name": "Wireless Headset Pro",
      "sku": "WH-PRO-001",
      "price": 299.99,
      "stockQuantity": 15,
      "active": true,
      "categoryId": 3,
      "categoryName": "Electronics"
    }
  ]
}
```

### cURL Example
```bash
curl -X POST http://localhost:8080/api/assistant/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Suggest an affordable wireless headset",
    "userId": 1,
    "email": "test@mail.com",
    "source": "web"
  }'
```

---

## n8n Workflow Automation

The project integrates with **n8n** for event-driven workflow automation. When the AI assistant completes a conversation, a `conversation_completed` event is published to an n8n webhook.

### How It Works

```
User Request → AI Assistant → Response + n8n Event
                                          ↓
                                   n8n Webhook
                                          ↓
                              Email / Slack / Sheets...
```

### Event Payload (sent to n8n)
```json
{
  "eventType": "conversation_completed",
  "timestamp": "2026-03-08T14:30:00Z",
  "userId": 1,
  "email": "kullanici@mail.com",
  "source": "web",
  "userMessage": "Kulaklık öner",
  "assistantAnswer": "Size uygun kulaklık önerilerimiz...",
  "recommendedProducts": [...]
}
```

### Configuration (`application.yml`)
```yaml
n8n:
  webhook-url: https://your-instance.app.n8n.cloud/webhook/conversation-completed
  enabled: true
  # api-key: optional-secret-key
```

### Supported Workflow Examples

| Workflow | Trigger | Action |
|----------|---------|--------|
| Follow-up Email | AI conversation completed | Send product recommendations via Gmail |
| Stock Alerts | Schedule (hourly) | Poll `/api/products`, alert if stock < 5 |
| Order Tracking | Schedule (5 min) | Log new orders to Google Sheets |
| Status Notifications | Schedule (2 min) | Email customer on SHIPPED/DELIVERED |

### n8n Cloud Setup
1. Create a workflow with a **Webhook** node (POST, path: `conversation-completed`)
2. Add action nodes (Gmail, Slack, Google Sheets, etc.)
3. Activate the workflow
4. Set the production webhook URL in `application.yml`

### Debug Endpoint (dev profile only)
```
GET /debug/n8n
```
Returns current n8n configuration status (webhook URL, enabled, API key presence).

---

## Error Handling

- Global exception handling via `@RestControllerAdvice`
- `NotFoundException` → 404
- `BusinessException` → 400
- Generic exceptions → 500
- Standardized error response format:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 99",
  "path": "/api/products/99",
  "timestamp": "2026-03-08T14:00:00"
}
```

---

## Testing & Code Quality

- Unit tests for all service layers using **JUnit 5** and **Mockito**
- Code coverage generated with **JaCoCo**
- Static code analysis and quality gates enforced by **SonarCloud**

### Test Coverage
| Service | Tests |
|---------|-------|
| CategoryService | 3 tests |
| CustomerService | 6 tests |
| ProductService | 8 tests |
| OrderService | 5 tests |
| AiShoppingAssistantService | 14 tests |

### Run Tests
```bash
mvn test
```

### Generate Coverage Report
```bash
mvn verify
# Report: target/site/jacoco/index.html
```

---

## Database

**PostgreSQL 15** with **Flyway** migrations.

### Tables
- `category` — Hierarchical categories (self-referencing parent_id)
- `product` — Products with SKU, price, stock, category FK
- `customer` — Customers with unique email
- `orders` — Orders with status tracking and customer FK
- `order_item` — Line items with product FK, quantity, pricing

### ER Diagram
```
category ←── product
customer ←── orders ←── order_item ──→ product
```

---

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

All REST endpoints can be explored and tested directly from Swagger.

---

## Run Locally

### 1. Start infrastructure with Docker
```bash
docker compose up -d
```

This starts **PostgreSQL**, **Ollama**, and **n8n**.

### 2. Pull the LLM model
```bash
docker exec -it spring_ecommerce_ollama ollama pull llama3.2
```

### 3. Run the application
```bash
mvn spring-boot:run
```

### 4. Access the services

| Service | URL |
|---------|-----|
| Spring API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| n8n Dashboard | http://localhost:5678 |
| Ollama | http://localhost:11434 |
| PostgreSQL | localhost:5432 |

---

## Project Structure

```
spring-ecommerce-api/
├── src/
│   ├── main/
│   │   ├── java/com/example/springecommerceapi/
│   │   │   ├── config/
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AiShoppingAssistantController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── N8nDebugController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   └── ProductController.java
│   │   │   ├── domain/
│   │   │   │   ├── Category.java
│   │   │   │   ├── Customer.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── OrderStatus.java
│   │   │   │   └── Product.java
│   │   │   ├── dto/
│   │   │   │   ├── AssistantRequest.java
│   │   │   │   ├── AssistantResponse.java
│   │   │   │   ├── CategoryRequest.java
│   │   │   │   ├── CategoryResponse.java
│   │   │   │   ├── CustomerRequest.java
│   │   │   │   ├── CustomerResponse.java
│   │   │   │   ├── OrderItemRequest.java
│   │   │   │   ├── OrderItemResponse.java
│   │   │   │   ├── OrderRequest.java
│   │   │   │   ├── OrderResponse.java
│   │   │   │   ├── ProductRequest.java
│   │   │   │   ├── ProductResponse.java
│   │   │   │   └── ProductSearchRequest.java
│   │   │   ├── exception/
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── NotFoundException.java
│   │   │   ├── function/
│   │   │   │   └── ProductSearchFunctionConfig.java
│   │   │   ├── integration/
│   │   │   │   └── N8nEventPublisher.java
│   │   │   ├── repository/
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   └── ProductRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AiShoppingAssistantService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── CustomerService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   └── ProductService.java
│   │   │   └── SpringEcommerceApiApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           └── V1__init.sql
│   └── test/
│       └── java/com/example/springecommerceapi/
│           ├── service/
│           │   ├── AiShoppingAssistantServiceTest.java
│           │   ├── CategoryServiceTest.java
│           │   ├── CustomerServiceTest.java
│           │   ├── OrderServiceTest.java
│           │   └── ProductServiceTest.java
│           └── SpringEcommerceApiApplicationTests.java
├── docker-compose.yml
├── pom.xml
└── README.md
```

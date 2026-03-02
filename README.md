# Spring E-commerce API

A production-grade, AI-powered e-commerce backend built with **Spring Boot 4.x**, following clean architecture principles and modern DevOps practices.

This project demonstrates:

- Layered Architecture
- RESTful API design
- Database versioning & migrations
- AI integration (Spring AI + Ollama)
- Dockerized infrastructure
- Test automation & code quality enforcement

---

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 4.x**
- **Spring Data JPA (Hibernate)**
- **PostgreSQL 15**
- **Flyway** (database migrations)
- **Spring AI**
- **Ollama (LLaMA 3.2)**
- **Docker & Docker Compose**
- **Swagger / OpenAPI**
- **JUnit 5 & Mockito**
- **JaCoCo** (test coverage)
- **SonarCloud** (code quality & coverage)

---

## 🧱 Architecture

The project follows a **Layered Architecture**.


### Packages
- `controller` – REST endpoints
- `service` – business logic
- `repository` – database access
- `domain` – JPA entities
- `dto` – request/response models
- `exception` – global exception handling
- `config` – OpenApi configuration

---

## 📦 Features

### Category Management
- Create category (root or with parent)
- Update category
- Delete category
- Get category by id
- List all categories
- Hierarchical category support (parent–child)
### Product Management
- Create product
- Update product
- Delete product
- Get product by id
### Customer Management
- Create customer
- Update customer
- Delete customer
- Get customer by id
- List all customers
### Order Management
- Create order
- Update order
- Delete order
- Get order by id
## 🤖 AI Shopping Assistant
- Allows natural language product search using local LLM.

### Assistant Endpoint
- POST /api/assistant/chat


### 📨 Example Request (cURL)

```bash
curl -X POST http://localhost:8080/api/assistant/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Suggest an affordable wireless headset"
  }'
```


### Error Handling
- Global exception handling with meaningful HTTP status codes
- Standardized error response format
- Meaningful HTTP status codes
- AI exception management

---

## 🧪 Testing & Code Quality

- Unit tests for service layer using **JUnit 5** and **Mockito**
- Code coverage generated with **JaCoCo**
- Static code analysis and quality gates enforced by **SonarCloud**

---

## 📄 API Documentation

Swagger UI is available at:

http://localhost:8080/swagger-ui/index.html


All REST endpoints can be explored and tested directly from Swagger.

---

## 🚀 Run Locally

### 1️⃣ Start PostgreSQL with Docker
```bash
docker compose up -d
mvn spring-boot:run


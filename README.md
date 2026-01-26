# Spring E-commerce API

A simple yet production-ready e-commerce backend API built with **Spring Boot** following a layered architecture and clean coding principles.

This project is designed as a portfolio project to demonstrate backend development skills including REST API design, database migrations, validation, testing, and global exception handling.

---

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot**
- **Spring Data JPA (Hibernate)**
- **PostgreSQL**
- **Flyway** (database migrations)
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

### Error Handling
- Global exception handling with meaningful HTTP status codes
- Standardized error response format

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


# OrderFlow – E-commerce Order Management System

<p align="center">
  <strong>A production-oriented e-commerce backend built with Java and Spring Boot.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.2-6DB33F?logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Maven-Build%20Tool-C71A36?logo=apachemaven" alt="Maven"/>
  <img src="https://img.shields.io/badge/Status-In%20Development-blue" alt="In Development"/>
  <img src="https://img.shields.io/badge/License-MIT-green" alt="MIT License"/>
</p>

---

## Overview

OrderFlow is an e-commerce order management backend designed to handle the core operations of an online shopping platform, including product management, inventory control, shopping carts, order processing, payments, and order tracking.

The project focuses on building a reliable, secure, and maintainable backend system while exploring real-world software engineering challenges such as database transactions, concurrent inventory updates, payment consistency, idempotency, and API performance.

Rather than being limited to basic CRUD operations, OrderFlow aims to demonstrate practical backend engineering principles and production-oriented system design.

This project is being developed incrementally, with an emphasis on understanding the underlying concepts, writing maintainable code, and testing real-world scenarios.

---

## Project Objectives

- Build RESTful APIs using Java and Spring Boot.
- Implement a clean, layered backend architecture.
- Design and manage relational databases using PostgreSQL.
- Implement authentication and role-based authorization.
- Maintain inventory consistency during concurrent purchases.
- Develop reliable order and payment workflows.
- Explore caching, asynchronous processing, and performance optimization.
- Write automated tests and containerize the application.

---

## Technology Stack

| Technology | Purpose | Status |
| :--- | :--- | :--- |
| **Java 21** | Core programming language | In use |
| **Spring Boot 4.1.2** | Backend framework | In use |
| **Maven** | Dependency and build management | In use |
| **Spring Web** | REST API development | In use |
| **PostgreSQL** | Relational database | Planned |
| **Spring Data JPA** | Database persistence | Planned |
| **Hibernate** | ORM | Planned |
| **Spring Security** | Application security | Planned |
| **JWT** | Authentication | Planned |
| **Redis** | Caching and rate limiting | Planned |
| **JUnit 5 & Mockito** | Automated testing | Planned |
| **Docker** | Containerization | Planned |
| **Swagger / OpenAPI** | API documentation | Planned |
| **Apache Kafka** | Event-driven processing | Future enhancement |

---

## Architecture

OrderFlow is being developed using a layered architecture to separate request handling, business logic, data access, and persistence.

### Target Architecture

```text
                  CLIENT
                    |
                    v
            REST CONTROLLERS
                    |
                    v
              SERVICE LAYER
                    |
        +-----------+-----------+
        |           |           |
        v           v           v
     PRODUCT      ORDER      PAYMENT
     SERVICE     SERVICE     SERVICE
        |           |           |
        +-----------+-----------+
                    |
                    v
            REPOSITORY LAYER
                    |
                    v
               POSTGRESQL
                    |
            +-------+-------+
            |               |
            v               v
          REDIS     EXTERNAL SERVICES
```

> **Note:** This represents the intended architecture. Components will be introduced as development progresses.

### Current Implemented Structure

The codebase currently implements the initial layered foundation inside `com.orderflow.orderflow`:

- `HealthController.java` — REST controller exposing the `/api/v1/health` endpoint.
- `service/ProductService.java` — Service layer handling product business logic and validation.
- `repository/ProductRepository.java` — In-memory repository layer backed by a `HashMap` and `AtomicLong` ID generator.
- `model/Product.java` — Domain model representing product attributes.

> **Temporary In-Memory Storage:** `ProductRepository` currently uses an in-memory `HashMap` rather than a database. Product data is not persistent and is lost when the application restarts.

---

## Project Structure

```text
orderflow/
│
├── .mvn/
│   └── wrapper/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/orderflow/orderflow/
│   │   │       ├── model/
│   │   │       │   └── Product.java
│   │   │       ├── repository/
│   │   │       │   └── ProductRepository.java
│   │   │       ├── service/
│   │   │       │   └── ProductService.java
│   │   │       ├── HealthController.java
│   │   │       └── OrderflowApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│           └── com/orderflow/orderflow/
│               └── OrderflowApplicationTests.java
│
├── .gitattributes
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

As the project grows, the structure will expand into additional dedicated packages for controllers, DTOs, configurations, exceptions, and security.

---

## Current Features

### Implemented
- [x] Spring Boot application initialization.
- [x] Maven project configuration.
- [x] Java 21 configuration.
- [x] Embedded Tomcat server setup.
- [x] Configurable application port.
- [x] Initial REST controller (`HealthController.java`).
- [x] Application health-check endpoint (`/api/v1/health`).
- [x] JSON response generation.
- [x] `Product` domain model (`model/Product.java`).
- [x] In-memory `ProductRepository` backed by `HashMap` (`repository/ProductRepository.java`).
- [x] `ProductService` business logic layer (`service/ProductService.java`).

### ProductService Capabilities
- Create products
- Retrieve all products
- Retrieve a product by ID
- Update products
- Delete products
- Validate product names, prices, and stock quantities
- Assign IDs through the repository
- Default newly created products to active (`true`) when no value is provided

> **Note:** The current repository uses an in-memory `HashMap`. Product data is not persistent and is lost when the application restarts.

---

## API Endpoints

| Method | Endpoint | Description | Status |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/health` | Check application health | Implemented |

### Health Check API

**Request**
```http
GET /api/v1/health
```

**Response**
```json
{
  "status": "UP",
  "application": "OrderFlow"
}
```

**Local development URL**
```
http://localhost:8081/api/v1/health
```

---

## Development Roadmap

The project is being developed in the following phases.

### Phase 1 – Project Foundation
- [x] Initialize Spring Boot project.
- [x] Configure Maven and Java 21.
- [x] Start embedded Tomcat server.
- [x] Implement health-check endpoint.
- [x] Verify JSON API response.
- [x] Initialize local Git repository.
- [x] Publish initial code to GitHub.

### Phase 2 – Product Management
- [x] Design Product model (`Product.java`).
- [x] Implement in-memory repository layer (`ProductRepository.java`).
- [x] Implement service layer (`ProductService.java`).
- [x] Implement service-layer product CRUD logic and validation.
- [ ] Implement Product DTOs (request and response).
- [ ] Build Product REST controller and CRUD API endpoints.
- [ ] Add controller/DTO request validation.
- [ ] Implement global exception handling.

### Phase 3 – Database Integration
- [ ] Configure PostgreSQL.
- [ ] Integrate Spring Data JPA.
- [ ] Configure Hibernate.
- [ ] Design database relationships.
- [ ] Implement database migrations.
- [ ] Test database operations.

### Phase 4 – Authentication and Authorization
- [ ] Integrate Spring Security.
- [ ] Implement customer registration.
- [ ] Implement secure login.
- [ ] Implement JWT authentication.
- [ ] Add password hashing using BCrypt.
- [ ] Implement role-based access control.
- [ ] Secure protected endpoints.

### Phase 5 – Shopping Cart and Order Management
- [ ] Implement shopping cart APIs.
- [ ] Add cart item management.
- [ ] Implement checkout workflow.
- [ ] Create order entities.
- [ ] Implement order placement.
- [ ] Implement order status lifecycle.
- [ ] Add order cancellation.
- [ ] Implement order history.

### Phase 6 – Inventory and Transaction Management
- [ ] Implement inventory management.
- [ ] Introduce database transactions.
- [ ] Understand and implement ACID properties.
- [ ] Implement concurrency control.
- [ ] Prevent inventory overselling.
- [ ] Implement stock reservation.
- [ ] Handle failed order workflows.

### Phase 7 – Payment Integration
- [ ] Integrate payment gateway sandbox.
- [ ] Implement payment initiation.
- [ ] Implement payment verification.
- [ ] Handle payment webhooks.
- [ ] Implement idempotency.
- [ ] Implement refund workflows.
- [ ] Handle payment and order failures.

### Phase 8 – Performance and Scalability
- [ ] Integrate Redis.
- [ ] Implement caching.
- [ ] Add API rate limiting.
- [ ] Implement asynchronous processing.
- [ ] Explore event-driven architecture.
- [ ] Explore Apache Kafka.
- [ ] Optimize database queries.

### Phase 9 – Testing and Deployment
- [ ] Write unit tests using JUnit 5.
- [ ] Implement Mockito-based tests.
- [ ] Add integration tests.
- [ ] Test concurrent purchase scenarios.
- [ ] Document APIs using Swagger/OpenAPI.
- [ ] Containerize using Docker.
- [ ] Configure deployment.
- [ ] Add application monitoring.

---

## Engineering Challenges

The project will address several important backend engineering challenges.

| Challenge | Intended Approach |
| :--- | :--- |
| **Concurrent purchases** | Database locking and transaction management |
| **Inventory overselling** | Atomic stock updates and concurrency control |
| **Duplicate requests** | Idempotency keys |
| **Payment failures** | Payment verification and recovery workflows |
| **Order consistency** | Transaction management and reliable state transitions |
| **Slow API responses** | Redis caching and query optimization |
| **Unauthorized access** | Spring Security and JWT |
| **Application reliability** | Automated testing and exception handling |

> *These are development objectives, not claims of already implemented functionality.*

---

## Running Locally

### Prerequisites
- Java Development Kit 21
- Git
- An IDE such as IntelliJ IDEA or Antigravity
- Maven is available through the included Maven Wrapper.

### 1. Clone the repository
```bash
git clone https://github.com/Mahishukla1207/OrderFlow.git
cd OrderFlow
```

### 2. Run the application

**Windows PowerShell:**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
./mvnw spring-boot:run
```

### 3. Access the application
The application currently runs on port `8081`.
- [http://localhost:8081/api/v1/health](http://localhost:8081/api/v1/health)

### 4. Build the application

**Windows:**
```powershell
.\mvnw.cmd clean package
```

**Linux / macOS:**
```bash
./mvnw clean package
```

---

## Commit Convention

We use conventional commit-style messages:

| Type | Purpose |
| :--- | :--- |
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `refactor` | Code restructuring |
| `test` | Test additions or modifications |
| `chore` | Configuration and maintenance |

**Examples:**
```bash
git commit -m "feat: add product management APIs"
git commit -m "fix: prevent negative inventory"
git commit -m "docs: update project roadmap"
```

---

## Learning and Development Philosophy

OrderFlow is also a hands-on backend engineering learning project.

The development process emphasizes:
- Understanding Java and Spring Boot fundamentals.
- Learning database design and transaction management.
- Writing and debugging code independently.
- Understanding architectural decisions and trade-offs.
- Using AI development tools as assistants rather than substitutes for engineering knowledge.
- Practicing real-world backend scenarios.
- Preparing for software development and backend engineering interviews.

---

## Project Progress

**Current milestone:** Phase 2 – Product Management (domain model, in-memory repository, and service layer).

| Metric | Current Progress |
| :--- | :--- |
| **Development phase** | Phase 2 – Product Management (In Progress) |
| **Implemented API endpoints** | 1 (`/api/v1/health`) |
| **Core business modules** | 1 (Product Management – Model, In-Memory Repository, Service) |
| **Database integration** | Pending (currently using an in-memory `HashMap`) |
| **Authentication** | Pending |
| **Payment integration** | Pending |

### Component Status
- Product model (`Product.java`) — Implemented
- In-memory product repository (`ProductRepository.java`) — Implemented
- Product service layer (`ProductService.java`) — Implemented
- Health check endpoint — Implemented

### Upcoming Milestones
- Product REST controller
- REST API endpoints for CRUD operations
- Request and response DTOs
- Global exception handling
- Unit testing
- PostgreSQL and Spring Data JPA integration

*Progress will be updated as features are implemented, tested, and documented.*

---

## Repository

GitHub: [Mahishukla1207/OrderFlow](https://github.com/Mahishukla1207/OrderFlow)

<p align="center">
  <strong>Built with Java. Powered by Spring Boot. Designed for reliable backend engineering.</strong>
</p>

<p align="center">
  <em>OrderFlow – From products to payments, one reliable transaction at a time.</em>
</p>


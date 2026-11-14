# OrderFlow – E-commerce Order Management System

<p align="center">
  <strong>An e-commerce order management backend built with Java and Spring Boot.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.2-6DB33F?logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Maven-Build%20Tool-C71A36?logo=apachemaven" alt="Maven"/>
  <img src="https://img.shields.io/badge/Status-In%20Development-blue" alt="In Development"/>
</p>

---

## Project Overview

**OrderFlow** is an e-commerce order management backend system designed to handle the core operations of an online shopping platform, including product catalog management, inventory control, shopping carts, checkout workflows, payment processing, and order lifecycle tracking.

The project is developed using **Java 21** and **Spring Boot**, focusing on building a maintainable, clean-architecture backend while incrementally exploring real-world software engineering concepts. Rather than implementing everything at once, OrderFlow is built step-by-step — establishing architectural boundaries, in-memory data structures, DTO contracts, and business validations first, before introducing database persistence, concurrency controls, security, and distributed tooling.

---

## Technology Stack

| Technology | Purpose | Status |
| :--- | :--- | :--- |
| **Java 21** | Core programming language | In use |
| **Spring Boot 4.1.2** | Application framework | In use |
| **Maven** | Dependency management and build automation | In use |
| **Spring Web (MVC)** | REST API routing, request handling, and JSON serialization | In use |
| **REST APIs** | HTTP-based client-server communication | In use |
| **Git & GitHub** | Version control and repository hosting | In use |
| **Jakarta Bean Validation** | Declarative request validation annotations | Planned |
| **Global Exception Handling** | Centralized `@RestControllerAdvice` error translation | Planned |
| **PostgreSQL** | Relational database persistence | Planned |
| **Spring Data JPA & Hibernate** | ORM and repository persistence | Planned |
| **JUnit 5 & Mockito** | Automated unit and integration testing | Planned |
| **Spring Security & JWT** | Authentication, authorization, and RBAC | Planned |
| **Redis** | In-memory caching and rate limiting | Planned |
| **Docker** | Containerization and local environment setup | Planned |
| **Apache Kafka** | Asynchronous event-driven processing | Future enhancement |

---

## Current Features

- **Application Health Check**: Endpoint (`/api/v1/health`) returning server status and application name.
- **Product Domain Model**: Encapsulated `Product` entity with `id`, `name`, `description`, `price`, `stockQuantity`, and `active` attributes.
- **In-Memory Storage**: `ProductRepository` using a `HashMap` for storage with `AtomicLong` for atomic ID generation.
- **Service Layer & Business Validation**:
  - Validates that product names are not null or blank.
  - Enforces positive product prices (`price > 0`) using `BigDecimal.compareTo`.
  - Ensures non-negative stock counts (`stockQuantity >= 0`).
  - Defaults newly created products to `active = true` when null.
  - Enforces server-assigned IDs during creation and preserves IDs during updates.
- **Data Transfer Objects (DTOs)**:
  - `ProductRequest`: Input payload containing `name`, `description`, `price`, and `stockQuantity`. Prevents client tampering with `id` or `active`.
  - `ProductResponse`: Read-only output representation exposing complete product details to clients.
- **Manual DTO Mapping**: Explicit, decoupling conversions between DTOs and domain models directly in the controller layer.
- **Complete Product REST API**: Full CRUD endpoints with standard HTTP status codes (`201 Created`, `200 OK`, `204 No Content`, `404 Not Found`).

---

## Architecture

OrderFlow follows a layered architecture to maintain clear boundaries between transport handling, business logic, and data storage:

```text
CLIENT (HTTP / JSON)
        │
        ▼
CONTROLLER LAYER  ─── [ProductController, HealthController]
        │             • Maps HTTP verbs and URI routes
        │             • Deserializes JSON into DTOs (ProductRequest)
        │             • Performs manual DTO ↔ Entity conversion
        │             • Dispatches ResponseEntity with appropriate HTTP status codes
        ▼
 SERVICE LAYER    ─── [ProductService]
        │             • Implements core business logic and input validation
        │             • Enforces domain invariants (valid price, stock, active status)
        │             • Prevents ID forgery on creation and preserves IDs on update
        ▼
REPOSITORY LAYER  ─── [ProductRepository]
        │             • Abstracts persistence operations (save, findById, findAll, deleteById)
        │             • Stores products in an in-memory HashMap
        │             • Generates sequential IDs atomically via AtomicLong
        ▼
   DATA STORE     ─── [JVM Heap Memory] (PostgreSQL planned)
```

### Layer Responsibilities

- **Controller Layer (`controller`)**: Handles HTTP requests, extracts request parameters and JSON bodies, converts DTOs to/from internal entities, and returns standard HTTP responses.
- **Service Layer (`service`)**: Houses business logic and validation rules. It operates independently of the transport protocol (HTTP) and coordinates changes through the repository.
- **Repository Layer (`repository`)**: Encapsulates data access. Currently manages an in-memory collection, which can be cleanly swapped for Spring Data JPA without altering service contracts.
- **Model & DTO Layer (`model`, `dto`)**:
  - `model/Product`: Internal domain representation.
  - `dto/ProductRequest`: External API contract for client write operations.
  - `dto/ProductResponse`: External API contract for client read responses.

---

## API Documentation

### Implemented Endpoints

| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/health` | Application health check | `200 OK` |
| `POST` | `/api/v1/products` | Create a new product | `201 Created` |
| `GET` | `/api/v1/products` | Retrieve all products | `200 OK` |
| `GET` | `/api/v1/products/{id}` | Retrieve a product by ID | `200 OK` (found) / `404 Not Found` (missing) |
| `PUT` | `/api/v1/products/{id}` | Update an existing product | `200 OK` (updated) / `404 Not Found` (missing) |
| `DELETE` | `/api/v1/products/{id}` | Delete a product by ID | `204 No Content` (deleted) / `404 Not Found` (missing) |

> **Note on Client Error Handling:** The service layer validates inputs and throws `IllegalArgumentException` for invalid data (e.g. blank name, non-positive price). Translating these into client-friendly `400 Bad Request` responses will be added in the upcoming Global Exception Handling milestone.

---

### Request & Response Examples

#### 1. Health Check
- **Request**: `GET /api/v1/health`
- **Response** (`200 OK`):
```json
{
  "status": "UP",
  "application": "OrderFlow"
}
```

#### 2. Create Product
- **Request**: `POST /api/v1/products`
```json
{
  "name": "Wireless Mechanical Keyboard",
  "description": "Compact 75% layout with hot-swappable switches",
  "price": 89.99,
  "stockQuantity": 50
}
```
- **Response** (`201 Created`):
```json
{
  "id": 1,
  "name": "Wireless Mechanical Keyboard",
  "description": "Compact 75% layout with hot-swappable switches",
  "price": 89.99,
  "stockQuantity": 50,
  "active": true
}
```

#### 3. Get All Products
- **Request**: `GET /api/v1/products`
- **Response** (`200 OK`):
```json
[
  {
    "id": 1,
    "name": "Wireless Mechanical Keyboard",
    "description": "Compact 75% layout with hot-swappable switches",
    "price": 89.99,
    "stockQuantity": 50,
    "active": true
  }
]
```

#### 4. Get Product by ID
- **Request**: `GET /api/v1/products/1`
- **Response** (`200 OK`):
```json
{
  "id": 1,
  "name": "Wireless Mechanical Keyboard",
  "description": "Compact 75% layout with hot-swappable switches",
  "price": 89.99,
  "stockQuantity": 50,
  "active": true
}
```
- **Response** (`404 Not Found`): Empty response body.

#### 5. Update Product
- **Request**: `PUT /api/v1/products/1`
```json
{
  "name": "Wireless Mechanical Keyboard V2",
  "description": "Upgraded compact 75% layout with silent switches",
  "price": 94.99,
  "stockQuantity": 45
}
```
- **Response** (`200 OK`):
```json
{
  "id": 1,
  "name": "Wireless Mechanical Keyboard V2",
  "description": "Upgraded compact 75% layout with silent switches",
  "price": 94.99,
  "stockQuantity": 45,
  "active": true
}
```
- **Response** (`404 Not Found`): Empty response body.

#### 6. Delete Product
- **Request**: `DELETE /api/v1/products/1`
- **Response** (`204 No Content`): Empty response body.
- **Response** (`404 Not Found`): Empty response body.

---

## Project Structure

```text
orderflow/
│
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/orderflow/orderflow/
│   │   │       ├── controller/
│   │   │       │   └── ProductController.java
│   │   │       ├── dto/
│   │   │       │   ├── ProductRequest.java
│   │   │       │   └── ProductResponse.java
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

---

## Current Limitations

- **In-Memory Storage Only**: Products are stored in a Java `HashMap`. All data resides in JVM heap memory and is lost when the application stops or restarts.
- **No Thread-Safety on Storage Map**: While `AtomicLong` provides atomic ID generation, `HashMap` itself is not thread-safe under concurrent write operations.
- **No Persistent Database Integration**: Relational persistence via PostgreSQL and Spring Data JPA is planned for future phases.
- **No Bean Validation or Centralized Exception Handling**: Validation currently relies on manual programmatic checks in `ProductService`, and uncaught runtime exceptions return standard Spring Boot 500 error responses rather than formatted 400 Bad Request payloads.
- **Not Production-Ready**: The system is an educational and exploratory backend under active development.

---

## Running Locally

### Prerequisites
- **Java Development Kit (JDK) 21** or higher
- **Git**
- Maven is bundled via the included **Maven Wrapper** (`mvnw` / `mvnw.cmd`)

### 1. Clone the repository
```bash
git clone https://github.com/Mahishukla1207/OrderFlow.git
cd OrderFlow
```

### 2. Run the application
The application runs on port **`8081`** as configured in `src/main/resources/application.properties` (`server.port=8081`).

**Windows PowerShell:**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
./mvnw spring-boot:run
```

### 3. Access endpoints
- **Health Check**: [http://localhost:8081/api/v1/health](http://localhost:8081/api/v1/health)
- **Products API**: [http://localhost:8081/api/v1/products](http://localhost:8081/api/v1/products)

### 4. Build package
**Windows:**
```powershell
.\mvnw.cmd clean package
```

**Linux / macOS:**
```bash
./mvnw clean package
```

---

## Project Progress & Roadmap

### Progress Summary

| Phase | Description | Status |
| :--- | :--- | :--- |
| **Phase 1 – Project Foundation** | Spring Boot 4.1.2, Java 21, Tomcat on port 8081, health check API | **Completed** |
| **Phase 2 – Product Management** | Model, in-memory repository, service validation, DTOs, CRUD REST endpoints | **Completed** |
| **Phase 3 – Validation & Error Handling** | Jakarta Bean Validation, `@RestControllerAdvice`, structured error responses | **Next Milestone** |
| **Phase 4 – Database Integration** | PostgreSQL, Spring Data JPA, Hibernate ORM, migrations | Planned |
| **Phase 5 – Automated Testing** | Unit tests (JUnit 5, Mockito), integration tests (`MockMvc`) | Planned |
| **Phase 6 – Authentication & Security** | Spring Security, BCrypt, JWT, role-based access control | Planned |
| **Phase 7 – Cart & Order Lifecycle** | Shopping cart management, order placement, state transitions | Planned |
| **Phase 8 – Inventory & Concurrency** | Transactions, concurrency control, preventing overselling | Planned |
| **Phase 9 – Payments & Resilience** | Payment sandbox, idempotency keys, webhooks | Planned |
| **Phase 10 – Performance & Scale** | Redis caching, API rate limiting, Apache Kafka event streaming | Planned |

---

## Development & Learning Philosophy

OrderFlow is developed as a hands-on backend engineering project with a focus on understanding core principles:
- **Foundational Understanding**: Grasping Java 21 and Spring Boot internals rather than relying on auto-configuration as a black box.
- **Architectural Discipline**: Strict separation between controller transport, service business rules, and repository persistence.
- **Contract Safety**: Using DTOs to decouple external API contracts from internal entity representations.
- **Incremental Engineering**: Moving deliberately from working in-memory prototypes to robust persistence and distributed architecture.

---

## Commit Convention

We use conventional commit messages to maintain clean project history:

| Type | Purpose |
| :--- | :--- |
| `feat` | New feature or capability |
| `fix` | Bug fix |
| `docs` | Documentation updates |
| `refactor` | Code restructuring without behavioral changes |
| `test` | Adding or updating tests |
| `chore` | Build configuration and maintenance |

---

## Repository

GitHub: [Mahishukla1207/OrderFlow](https://github.com/Mahishukla1207/OrderFlow)

<p align="center">
  <em>OrderFlow – Exploring reliable backend engineering with Java and Spring Boot.</em>
</p>

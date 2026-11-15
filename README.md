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

The project is developed using **Java 21** and **Spring Boot**, focusing on building a maintainable, clean-architecture backend while incrementally exploring real-world software engineering concepts. Rather than implementing everything at once, OrderFlow is built step-by-step — establishing architectural boundaries, in-memory data structures, DTO contracts, declarative validations, and service logic first, before introducing persistent databases, concurrency controls, security, and distributed tooling.

---

## Technology Stack

| Technology | Purpose | Status |
| :--- | :--- | :--- |
| **Java 21** | Core programming language | In use |
| **Spring Boot 4.1.2** | Application framework | In use |
| **Maven** | Dependency management and build automation | In use |
| **Spring Web (MVC)** | REST API routing, request handling, and JSON serialization | In use |
| **Jakarta Bean Validation** | Declarative payload constraint validation (`spring-boot-starter-validation`) | In use |
| **REST APIs** | HTTP-based client-server communication | In use |
| **Git & GitHub** | Version control and repository hosting | In use |
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

- **Application Health Check**: Endpoint (`/api/v1/health`) returning server status (`UP`) and application name (`OrderFlow`).
- **Product Domain Model**: Encapsulated `Product` entity with `id`, `name`, `description`, `price`, `stockQuantity`, and `active` attributes.
- **In-Memory Storage**: `ProductRepository` backed by a `HashMap` for storage with `AtomicLong` for atomic ID generation.
- **Service Layer & Business Rules**:
  - Validates that product names are not null or blank.
  - Enforces positive product prices (`price > 0`) using `BigDecimal.compareTo`.
  - Ensures non-negative stock counts (`stockQuantity >= 0`).
  - Defaults newly created products to `active = true` when null.
  - Enforces server-assigned IDs during creation and preserves IDs during updates.
- **Data Transfer Objects (DTOs)**:
  - `ProductRequest`: Clean client input contract (`name`, `description`, `price`, `stockQuantity`), preventing exposure or tampering with `id` or `active`.
  - `ProductResponse`: Clean client output representation exposing complete product details.
  - `ErrorResponse`: Standardized error DTO with `status`, `error`, and `messages` (`List<String>`) fields (created, but not yet integrated into an exception handler).
- **Declarative Bean Validation**:
  - Integrated `spring-boot-starter-validation`.
  - Enforced constraints on `ProductRequest`: `@NotBlank` on `name`, `@NotNull` & `@Positive` on `price`, and `@NotNull` & `@Min(0)` on `stockQuantity`.
  - Activated request body validation via `@Valid` on `POST` and `PUT` controller endpoints.
  - Verified with Postman: valid requests return `201 Created`, while invalid payloads fail validation and return `400 Bad Request`.
- **Manual DTO Mapping**: Explicit, decoupling conversions between DTOs and domain models directly in the controller layer.
- **Complete Product REST API**: Full CRUD endpoints with standard HTTP status codes (`201 Created`, `200 OK`, `204 No Content`, `400 Bad Request`, `404 Not Found`).

---

## Architecture

OrderFlow follows a layered architecture to maintain clear boundaries between transport handling, business logic, and data storage:

```text
CLIENT (HTTP / JSON)
        │
        ▼
CONTROLLER LAYER  ─── [ProductController, HealthController]
        │             • Maps HTTP verbs and URI routes
        │             • Validates request bodies with @Valid
        │             • Deserializes JSON into DTOs (ProductRequest)
        │             • Performs manual DTO ↔ Entity conversion
        │             • Dispatches ResponseEntity with appropriate HTTP status codes
        ▼
 SERVICE LAYER    ─── [ProductService]
        │             • Implements core business logic and programmatic validation
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

- **Controller Layer (`controller`)**: Handles HTTP requests, extracts parameters, validates incoming payloads using `@Valid`, converts DTOs to/from internal entities, and dispatches HTTP responses.
- **Service Layer (`service`)**: Houses business logic and domain validation rules. It operates independently of the transport protocol (HTTP) and coordinates changes through the repository.
- **Repository Layer (`repository`)**: Encapsulates data access. Currently manages an in-memory collection, which can be cleanly swapped for Spring Data JPA without altering service contracts.
- **Model & DTO Layer (`model`, `dto`)**:
  - `model/Product`: Internal domain representation.
  - `dto/ProductRequest`: External API contract for client write operations with Bean Validation constraints.
  - `dto/ProductResponse`: External API contract for client read responses.
  - `dto/ErrorResponse`: Standardized structure for error and validation error reporting (created, but not yet integrated into an exception handler).

---

## API Documentation

### Implemented Endpoints

| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/health` | Application health check | `200 OK` |
| `POST` | `/api/v1/products` | Create a new product | `201 Created` / `400 Bad Request` |
| `GET` | `/api/v1/products` | Retrieve all products | `200 OK` |
| `GET` | `/api/v1/products/{id}` | Retrieve a product by ID | `200 OK` (found) / `404 Not Found` (missing) |
| `PUT` | `/api/v1/products/{id}` | Update an existing product | `200 OK` (updated) / `400 Bad Request` / `404 Not Found` (missing) |
| `DELETE` | `/api/v1/products/{id}` | Delete a product by ID | `204 No Content` (deleted) / `404 Not Found` (missing) |

> **Note on Error Responses:** Payload validation constraints are enforced using `@Valid` on `POST` and `PUT` endpoints, rejecting invalid requests with `400 Bad Request`. Formatting these into a customized `ErrorResponse` structure via `@RestControllerAdvice` will be finalized in the upcoming Global Exception Handling milestone.

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

#### 2. Create Product (Valid Request)
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

#### 3. Create Product (Invalid Request)
- **Request**: `POST /api/v1/products`
```json
{
  "name": "",
  "description": "Invalid product with blank name and negative price",
  "price": -10.00,
  "stockQuantity": -5
}
```
- **Response** (`400 Bad Request`): Triggers Spring Boot validation rejection due to `@NotBlank`, `@Positive`, and `@Min(0)` constraint violations.

#### 4. Get All Products
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

#### 5. Get Product by ID
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

#### 6. Update Product
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

#### 7. Delete Product
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
│   │   │       │   ├── ErrorResponse.java
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
- **Global Exception Handling Pending**: While the `ErrorResponse` DTO has been created, centralized global exception translation via `@RestControllerAdvice` has not yet been integrated. Invalid requests currently trigger Spring Boot's default validation error response.
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
| **Phase 2 – Product Management** | Model, in-memory repository, service validation, DTOs, CRUD REST endpoints, Bean Validation (`@Valid`) | **Completed** |
| **Phase 3 – Global Exception Handling** | Implement `@RestControllerAdvice` to translate validation and business exceptions into `ErrorResponse` payloads | **Next Milestone** |
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
- **Input Validation**: Enforcing constraints declaratively at the web boundary and defensively in the service layer.
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

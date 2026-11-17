# OrderFlow – E-commerce Order Management System

<p align="center">
  <strong>An e-commerce order management backend built with Java and Spring Boot.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.2-6DB33F?logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Maven-Build%20Tool-C71A36?logo=apachemaven" alt="Maven"/>
  <img src="https://img.shields.io/badge/Tests-23%20Passing-brightgreen" alt="Tests 23 Passing"/>
  <img src="https://img.shields.io/badge/Status-In%20Development-blue" alt="In Development"/>
</p>

---

## Project Overview

**OrderFlow** is an e-commerce order management backend system designed to handle the core operations of an online shopping platform, including product catalog management, inventory control, shopping carts, checkout workflows, payment processing, and order lifecycle tracking.

The project is developed using **Java 21** and **Spring Boot**, focusing on building a maintainable, clean-architecture backend while incrementally exploring real-world software engineering concepts. Rather than implementing everything at once, OrderFlow is built step-by-step — establishing architectural boundaries, in-memory data structures, DTO contracts, declarative validations, centralized exception handling, and comprehensive automated testing first, before transitioning to persistent relational databases, concurrency controls, security, and distributed tooling.

---

## Technology Stack

| Technology | Purpose | Status |
| :--- | :--- | :--- |
| **Java 21** | Core programming language | In use |
| **Spring Boot 4.1.2** | Application framework | In use |
| **Maven** | Dependency management and build automation | In use |
| **Spring Web (MVC)** | REST API routing, request handling, and JSON serialization | In use |
| **Jakarta Bean Validation** | Declarative payload constraint validation (`spring-boot-starter-validation`) | In use |
| **Global Exception Handling** | Centralized `@RestControllerAdvice` exception and validation error handling | In use |
| **JUnit Jupiter & Mockito** | Automated unit and web layer slice testing (`MockMvc`, `@WebMvcTest`) | In use |
| **REST APIs** | HTTP-based client-server communication | In use |
| **Git & GitHub** | Version control and repository hosting | In use |
| **MySQL 8.4 LTS** | Relational database persistence | Planned |
| **Spring Data JPA & Hibernate** | ORM and repository persistence | Planned |
| **Spring Security & JWT** | Authentication, authorization, and RBAC | Planned |
| **Redis** | In-memory caching and rate limiting | Planned |
| **Docker** | Containerization and local environment setup | Planned |
| **Apache Kafka** | Asynchronous event-driven processing | Future enhancement |

---

## Current Features

- **Application Health Check**: Endpoint (`/api/v1/health`) returning server status (`UP`) and application name (`OrderFlow`).
- **Product Domain Model**: Encapsulated `Product` domain model with `id`, `name`, `description`, `price`, `stockQuantity`, and `active` attributes.
- **In-Memory Storage**: `ProductRepository` backed by a `HashMap` for storage with `AtomicLong` for atomic ID generation.
- **Service Layer & Business Rules**:
  - Validates that product names are not null or blank.
  - Enforces positive product prices (`price > 0`) using `BigDecimal.compareTo`.
  - Ensures non-negative stock counts (`stockQuantity >= 0`).
  - Defaults newly created products to `active = true` when null.
  - Enforces server-assigned IDs during creation and preserves IDs during updates.
- **Data Transfer Objects (DTOs)**:
  - `ProductRequest`: Clean client input contract (`name`, `description`, `price`, `stockQuantity`), preventing client tampering with `id` or `active`.
  - `ProductResponse`: Clean client output representation exposing complete product details.
  - `ErrorResponse`: Standardized error DTO with `status`, `error`, and `messages` (`List<String>`) fields.
- **Declarative Bean Validation**:
  - Integrated `spring-boot-starter-validation`.
  - Constraints on `ProductRequest`: `@NotBlank` on `name`, `@NotNull` & `@Positive` on `price`, and `@NotNull` & `@Min(0)` on `stockQuantity`.
  - Activated request body validation via `@Valid` on `POST` and `PUT` controller endpoints.
- **Global Exception Handling (`@RestControllerAdvice`)**:
  - Intercepts `MethodArgumentNotValidException` to extract field validation errors and format them as `fieldName: errorMessage`.
  - Intercepts `IllegalArgumentException` from service validation to return structured `ErrorResponse` payloads.
  - Returns standard HTTP `400 Bad Request` with consistent JSON payloads across all validation and argument errors.
- **Manual DTO Mapping**: Explicit, decoupling conversions between DTOs and domain models directly in the controller layer.
- **Complete Product REST API**: Full CRUD endpoints with standard HTTP status codes (`201 Created`, `200 OK`, `204 No Content`, `400 Bad Request`, `404 Not Found`).
- **Comprehensive Automated Testing**: Rigorous unit and web layer slice tests covering business logic and REST endpoints.

---

## Architecture

OrderFlow follows a layered architecture to maintain clear boundaries between transport handling, business logic, error handling, and data storage:

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
   DATA STORE     ─── [JVM Heap Memory] (MySQL 8.4 LTS planned)

─────────────────────────────────────────────────────────────────────────────
GLOBAL ERROR HANDLING ─── [GlobalExceptionHandler] (@RestControllerAdvice)
                          • Catches MethodArgumentNotValidException & IllegalArgumentException
                          • Translates exceptions into standardized ErrorResponse payloads (HTTP 400)
```

### Layer Responsibilities

- **Controller Layer (`controller`)**: Handles HTTP requests, extracts parameters, triggers Bean Validation via `@Valid`, converts DTOs to/from internal entities, and dispatches HTTP responses.
- **Service Layer (`service`)**: Houses business logic and domain validation rules. It operates independently of the transport protocol (HTTP) and coordinates changes through the repository.
- **Repository Layer (`repository`)**: Encapsulates data access. Currently manages an in-memory collection, which will be swapped for Spring Data JPA and MySQL without altering service contracts.
- **Exception Layer (`exception`)**: Centralizes application-wide error handling using `@RestControllerAdvice`, converting uncaught validation and argument exceptions into uniform `ErrorResponse` objects.
- **Model & DTO Layer (`model`, `dto`)**:
  - `model/Product`: Internal domain representation.
  - `dto/ProductRequest`: External API contract for client write operations with Bean Validation constraints.
  - `dto/ProductResponse`: External API contract for client read responses.
  - `dto/ErrorResponse`: Standardized structure for error and validation error reporting.

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

#### 3. Create Product (Invalid Request — Validation Failure)
- **Request**: `POST /api/v1/products`
```json
{
  "name": "",
  "description": "Invalid product with blank name and negative price",
  "price": -10.00,
  "stockQuantity": -5
}
```
- **Response** (`400 Bad Request`): Handled by `GlobalExceptionHandler`:
```json
{
  "status": 400,
  "error": "Validation Failed",
  "messages": [
    "name: must not be blank",
    "price: must be greater than 0",
    "stockQuantity: must be greater than or equal to 0"
  ]
}
```

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

## Automated Testing

OrderFlow emphasizes high test coverage through isolated unit testing and web-layer slice tests using `@WebMvcTest` and `MockMvc`.

### Test Suite Summary

| Test Class | Scope | Passing Tests | Failures | Errors |
| :--- | :--- | :---: | :---: | :---: |
| **`ProductServiceTest`** | Unit tests for service layer business rules & repository interactions | **12** | 0 | 0 |
| **`ProductControllerTest`** | Web-layer slice tests using `MockMvc` and `@WebMvcTest` | **10** | 0 | 0 |
| **`OrderflowApplicationTests`** | Spring Boot context load verification | **1** | 0 | 0 |
| **Total** | | **23** | **0** | **0** |

### Controller Test Coverage Details

`ProductControllerTest.java` thoroughly validates all web-layer interactions as a slice test using `@WebMvcTest` and `MockMvc`:
- **Product Creation**: Verifies valid `POST /api/v1/products` returns `201 Created` with expected JSON payload.
- **Invalid Product Request**: Verifies malformed payloads trigger validation rejection returning `400 Bad Request`.
- **Validation Error Structure**: Verifies `ErrorResponse` structure containing `status`, `error`, and field-specific error `messages`.
- **Get All Products**: Verifies `GET /api/v1/products` returns `200 OK` and a list of product responses.
- **Get Product by ID**: Verifies `GET /api/v1/products/{id}` returns `200 OK` and the matching product payload.
- **Product Not Found**: Verifies `GET /api/v1/products/{id}` returns `404 Not Found` when product does not exist.
- **Update Product**: Verifies valid `PUT /api/v1/products/{id}` returns `200 OK` with updated attributes.
- **Update Non-Existing Product**: Verifies `PUT /api/v1/products/{id}` returns `404 Not Found` for non-existent IDs.
- **Delete Product**: Verifies `DELETE /api/v1/products/{id}` returns `204 No Content` upon successful deletion.
- **Delete Non-Existing Product**: Verifies `DELETE /api/v1/products/{id}` returns `404 Not Found` when attempting to delete a missing product.

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
│   │   │       ├── exception/
│   │   │       │   └── GlobalExceptionHandler.java
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
│               ├── controller/
│               │   └── ProductControllerTest.java
│               ├── OrderflowApplicationTests.java
│               └── ProductServiceTest.java
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
- **Persistent Database Integration (Upcoming)**: Relational persistence via MySQL 8.4 LTS and Spring Data JPA + Hibernate is planned for the next development phase.
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

### 3. Run automated tests
Execute the full test suite (23 passing tests):

**Windows PowerShell:**
```powershell
.\mvnw.cmd test
```

**Linux / macOS:**
```bash
./mvnw test
```

### 4. Access endpoints
- **Health Check**: [http://localhost:8081/api/v1/health](http://localhost:8081/api/v1/health)
- **Products API**: [http://localhost:8081/api/v1/products](http://localhost:8081/api/v1/products)

### 5. Build package
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

| Phase | Milestone | Scope | Status |
| :--- | :--- | :--- | :--- |
| **Phase 1** | **Project Foundation** | Spring Boot 4.1.2, Java 21, Tomcat on port 8081, health check API | **Completed** |
| **Phase 2** | **Product Management & CRUD APIs** | Product domain model, in-memory repository, service business rules, DTOs, full CRUD REST endpoints | **Completed** |
| **Phase 3** | **Validation & Error Handling** | Jakarta Bean Validation (`@Valid`), `@RestControllerAdvice` global exception handling, `ErrorResponse` DTO | **Completed** |
| **Phase 4** | **Automated Testing** | Unit testing (`ProductServiceTest` - 12 tests), web slice testing (`ProductControllerTest` - 10 tests), context test (1 test) — 23 tests total | **Completed** |
| **Phase 5** | **Database Integration** | MySQL 8.4 LTS, Spring Data JPA, Hibernate ORM, database migrations | **Next Milestone** (Planned) |
| **Phase 6** | **Authentication & Security** | Spring Security, BCrypt, JWT, role-based access control (RBAC) | Planned |
| **Phase 7** | **Cart & Order Lifecycle** | Shopping cart management, checkout workflow, order placement, state transitions | Planned |
| **Phase 8** | **Inventory & Concurrency** | Transactions, concurrency control, atomic stock reduction, preventing overselling | Planned |
| **Phase 9** | **Payments & Resilience** | Payment gateway sandbox, idempotency keys, webhooks, refund workflows | Planned |
| **Phase 10** | **Performance & Scale** | Redis caching, API rate limiting, Apache Kafka event streaming | Planned |

---

## Development & Learning Philosophy

OrderFlow is developed as a hands-on backend engineering project with a focus on understanding core principles:
- **Foundational Understanding**: Grasping Java 21 and Spring Boot internals rather than relying on auto-configuration as a black box.
- **Architectural Discipline**: Strict separation between controller transport, service business rules, and repository persistence.
- **Contract Safety**: Using DTOs to decouple external API contracts from internal entity representations.
- **Input Validation & Error Handling**: Enforcing constraints declaratively at the web boundary and translating errors via global exception advice.
- **Testing Rigor**: Ensuring high test coverage with isolated unit and MockMvc slice tests before adding architectural complexity.
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

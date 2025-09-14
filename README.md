# SeatSync API

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/javase/21-GAA.html)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-blueviolet.svg)](https://kotlinlang.org)

A modern, high-performance backend API for a cinema seat reservation system. This project is architected following the principles of **Clean Architecture** and **Domain-Driven Design (DDD)**, featuring a secure RBAC model and built on a cutting-edge Kotlin & Java 21 stack.

This project is a personal study to explore and implement a robust, scalable, testable, and domain-focused backend system.

## Core Business Logic

This section defines the core business rules and the **Ubiquitous Language** (terms from DDD) used throughout the domain.

* **Public Catalog:** All cinema content (`Movies`, `Sessions`, `Seat Layouts`) is publicly browsable.
* **User & Roles (Context: Identity & Access):**
    * Any user can `register` an account, receiving the `ROLE_CUSTOMER`.
    * Authentication is handled via a stateless **JWT + Refresh Token** flow.
    * Permissions are enforced via **RBAC** using three distinct roles: `ROLE_CUSTOMER`, `ROLE_STAFF`, and `ROLE_ADMIN`.
* **Reservation (Context: Booking):** This is the Core Domain.
    * A **`Reservation`** is the act of booking a specific `Seat` for a specific `Session` by a `Customer`.
    * Only users with `ROLE_CUSTOMER` can create a `Reservation`.
    * Each `Reservation` is explicitly linked to the `User` who created it.
    * **Atomic Booking Rule (Core Domain Invariant):** The system enforces an atomic uniqueness constraint at the database level (`UNIQUE(session_id, seat_id)` in the `RESERVATION` table). This guarantees that a specific `Seat` can **never** be booked more than once for the same `Session`, preventing all race conditions and double-booking.
* **Staff & Admin Rules:**
    * `ROLE_STAFF` users can manage all operational data (`Movie`, `Room`, `Session`).
    * `ROLE_ADMIN` users have `STAFF` privileges plus User Management (assigning roles).

---

## Architectural Design (Clean Architecture + DDD)

This project follows a pragmatic implementation of **Clean Architecture** (by Robert C. Martin) combined with tactical **DDD** patterns. The architecture is built on the **Dependency Rule**: all dependencies must point *inwards*.

This isolates our core business logic from frameworks and implementation details, resulting in a system that is testable, flexible, and framework-independent at its core.

The project code is structured into three primary layers (packages):

### 1. `domain` (The Core - Center Circle)
This is the heart of the application. It contains all core business logic and models.
* **Contents:** All tactical DDD patterns live here:
    * **Aggregates, Entities, and Value Objects:** The domain models (`Movie`, `Session`, `Reservation`, `User`, `Role`, etc.) that represent our business.
    * **Repository Interfaces (Ports):** Abstract contracts defining what the application needs from persistence (e.g., `ReservationRepositoryPort`, `UserRepositoryPort`), but *not how* it's implemented.
* **Dependencies:** This layer has **ZERO dependencies** on any framework (no Spring, no JPA). It is pure Kotlin.

### 2. `application` (Use Cases - Second Circle)
This layer contains all application-specific logic and orchestrates the domain.
* **Contents:**
    * **Use Cases / Application Services:** (e.g., `CreateReservationUseCase`, `ListMoviesUseCase`, `RegisterUserUseCase`). These classes orchestrate the flow of logic.
    * **Request/Response DTOs:** Simple, plain data structures used to communicate with the application layer.
* **Dependencies:** Depends *only* on the `domain` layer. It calls the domain entities and uses the repository *interfaces* (Ports). This layer does not know *how* data is saved or *how* the use case is triggered (e.g., by HTTP or a message queue).

### 3. `infrastructure` (Frameworks & Drivers - Outer Circle)
This is the outermost layer containing all implementation details, frameworks, and tools. This layer acts as the "glue" that implements the contracts defined in the inner layers.
* **Contents:**
    * **Adapters:**
        * **Web Adapters (Controllers):** Spring `@RestController`s that handle HTTP requests, map them to Application DTOs, call the Use Cases, and map responses back to JSON.
        * **Persistence Adapters (Gateways):** Implementations of the repository interfaces from the `domain` layer, using **Spring Data JPA** and `@Repository` annotations. This is where the translation from the domain model to the persistence model (JPA `@Entity`) occurs.
    * **Frameworks:** Spring Boot itself, **Spring Security** (configuration), **Flyway** (migration scripts), **Testcontainers** (test configs), **Docker** (`docker-compose.yml`), and the main application entrypoint (`Application.kt`).
* **Dependencies:** This layer depends on `application` and `domain` to wire everything together.

---

## Technology Stack & Architectural Decisions

This is the list of tools and frameworks used primarily within the **Infrastructure Layer** to implement our architecture.

* **Language:** **Kotlin**
* **Framework:** **Spring Boot 3.x**
* **Runtime:** **Java 21 (LTS)**
    * **Key Config:** Enabled **Virtual Threads (Project Loom)** (`spring.threads.virtual.enabled=true`) to achieve massive scalability for I/O-bound operations (like JPA calls) while using a simple, imperative coding model.
* **Security:** **Spring Security 6:** Implements the `infrastructure` adapters for authentication (JWT flow) and authorization (RBAC rules).
* **Database:** **PostgreSQL**
* **DB Migrations:** **Flyway:** Manages the `infrastructure` task of versioning the database schema.
* **Persistence:** **Spring Data JPA (Hibernate):** Serves as the persistence *adapter* implementing the domain's repository ports.
* **Testing:** **Testcontainers:** The core of our integration testing strategy, providing a high-fidelity test environment by running our persistence adapters and Flyway migrations against a real, temporary Postgres container.
* **Monitoring:** **Spring Boot Actuator**
* **Documentation:** **Springdoc OpenAPI**
* **Build Tool:** **Gradle (Kotlin DSL)** (`build.gradle.kts`)

---

## Getting Started (Installation Guide)

Follow these instructions to get a local copy up and running for development and testing.

### Prerequisites

* **JDK 21** (or newer)
* **Docker Desktop** (or Docker Engine + Docker Compose)
* Git

### 1. Clone the Repository

```bash
git clone [https://github.com/andreramosdovale/seatsync-api.git](https://github.com/andreramosdovale/seatsync-api.git)
cd seatsync-api
````

### 2\. Configure the Environment

This project uses Docker Compose to easily start the required PostgreSQL database. (A `docker-compose.yml` file should be present in the root of the repository).

1.  Start the database container:

    ```bash
    docker-compose up -d
    ```

    This will start a PostgreSQL server on `localhost:5432` with the user, password, and database name defined in the `docker-compose.yml` file.

2.  Create a local configuration file. This project is set up to read from an `application-local.properties` file when the `local` profile is active. Create this file in `src/main/resources/` (this path will likely be within the `infrastructure` module).

3.  Add the following properties to your new `application-local.properties` file:

    ```properties
    # Database Connection (match the values from your docker-compose.yml)
    spring.datasource.url=jdbc:postgresql://localhost:5432/seatsync_db
    spring.datasource.username=youruser
    spring.datasource.password=yourpass

    # Flyway will find the DB and automatically run all pending migrations on startup.
    spring.flyway.baseline-on-migrate=true

    # The magic switch! Tell Spring Boot to use Java 21's Virtual Threads for every web request.
    spring.threads.virtual.enabled=true

    # JWT Configuration (generate your own secure 64-byte random string)
    jwt.secret.key=REPLACE_ME_WITH_A_VERY_LONG_AND_SECURE_BASE64_RANDOM_KEY
    jwt.refresh.expiration-days=7
    jwt.access.expiration-minutes=15
    ```

### 3\. Run the Application

Once the database is running and your configuration is set, you can run the application using the Gradle wrapper, explicitly activating the `local` profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

The API will start on `http://localhost:8080`.

You can access the **interactive API documentation (Swagger UI)** generated by Springdoc at:
**[http://localhost:8080/swagger-ui.html](https://www.google.com/search?q=http://localhost:8080/swagger-ui.html)**

-----

## API Documentation Specification

This documentation details every endpoint (the entry point to the Infrastructure layer), its payloads (DTOs for the Application layer), and its response codes.

### 1\. Auth Module

Endpoints responsible for registration, login, and token management.

#### `POST /auth/register`

* **Description:** Registers a new user. Calls the `RegisterUserUseCase`.
* **Authorization:** `PUBLIC`.
* **Request Body (Payload):**
  ```json
  {
    "name": "string (required, min: 3)",
    "email": "string (required, valid email format, unique in the system)",
    "password": "string (required, min: 8 characters)"
  }
  ```
* **Responses:**
  * `201 Created`: User successfully created.
  * `400 Bad Request`: Validation error.
  * `409 Conflict`: The provided email already exists.

#### `POST /auth/login`

* **Description:** Authenticates an existing user and returns a pair of tokens.
* **Authorization:** `PUBLIC`.
* **Request Body (Payload):**
  ```json
  {
    "email": "string",
    "password": "string"
  }
  ```
* **Responses:**
  * `200 OK`: Login successful.
    ```json
    {
      "accessToken": "ey...",
      "refreshToken": "ey..."
    }
    ```
  * `401 Unauthorized`: Invalid credentials.

#### `POST /auth/refresh`

* **Description:** Generates a new Access Token using a valid Refresh Token.
* **Authorization:** `PUBLIC`.
* **Request Body (Payload):**
  ```json
  {
    "refreshToken": "string (The valid refresh token obtained from login)"
  }
  ```
* **Responses:**
  * `200 OK`: New access token generated.
  * `401 Unauthorized`: The Refresh Token is invalid, expired, or revoked.

-----

### 2\. Public Module (Catalog)

Public-facing endpoints for browsing the cinema catalog.

#### `GET /api/movies`

* **Description:** Returns a paginated list of all currently available movies.
* **Authorization:** `PUBLIC`.
* **Query Parameters:**
  * `page` (optional, default: 0), `size` (optional, default: 10).
* **Responses:**
  * `200 OK`: Returns the pagination object (`Page<MovieDTO>`).

#### `GET /api/movies/{movieId}/sessions`

* **Description:** Returns a paginated list of all available sessions for a specific movie.
* **Authorization:** `PUBLIC`.
* **Path Parameter:**
  * `movieId` (UUID): The ID of the movie.
* **Responses:**
  * `200 OK`: Returns the pagination object (`Page<SessionDTO>`).
  * `404 Not Found`: No movie found with the provided `movieId`.

#### `GET /api/sessions/{sessionId}/layout-cadeiras`

* **Description:** Returns the complete seat layout for a specific session.
* **Authorization:** `PUBLIC`.
* **Path Parameter:**
  * `sessionId` (UUID): The ID of the session.
* **Responses:**
  * `200 OK`: Returns the layout object.
  * `404 Not Found`: No session found with the provided `sessionId`.

-----

### 3\. Customer Module (Reservations)

Secured endpoints for authenticated customers.

#### `POST /api/reservas`

* **Description:** Creates a new reservation. Calls the `CreateReservationUseCase`.
* **Authorization:** `ROLE_CUSTOMER` (Requires Bearer Access Token).
* **Request Body (Payload):**
  ```json
  {
    "id_sessao": "UUID (required)",
    "id_cadeira": "UUID (required)"
  }
  ```
* **Responses:**
  * `201 Created`: Reservation created successfully.
  * `400 Bad Request`: Validation error.
  * `401 Unauthorized`: Access token is missing or invalid.
  * `403 Forbidden`: Authenticated user does not have the `ROLE_CUSTOMER` role.
  * `404 Not Found`: The provided `id_sessao` or `id_cadeira` does not exist.
  * `409 Conflict`: The selected seat is already reserved for this session (Domain rule violation).

#### `GET /api/me/reservas`

* **Description:** Returns a list of all reservations made by the currently authenticated user.
* **Authorization:** `ROLE_CUSTOMER` (Requires Bearer Access Token).
* **Responses:**
  * `200 OK`: Returns an array (or page object) of the user's reservations.
  * `401 Unauthorized`: Access token is missing or invalid.
  * `403 Forbidden`: User does not have the `ROLE_CUSTOMER` role.

-----

### 4\. Staff Module (Operational Management)

Secured endpoints for cinema staff.

#### `POST /api/staff/filmes`

* **Description:** Adds a new movie to the system catalog. Calls the `CreateMovieUseCase`.
* **Authorization:** `ROLE_STAFF` (or `ROLE_ADMIN`).
* **Request Body (Payload):**
  ```json
  {
    "titulo": "string",
    "sinopse": "string (text)",
    "duracao_minutos": "integer",
    "classificacao_indicativa": "string (e.g., 'PG-13', 'R', 'G')"
  }
  ```
* **Responses:**
  * `201 Created`: Movie created.
  * `400 Bad Request`: Validation error.
  * `401 Unauthorized`: Token missing/invalid.
  * `403 Forbidden`: User is not `STAFF` or `ADMIN`.

*(Note: Similar endpoints exist for `POST /api/staff/salas` and `POST /api/staff/sessoes`)*

-----

### 5\. Admin Module (System Management)

Highest-level endpoints for user management.

#### `GET /api/admin/usuarios`

* **Description:** Lists all registered users in the system.
* **Authorization:** `ROLE_ADMIN`.
* **Responses:**
  * `200 OK`: Returns the paginated list of users.
  * `401 Unauthorized`: Token missing/invalid.
  * `403 Forbidden`: User is not an `ADMIN`.

#### `PUT /api/admin/usuarios/{userId}/role`

* **Description:** Changes a user's role (e.g., promotes a `CUSTOMER` to `STAFF`). Calls `AssignRoleUseCase`.
* **Authorization:** `ROLE_ADMIN`.
* **Path Parameter:**
  * `userId` (UUID): The ID of the user to be modified.
* **Request Body (Payload):**
  ```json
  {
    "newRole": "string (e.g., 'ROLE_STAFF')"
  }
  ```
* **Responses:**
  * `200 OK`: Role successfully changed.
  * `400 Bad Request`: The provided role name is not a valid role.
  * `401 Unauthorized`: Token missing/invalid.
  * `403 Forbidden`: User is not an `ADMIN`.
  * `404 Not Found`: The `userId` was not found.

-----

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.

```
```
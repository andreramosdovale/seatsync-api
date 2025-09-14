# Project Technical Brief: SeatSync API

This document serves as the single source of truth for the development of the **SeatSync API**. It contains all defined business rules, architectural decisions, and technical specifications.

## 1. Project Overview

The **SeatSync API** is a backend for a cinema seat reservation system. The project is conceived as an in-depth study to implement a robust, concurrent-safe, and modern system utilizing a high-performance stack (Kotlin + Java 21 with Virtual Threads).

The system must manage movies, sessions, and seat layouts while securing transactional operations (like reservations) and managing different user permission levels via RBAC.

## 2. Core Business Logic & Security Model (RBAC)

The system core is based on the following business and security rules:

### Authentication
* The system uses a **Stateless JWT (JSON Web Tokens)** authentication flow.
* A successful login provides a short-lived **Access Token** (for accessing the API) and a long-lived **Refresh Token** (for obtaining new Access Tokens).

### Access Rules (RBAC)
There are three distinct permission levels (Roles):

1.  **`ROLE_CUSTOMER`:**
    * This is the default role for any user who registers (`POST /auth/register`).
    * **Can:** View the entire catalog (movies, sessions, layouts), log in, manage their own profile, **create new reservations**, and **view their own reservation history**.
    * **Cannot:** View reservations of other users or manage cinema data.

2.  **`ROLE_STAFF`:**
    * A role assigned by an Admin.
    * **Can:** Do everything `ROLE_CUSTOMER` can.
    * **Can (Additionally):** Manage all operational cinema data: create, update, and delete `Movies`, `Rooms`, `Seats`, and `Sessions`.

3.  **`ROLE_ADMIN` (Superuser):**
    * The highest access level.
    * **Can:** Do everything `ROLE_STAFF` can.
    * **Can (Additionally):** Manage the system, primarily by **managing users** and **assigning/changing the Roles** of other users (e.g., promoting a `CUSTOMER` to `STAFF`).

### Critical Business Rule: Reservation Atomicity
The most important business rule is ensuring a seat is not sold twice for the same session.
* This is resolved at the lowest, most secure level: the **Database**.
* The `RESERVATION` table **must** have a composite `UNIQUE` constraint on the fields `(session_id, seat_id)`.
* Any concurrent attempt to insert a second entry for the same pair will result in an integrity constraint violation (a `409 Conflict` error), guaranteeing 100% data consistency.

## 3. Architecture & Technology Stack

This project is defined by the following technology and architectural choices:

* **Architecture:** **Clean Architecture** combined with tactical **Domain-Driven Design (DDD)** patterns. The code is separated into layers (`domain`, `application`, `infrastructure`) and follows the Dependency Rule (dependencies point only inwards).
* **Language:** **Kotlin** (Preferred over Java for its conciseness, null-safety, and functional syntax).
* **Framework:** **Spring Boot 3.x+**.
* **Runtime:** **Java 21 (LTS)**.
    * **Critical Decision:** The system is configured to use **Virtual Threads (Project Loom)** (`spring.threads.virtual.enabled=true`) for massive I/O scalability (JPA) using simple, imperative code.
* **Database:** **PostgreSQL**.
* **DB Migrations:** **Flyway** (Declarative, version-controlled schema management via SQL scripts).
* **Persistence:** **Spring Data JPA (Hibernate)**.
* **Security:** **Spring Security 6**.
    * **Critical Decision (JWT):** The JWT implementation (token creation/validation) is handled using official Spring modules. This requires two dependencies from the Initializr: `Spring Security` (for the core) and **`OAuth2 Resource Server`** (which provides JWT validation filters and the underlying Nimbus JOSE library for token handling).
* **Build Tool:** **Gradle (with Kotlin DSL)** (Using `build.gradle.kts`).
* **Testing:** **Testcontainers** (For high-fidelity integration tests against a real, temporary Postgres container running Flyway migrations).
* **API Documentation:** **Springdoc OpenAPI** (For automatic Swagger UI generation).
* **Monitoring:** **Spring Boot Actuator**.
* **Dev Environment:** **Docker** and **Docker Compose**.

## 4. Data Model (ER Diagram)

This project uses JPA entities defined in the `infrastructure` layer. As you are using **IntelliJ Ultimate**, you do not need a static Mermaid diagram. You can (and should) use the built-in designer which generates a live diagram directly from your code.

**How to view your Data Model:**

Once you have written your `@Entity` classes (like `User`, `Reservation`, `Session`, etc.) in your `infrastructure` module:

1.  Open the **Persistence** tool window (usually in the right-hand sidebar).
2.  Your Spring Boot module should be recognized as a persistence unit.
3.  Right-click the persistence unit and select **Show Diagram** (or **JPA Designer**).

This will generate a complete, accurate, and interactive ER diagram showing all your entities and their `@OneToMany`, `@ManyToOne`, and `@ManyToMany` relationships, which serves as your live data model.

## 5. API Documentation Specification

This documentation details every endpoint, payload, and response code.

---

### 1. Auth Module

Endpoints responsible for registration, login, and token management.

#### `POST /auth/register`

* **Description:** Registers a new user in the system. By default, the new user receives the `ROLE_CUSTOMER` role.
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
    * `201 Created`: User successfully created. Returns the new user object (without password).
    * `400 Bad Request`: Validation error (e.g., invalid email, password too short).
    * `409 Conflict`: The provided email already exists in the database.

#### `POST /auth/login`

* **Description:** Authenticates an existing user and returns a pair of tokens (Access and Refresh).
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
    * `401 Unauthorized`: Invalid credentials (email or password incorrect).

#### `POST /auth/refresh`

* **Description:** Generates a new Access Token using a valid Refresh Token.
* **Authorization:** `PUBLIC` (but requires a valid Refresh Token in the payload).
* **Request Body (Payload):**
    ```json
    {
      "refreshToken": "string (The valid refresh token obtained from login)"
    }
    ```
* **Responses:**
    * `200 OK`: New access token generated.
        ```json
        {
          "accessToken": "ey...",
          "refreshToken": "ey... (may or may not return a new refresh token)"
        }
        ```
    * `401 Unauthorized`: The Refresh Token is invalid, expired, or has been revoked.

---

### 2. Public Module (Catalog)

Public-facing endpoints for browsing the cinema catalog.

#### `GET /api/movies`

* **Description:** Returns a paginated list of all currently available movies.
* **Authorization:** `PUBLIC`.
* **Query Parameters:**
    * `page` (optional, default: 0): Page number.
    * `size` (optional, default: 10): Page size.
* **Responses:**
    * `200 OK`: Returns the pagination object (`Page<Movie>`).

#### `GET /api/movies/{movieId}/sessions`

* **Description:** Returns a paginated list of all available sessions for a specific movie.
* **Authorization:** `PUBLIC`.
* **Path Parameter:**
    * `movieId` (UUID): The ID of the movie.
* **Query Parameters:** (Same as above: `page`, `size`).
* **Responses:**
    * `200 OK`: Returns the pagination object (`Page<Session>`).
    * `404 Not Found`: No movie found with the provided `movieId`.

#### `GET /api/sessions/{sessionId}/layout-cadeiras`

* **Description:** Returns the complete seat layout for a specific session, detailing the status of each seat (available or occupied).
* **Authorization:** `PUBLIC`.
* **Path Parameter:**
    * `sessionId` (UUID): The ID of the session.
* **Responses:**
    * `200 OK`: Returns the layout object (room details + array of seats with status).
    * `404 Not Found`: No session found with the provided `sessionId`.

---

### 3. Customer Module (Reservations)

Secured endpoints for authenticated customers.

#### `POST /api/reservas`

* **Description:** Creates a new reservation for the authenticated user, linking them to a specific seat in a specific session. This is the primary atomic transaction.
* **Authorization:** `ROLE_CUSTOMER` (Requires Bearer Access Token).
* **Request Body (Payload):**
    ```json
    {
      "id_sessao": "UUID (required)",
      "id_cadeira": "UUID (required)"
    }
    ```
* **Responses:**
    * `201 Created`: Reservation created successfully. Returns the complete reservation object.
    * `400 Bad Request`: Validation error (IDs not provided or malformed).
    * `401 Unauthorized`: Access token is missing or invalid.
    * `403 Forbidden`: Authenticated user does not have the `ROLE_CUSTOMER` role.
    * `404 Not Found`: The provided `id_sessao` or `id_cadeira` does not exist.
    * `409 Conflict`: The selected seat (`id_cadeira`) is already reserved for this session (`id_sessao`). (Triggered by the database UNIQUE constraint).

#### `GET /api/me/reservas`

* **Description:** Returns a list of all reservations made by the currently authenticated user.
* **Authorization:** `ROLE_CUSTOMER` (Requires Bearer Access Token).
* **Query Parameters:** Can include pagination (`page`, `size`).
* **Responses:**
    * `200 OK`: Returns an array (or page object) of the user's reservations.
    * `401 Unauthorized`: Access token is missing or invalid.
    * `403 Forbidden`: User does not have the `ROLE_CUSTOMER` role.

---

### 4. Staff Module (Operational Management)

Secured endpoints for cinema staff.

#### `POST /api/staff/filmes`

* **Description:** Adds a new movie to the system catalog.
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
    * `201 Created`: Movie created. Returns the movie object.
    * `400 Bad Request`: Validation error.
    * `401 Unauthorized`: Token missing/invalid.
    * `403 Forbidden`: User is not `STAFF` or `ADMIN`.

*(Note: Similar endpoints exist for `POST /api/staff/salas` and `POST /api/staff/sessoes`)*

---

### 5. Admin Module (System Management)

Highest-level endpoints for user management.

#### `GET /api/admin/usuarios`

* **Description:** Lists all registered users in the system.
* **Authorization:** `ROLE_ADMIN`.
* **Query Parameters:** Can include pagination (`page`, `size`).
* **Responses:**
    * `200 OK`: Returns the paginated list of users.
    * `401 Unauthorized`: Token missing/invalid.
    * `403 Forbidden`: User is not an `ADMIN`.

#### `PUT /api/admin/usuarios/{userId}/role`

* **Description:** Changes a user's role (e.g., promotes a `CUSTOMER` to `STAFF`).
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
    * `200 OK`: Role successfully changed. Returns the updated user object.
    * `400 Bad Request`: The provided role name is not a valid role.
    * `401 Unauthorized`: Token missing/invalid.
    * `403 Forbidden`: User is not an `ADMIN`.
    * `404 Not Found`: The `userId` was not found.

---

## 6. Critical Implementation & Configuration Notes

Key points reflecting your final setup:

1.  **Enable Virtual Threads:** This is essential. You must add `spring.threads.virtual.enabled=true` to your main `application.properties` file.

2.  **Simplified Profile Configuration:** You are using the `default` profile. Therefore, **all** configuration (database connection, JWT secrets, VT flag) must go directly into the main config file: `src/main/resources/application.properties`. No `application-local.properties` file is needed.

3.  **Development Environment (Simplified Flow):**
    * (1) Run `docker-compose up -d` to start the Postgres container.
    * (2) Ensure all configurations are set in `application.properties`. It is assumed your configuration points to the database **`seatsync_db`** to match the project name (e.g., `spring.datasource.url=jdbc:postgresql://localhost:5432/seatsync_db`).
    * (3) The command to run the application is simply: `./gradlew bootRun`.

4.  **Kotlin Gradle Plugins:** Remember to configure the `kotlin-jpa` and `kotlin-allopen` plugins in your `build.gradle.kts` to allow Hibernate to work with Kotlin's `data classes` (the Spring Initializr typically handles this).

5.  **Jackson Kotlin JSON:** The `com.fasterxml.jackson.module:jackson-module-kotlin` dependency is required for Jackson to correctly serialize/deserialize Kotlin `data classes`.

6.  **Spring Initializr Dependencies (Final List):** The required list to implement this architecture is:
    * `Spring Web`
    * `Spring Security`
    * **`OAuth2 Resource Server`** (This is key for the JWT-only implementation)
    * `Spring Data JPA`
    * `Flyway Migration`
    * `PostgreSQL Driver`
    * `Validation`
    * `Spring Boot Actuator`
    * `Testcontainers`
    * `Spring Boot DevTools`
    * (And ensure the project is set to Kotlin & Gradle - Kotlin DSL)
````
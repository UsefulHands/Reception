# Reception Management System

This project is a full-stack Reception Management System, feature based, built with Spring Boot 3 and Angular. It features a robust security layer with JWT, containerized PostgreSQL, automated Audit Logs, and comprehensive API documentation via Swagger.

---

## Prerequisites

Before you begin, ensure you have the following installed:
* Java JDK 17 or newer
* Node.js (v18+ recommended) & npm
* Docker Desktop
* Maven (integrated in most IDEs)

---

## 1. Environment Configuration

The application requires specific environment variables to manage sensitive data. These are not stored in the repository for security reasons.

1. Create a .env file in the root directory.
2. Add the following keys:
   DB_USER=your_postgres_username
   DB_PASSWORD=your_postgres_password
   JWT_SECRET_KEY=your_super_secret_key

---

## 2. Docker and Database Setup

The project uses a Dockerized PostgreSQL instance to ensure environment consistency.

1. Open your terminal in the root folder.
2. Run the following command to start the database and the application:
   docker-compose up --build -d
3. To stop all services and wipe volumes (for a fresh start):
   docker-compose down -v
4. To monitor backend logs:
   docker logs -f reception_backend

The database is available at localhost:5432 and persists data in a local volume named postgres_data.

---

## 3. Frontend Build Instructions

The Spring Boot backend serves the Angular frontend as static content.

1. Navigate to: src/main/frontend
2. Run: npm install
3. Run: ng build --output-path=../resources/static --delete-output-path=false

This process populates the src/main/resources/static folder with the necessary assets for the Spring Boot application.

---

## 4. Running the Application

* Using IDE: Run the ReceptionApplication.java file.
* Using Maven: Run mvn spring-boot:run
* Access the App:
   * UI: http://localhost:8080
   * API Documentation (Swagger): http://localhost:8080/swagger-ui/index.html

---

## Technical Highlights

* Security: Stateless authentication using JWT via JwtAuthenticationFilter. Security secrets are managed through environment variables.
* Audit Logs: Integrated JPA Auditing to automatically track created_by, created_at, updated_by, and updated_at fields for all database entities.
* Console logs: Integrated console logs via @Slf4j.
* API Versioning: All REST endpoints are prefixed with /api/v1.
* Docker: The program itself and its database is running on docker.
* Postgres: Selected Postgres for database.
* Angular: Angular is selected for frontend due to its compatibility with my system.
* Global Exception Handling: Created an exception handling mechanism for better understanding of HTTPs.
* Swagger: Implemented Swagger for easing backend developments.
* Flyway: Used to initialization of db migrations.
* Architecture: Organized into common (security/config) and features (business logic) packages for better maintainability.
* Testing: Backend unit and integration tests can be executed using mvn test.

---

## Git Policies

The following are strictly excluded via .gitignore to maintain security and keep the repository clean:
* .env (Environment secrets)
* target/ (Java build artifacts)
* node_modules/ (Frontend dependencies)
* src/main/resources/static/* (Auto-generated frontend assets)
* IDE specific configurations (.idea, .vscode)
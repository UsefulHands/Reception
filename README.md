# Reception Management System

This project is a full-stack Reception Management System built with **Spring Boot 3** and **Angular**. It features a robust security layer with **JWT**, containerized **PostgreSQL**, and automated API documentation via Swagger.

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

1. Create a **.env** file in the root directory.
2. Add the following keys:
    - DB_USER=your_postgres_username
    - DB_PASSWORD=your_postgres_password
    - JWT_SECRET_KEY=your_super_secret_key_at_least_32_chars

---

## 2. Database Setup
The project uses a Dockerized PostgreSQL instance to ensure environment consistency.

1. Open your terminal in the root folder.
2. Run: **docker compose up -d**

The database will be available at localhost:5432 and persists data in a local volume named postgres_data.

---

## 3. Frontend Build Instructions
The Spring Boot backend serves the Angular frontend as static content.

1. Navigate to: **src/main/frontend**
2. Run: **npm install**
3. Run: **ng build --output-path=../resources/static --delete-output-path=false**

This process populates the src/main/resources/static folder with the necessary assets.

---

## 4. Running the Application
1. **Using IDE:** Run the ReceptionApplication.java file.
2. **Using Maven:** Run **mvn spring-boot:run**
3. **Access the App:** - UI: http://localhost:8080
    - API Documentation (Swagger): http://localhost:8080/swagger-ui.html

---

## Technical Highlights
* **Security:** Stateless authentication using JWT via JwtAuthenticationFilter.
* **API Versioning:** All REST endpoints are prefixed with /api/v1.
* **Architecture:** Organized into common (security/config) and features (business logic) packages.

## Git Policies
The following are strictly excluded via .gitignore:
* .env (Environment secrets)
* target/ (Java build artifacts)
* src/main/resources/static/* (Auto-generated frontend assets)
* IDE specific configurations (.idea, .vscode)
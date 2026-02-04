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

Open ReceptionApplication environments and put this environments

1. DB_USER=reception_admin;
2. DB_PASSWORD=your_password;
3. DB_NAME=reception_db;
4. SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/reception_db;
5. SPRING_DATASOURCE_USERNAME=reception_admin;
6. SPRING_DATASOURCE_PASSWORD=same_with_DB_PASSWORD;
7. JWT=your_unique_jwt_key;

As a second step you have to create a .env file in the root directory.
And put the exact same environments as a plain string.

---

## 2. Docker and Database Setup

The project uses a Dockerized PostgreSQL instance to ensure environment consistency.

1. After downloading docker desktop, open your terminal in the root folder.
2. Run the following command to start the database and the application:
   docker-compose up --build -d
3. To stop all services and wipe volumes (for a fresh start, use to reset):
   docker-compose down -v
4. To monitor backend logs:
   docker logs -f reception_backend

The database is available at localhost:5432 and persists data in a local volume named postgres_data.
Be careful. Port 5432 and 8080 should be good to go for this project.

---

## 3. Frontend Build Instructions

The Spring Boot backend serves the Angular frontend as static content.

1. Navigate to: src/main/frontend
2. Run: npm install
3. Run npm install bootswatch
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
* Flyway: Database migrations are managed via Flyway for version control.
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

## Important

Very important
* You have to donwload docker desktop
* You have to install npm inside terminal of src/main/frontend
* After proper installs, create your .env file and change the system environments due to this file (Section 1)
* port 8082 is for docker-app.
* port 8280 is local-app.
* port 4200 is frontend-app.
* port 5432 is postgres-db.

## Running the project

### 1.Local Run
* If docker and npm install is ok,
* Frontend: Open directory src/main/frontend and use command (npm run build), after use command (ng serve) and check url (http://localhost:4200/)
* Db : You still have to use command (docker compose --build -d db) to acquire a db.
* Backend: Run the project and check url (http://localhost:8080/swagger-ui/index.html)

### 2.Docker-Container
* If installations are ok,
1. Open directory src/main/frontend and use command (npm run build). This will put the frontend inside src/main/resources/static directory.
2. use command "./mvnw clean package -DskipTests" inside project directory. This will create .jar file in /target directory.
* I have set a DockerFile for you :). Just run the command (docker compose up --build -d)
* check the url (http://localhost:8082/)
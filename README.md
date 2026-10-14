# Campus Fix - Backend (Spring Boot)

REST API for **Campus Fix: A Web-Based Campus Complaint Tracking System**.
Students report campus problems; admins assign and resolve them.

**Stack:** Java 17, Spring Boot 3.3, Spring Security + JWT, Spring Data JPA, H2 / MySQL, Maven

## Run

Requirements: JDK 17+ and Maven (or open the folder in IntelliJ / VS Code / Eclipse and run `CampusFixApplication`).

```bash
mvn spring-boot:run        # API on http://localhost:5000
```

It works with **no database setup** (uses a local H2 file database in `./data`).
To use MySQL, edit `src/main/resources/application.properties` (instructions inside).

Settings can be changed in `application.properties` or with environment variables:
`PORT`, `JWT_SECRET`, `ADMIN_CODE`, `CLIENT_URL`.

## Creating an admin
Register with an extra `adminCode` equal to `app.admin-code` (default `anurag-admin-2026`).
The frontend's register page has an optional "Admin code" box for this.

## API

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | /api/auth/register | public | Create account |
| POST | /api/auth/login | public | Login, returns JWT |
| GET | /api/auth/me | user | Current user |
| GET | /api/complaints/categories | user | Category list |
| POST | /api/complaints | student | Submit complaint (multipart, optional `image`) |
| GET | /api/complaints/mine | student | My complaints |
| GET | /api/complaints | admin | All complaints (`?status=&category=`) |
| GET | /api/complaints/stats | admin | Counts by status |
| PATCH | /api/complaints/{id} | admin | Update status / assignedTo / adminNote |
| DELETE | /api/complaints/{id} | owner (Pending) / admin | Delete |

Send the token as `Authorization: Bearer <token>`.

## Project structure

```
src/main/java/com/campusfix/
  CampusFixApplication.java     entry point
  config/                       SecurityConfig (JWT, CORS, roles), WebConfig (serves /uploads)
  model/                        User, Complaint (JPA entities)
  repository/                   UserRepository, ComplaintRepository
  security/                     JwtUtil, JwtAuthFilter
  controller/                   AuthController, ComplaintController, GlobalExceptionHandler
  service/                      FileStorageService (image upload)
  dto/                          request/response objects
src/main/resources/application.properties
uploads/                        stored complaint images
```

## How it works (for viva)
1. User logs in -> server checks BCrypt password hash -> returns a signed JWT.
2. React sends the JWT on every request; `JwtAuthFilter` identifies the user and role.
3. `SecurityConfig` protects admin endpoints with `hasRole("ADMIN")`.
4. Complaints are saved with JPA; images are stored on disk and served from `/uploads`.

## Deploy
Render / Railway (build: `mvn clean package`, run: `java -jar target/campusfix-backend-1.0.0.jar`).
Use a hosted MySQL and set the datasource variables there.

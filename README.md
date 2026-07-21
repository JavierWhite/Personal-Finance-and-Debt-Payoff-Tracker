# Personal Finance and Debt Payoff Tracker

A Spring Boot microservices prototype for manually tracking user accounts, gross income, debts, debt payments, savings goals, retirement accounts, and monthly financial snapshots.

The project includes REST APIs, a Postman collection, centralized configuration, Docker Compose deployment, and a small web dashboard for account, income, and debt operations.

## Checkpoint 2 security features

The application now includes the Week 11 security requirements:

1. User registration
2. Authentication with username and password
3. JWT token creation after login or registration
4. Authorization with user and admin roles
5. Password reminder and reset token flow
6. BCrypt password hashing
7. Protected REST endpoints across the business services

Development users:

```text
Regular user:
Username: javier
Password: Password123!

Admin user:
Username: admin
Password: Password123!
```

The password reminder endpoint returns the reset token in the response for classroom demonstration. In a production system, the token would be sent by email instead.

## Services

| Service | Port | Responsibility |
|---|---:|---|
| Frontend Service | 8080 | Static web dashboard |
| Config Service | 8888 | Central dev and prod configuration |
| User Account Service | 8081 | Accounts, registration, login, password reset, profiles, and income records |
| Debt Service | 8082 | Debt accounts and payments |
| Savings Service | 8083 | Savings goals and contributions |
| Retirement Service | 8084 | Retirement accounts, contributions, and projections |
| Analytics Service | 8085 | Monthly snapshots and chart-ready data |
| MySQL | 3306 | Five service-owned databases |

## Architecture

Each business microservice uses the required stack:

1. JPA entities saved in MySQL
2. Spring Data repositories
3. Service classes for business logic
4. REST controllers for CRUD operations

The shared `security-common` module provides JWT validation and helper authorization logic. Each protected service imports that module and applies stateless Spring Security.

## Authorization model

The system uses two roles:

```text
USER
ADMIN
```

Authorization rules:

- A regular user can access only records that match their own `userId`.
- An admin can list all users and access records across users.
- Public endpoints are limited to registration, login, password reminder, password reset, and health checks.
- All tracker endpoints require a Bearer token.

Example protected request header:

```text
Authorization: Bearer <jwt-token>
```

## Web dashboard features

Open this URL after the containers start:

```text
http://localhost:8080
```

The dashboard supports:

- Register a new account
- Sign in with username and password
- Request a password reminder token
- Reset a password with a reset token
- Update profile details and account status
- Delete an account
- Add and remove gross income records
- Store income as monthly or yearly gross income
- Display normalized monthly and yearly income totals
- Add and remove debt accounts
- Record debt payments
- Display total debt and minimum payments

## Run with Docker Compose

From the repository root:

```powershell
docker compose up --build
```

To start with a clean development database:

```powershell
docker compose down -v
docker compose up --build
```

## Verify services

```text
http://localhost:8888/actuator/health
http://localhost:8080/actuator/health
http://localhost:8081/actuator/health
http://localhost:8082/actuator/health
http://localhost:8083/actuator/health
http://localhost:8084/actuator/health
http://localhost:8085/actuator/health
```

Each service should return:

```json
{
  "status": "UP"
}
```

## Dev and prod profiles

The Config Service stores dev and prod configuration files for the business services and frontend service.

Dev profile:

- `ddl-auto=update`
- SQL logging enabled
- Seed data enabled
- Local default database credentials
- Development JWT secret and token lifetime defaults

Prod profile:

- `ddl-auto=validate`
- SQL logging disabled
- Seed data disabled
- Database credentials supplied through environment variables
- JWT secret supplied through environment variables

Run dev in Windows PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
docker compose up --build
```

Run dev in WSL or Linux Bash:

```bash
export SPRING_PROFILES_ACTIVE=dev
docker compose up --build
```

Or use the one-line WSL/Bash command:

```bash
SPRING_PROFILES_ACTIVE=dev docker compose up --build
```

Run prod after the schema exists in Windows PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:FINANCE_DB_USER = "finance_user"
$env:FINANCE_DB_PASSWORD = "finance_password"
$env:JWT_SECRET = "replace-with-a-long-random-secret"
docker compose up --build
```

Run prod after the schema exists in WSL or Linux Bash:

```bash
export SPRING_PROFILES_ACTIVE=prod
export FINANCE_DB_USER=finance_user
export FINANCE_DB_PASSWORD=finance_password
export JWT_SECRET=replace-with-a-long-random-secret
docker compose up --build
```

Do not remove the MySQL volume before switching from dev to prod because the prod profile validates an existing schema.

## Main REST endpoints

### Authentication

```text
POST /auth/register
POST /auth/login
POST /auth/password-reminder
POST /auth/password-reset
```

### User accounts

```text
POST   /users
GET    /users
GET    /users/{id}
PUT    /users/{id}
DELETE /users/{id}
```

`GET /users` is admin-only.

### Income

```text
POST   /incomes
GET    /incomes
GET    /incomes/user/{userId}
GET    /incomes/{id}
PUT    /incomes/{id}
DELETE /incomes/{id}
```

`GET /incomes` is admin-only.

### Debt

```text
POST   /debts
GET    /debts
GET    /debts/user/{userId}
GET    /debts/{id}
PUT    /debts/{id}
DELETE /debts/{id}
POST   /debts/{id}/payments
```

`GET /debts` is admin-only.

Savings, retirement, and analytics endpoints are also protected by the same JWT and user ownership rules.

## Postman

Import:

```text
postman/Personal-Finance-Tracker.postman_collection.json
```

The collection includes:

- Login as regular user
- Register a demo user
- Request a password reminder token
- Reset a password
- Show a regular user blocked from an admin-only endpoint
- Login as admin
- Show the admin listing users
- Run the finance tracker workflow with the regular user token

The collection stores `authToken`, `adminToken`, `resetToken`, `userId`, and created resource IDs as collection variables.

## Build without Docker

Java 17 and Maven are required.

```powershell
mvn clean package
```

Run Config Service first, followed by the required business services and Frontend Service.

## Repository layout

```text
analytics-service/
config-service/
debt-service/
frontend-service/
mysql-init/
postman/
retirement-service/
savings-service/
security-common/
user-account-service/
docker-compose.yml
pom.xml
README.md
```

## UI refresh and input checks

The browser dashboard refreshes after account, income, and debt changes. It also performs a background refresh every 15 seconds while a user is signed in and refreshes again when the browser window regains focus.

The backend uses Spring Data JPA repository methods instead of hand-written SQL string concatenation. This keeps database parameters bound by the persistence layer. The services also trim and validate free-text fields, reject SQL comment markers, statement terminators, and common injection phrases, and enforce length limits on user-entered strings. The frontend runs the same basic text check before sending account, income, and debt requests.

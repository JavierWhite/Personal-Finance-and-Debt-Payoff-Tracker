# Personal-Finance-and-Debt-Payoff-Tracker
Semester Project for Personal Finance Tracker and Debt payoff using Java 17, Spring in action, Docker, and creating a distributed system with  microservices. Using 4 microservices.

 First simple iteration of a REST-based personal finance system. It uses six Spring Boot applications, one MySQL container with five service-owned databases, Spring Cloud Config, Docker Compose, and a Postman collection.

# ----------------------------------------------------------------------------------------------------
## Services and ports

| Service | Port | Database | Main responsibility |
|---|---:|---|---|
| Config Service | 8888 | None | Dev and prod configuration |
| User Account Service | 8081 | finance_users | Accounts and profiles |
| Debt Service | 8082 | finance_debt | Debts and payments |
| Savings Service | 8083 | finance_savings | Goals and contributions |
| Retirement Service | 8084 | finance_retirement | Accounts, contributions, projection |
| Analytics Service | 8085 | finance_analytics | Monthly snapshots and chart data |

## Architecture

Each business microservice uses the required stack:

1. JPA entities saved in MySQL
2. Spring Data repositories
3. Service classes for business logic
4. REST controllers for CRUD operations

The web dashboard is a separate presentation client. It does not replace the REST APIs.

## Web dashboard features

Open `http://localhost:8080` after the containers start.

The dashboard supports:

- Create a user account
- Sign in with username and password
- Update profile details and account status
- Delete an account
- Add and remove gross income records
- Store income as monthly or yearly gross income
- Display normalized monthly and yearly income totals
- Add and remove debt accounts
- Record debt payments
- Display total debt and minimum payments

Development sign-in:

```text
Username: javier
Password: Password123!
```

The sign-in flow uses BCrypt password hashing, but it is still a basic local course demonstration. It does not issue JWTs or provide production-grade cross-service authorization.

## Run with Docker Compose

From the repository root:

```bash
docker compose up --build
```

Wait for the services to start, then verify:

```bash
curl http://localhost:8888/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
```

Stop the system:

```bash
docker compose down
```

Delete containers and the MySQL volume when you need a clean database:

```bash
docker compose down -v
```

# ----------------------------------------------------------------------------------------------------
## Profiles

The Docker Compose file defaults to `dev`.

Dev profile:

- MySQL host defaults to `mysql` inside Docker.
- Hibernate uses `ddl-auto=update`.
- SQL logging is enabled.
- Seed data is enabled.

Prod profile:

- Database credentials are required through environment variables.
- Hibernate uses `ddl-auto=validate`.
- SQL logging is disabled.
- Seed data is disabled.
- The sample prod JDBC configuration requires TLS. Adjust it to match the production MySQL server.

To select prod:

```bash
SPRING_PROFILES_ACTIVE=prod docker compose up --build
```

For PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
docker compose up --build
```

# ----------------------------------------------------------------------------------------------------
## Local Maven build

Java 17 and Maven are required.

```bash
mvn clean package
```

For local execution outside Docker, start MySQL and Config Service first. Set `DB_HOST=localhost`. Then run each service with the dev profile.

# ----------------------------------------------------------------------------------------------------
## Endpoints
### User accounts

- `POST /users`
- `GET /users`
- `GET /users/{id}`
- `PUT /users/{id}`
- `DELETE /users/{id}`

### Debts

- `POST /debts`
- `GET /debts`
- `GET /debts/user/{userId}`
- `GET /debts/{id}`
- `PUT /debts/{id}`
- `DELETE /debts/{id}`
- `POST /debts/{id}/payments`

### Savings

- `POST /savings-goals`
- `GET /savings-goals`
- `GET /savings-goals/user/{userId}`
- `GET /savings-goals/{id}`
- `PUT /savings-goals/{id}`
- `DELETE /savings-goals/{id}`
- `POST /savings-goals/{id}/contributions`

### Retirement

- `POST /retirement-accounts`
- `GET /retirement-accounts`
- `GET /retirement-accounts/user/{userId}`
- `GET /retirement-accounts/{id}`
- `PUT /retirement-accounts/{id}`
- `DELETE /retirement-accounts/{id}`
- `POST /retirement-accounts/{id}/contributions`
- `GET /retirement-accounts/{id}/projection`

### Analytics

- `POST /analytics/snapshots`
- `GET /analytics/snapshots/{id}`
- `GET /analytics/snapshots/user/{userId}`
- `PUT /analytics/snapshots/{id}`
- `DELETE /analytics/snapshots/{id}`
- `GET /analytics/user/{userId}/summary`
- `GET /analytics/user/{userId}/debt-chart`
- `GET /analytics/user/{userId}/savings-chart`
- `GET /analytics/user/{userId}/net-worth-chart`

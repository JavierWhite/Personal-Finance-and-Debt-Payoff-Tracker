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

## Checkpoint documents

- `docs/canonical-data-model.md` contains the canonical model and bounded contexts.
- `docs/deployment.md` contains the aimed Docker deployment.
- `postman/Personal-Finance-Tracker.postman_collection.json` contains the REST demo flow.

## Architecture rules

- Each business service has an entity, repository, service, and REST controller layer.

- Each service owns its database schema.

- Cross-service ownership uses scalar IDs such as `userId`. There are no JPA relationships across services.

- Dev data is inserted by beans marked with `@Profile("dev")`.

- The analytics service accepts manual monthly snapshots in iteration one. It does not call the other services yet.

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

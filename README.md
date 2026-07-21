# Personal Finance Microservices Upgrade

This overlay adds the missing presentation components without replacing the existing finance entities, repositories, services, controllers, security module, database, or frontend.

## Added components

- Eureka discovery server on port `8761`
- Spring Cloud Gateway on port `8090`
- Gateway routes that use `lb://service-name`
- Bearer-token forwarding through the gateway
- Safe token fingerprint logging, without logging the JWT itself
- Eureka clients for the existing Spring services
- Analytics-to-Debt service call using a load-balanced `RestClient`
- Bearer-token forwarding from Analytics to Debt Service
- Resilience4j circuit breaker and fallback for the Analytics-to-Debt call
- Debt Service instance endpoint for showing load distribution
- Gatling test that runs through the gateway
- A generated Compose file for scaling Debt Service to multiple instances

The frontend remains on `http://localhost:8080`. The gateway uses `http://localhost:8090` so the current frontend port does not need to change.

## Assumptions

The overlay matches the current project structure shown in the existing build output:

- Maven group: `com.javier.finance`
- Root artifact: `personal-finance-debt-tracker`
- Version: `0.0.1-SNAPSHOT`
- Java 17
- Spring Boot 3.5.x
- Existing packages under `com.javier.finance.analytics` and `com.javier.finance.debt`
- Existing login endpoint: `POST /auth/login`
- Login response contains `token` and `id`
- Existing protected Debt endpoint: `GET /debts/user/{userId}`

## Install into the working repository

Extract this upgrade archive somewhere outside the repository, then run the installer with the repository path.

```bash
cd ~/Downloads/personal-finance-microservices-upgrade
./apply-microservices-upgrade.sh ~/Projects/personal-finance-debt-tracker
```

For the Checkpoint 2 copy instead:

```bash
./apply-microservices-upgrade.sh ~/claude/Checkpoint2/personal-finance-debt-tracker
```

The installer:

1. Copies the new modules and source files.
2. Adds `eureka-server` and `api-gateway` to the Maven reactor.
3. Adds Spring Cloud `2025.0.3` dependency management.
4. Adds Eureka client dependencies to the existing Spring services.
5. Adds LoadBalancer, Resilience4j, and AOP dependencies to Analytics Service.
6. Creates backups under `.microservices-upgrade-backup/`.

## Build the project before Docker

From the repository root:

```bash
mvn clean package -DskipTests
```

This catches Java or Maven errors before Docker rebuilds every image.

## Start the normal discovery stack

```bash
docker compose down

docker compose \
  -f docker-compose.yml \
  -f docker-compose.discovery.yml \
  up -d --build
```

Wait for the applications:

```bash
scripts/wait-for-discovery-stack.sh
```

Check the containers:

```bash
docker compose \
  -f docker-compose.yml \
  -f docker-compose.discovery.yml \
  ps
```

## URLs

```text
Frontend:       http://localhost:8080
API Gateway:    http://localhost:8090
Eureka:         http://localhost:8761
Config Service: http://localhost:8888
```

Gateway health:

```bash
curl -s http://localhost:8090/actuator/health
```

Gateway routes:

```bash
curl -s http://localhost:8090/actuator/gateway/routes | python3 -m json.tool
```

## Get a bearer token through the gateway

Run the script with `source` so the exported variables remain in your terminal:

```bash
source scripts/login-and-export.sh
```

Defaults:

```text
Username: javier
Password: Password123!
```

Override the credentials when needed:

```bash
FINANCE_USERNAME=admin \
FINANCE_PASSWORD='Password123!' \
source scripts/login-and-export.sh
```

Confirm the token is set without printing it:

```bash
echo "TOKEN length: ${#TOKEN}"
echo "USER_ID: $USER_ID"
```

## Show a protected request through the gateway

```bash
curl -i \
  -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8090/api/debts/user/$USER_ID"
```

The gateway removes `/api` and routes the request to `lb://debt-service` through Eureka.

Show the unauthorized result by removing the token:

```bash
curl -i "http://localhost:8090/api/debts/user/$USER_ID"
```

Expected result: `401 Unauthorized` from the protected Debt Service.

## Prove that the bearer token crosses services

Run:

```bash
scripts/demo-token-relay.sh
```

Request path:

```text
Client
  -> API Gateway
  -> Analytics Service
  -> Debt Service
```

The gateway forwards the original `Authorization` header. Analytics receives the header and sends the same bearer token on its load-balanced call to `http://debt-service/debts/user/{userId}`.

Inspect these response headers:

```text
X-Token-Relay: forwarded
X-Token-Fingerprint: <12-character fingerprint>
X-Gateway-Token-Fingerprint-Seen: <same fingerprint>
X-Resilience-Status: UP
```

The fingerprint is derived from SHA-256 and is only used for the classroom demonstration. The full token is never logged or returned.

Also inspect Gateway logs:

```bash
docker logs finance-api-gateway --tail 50
```

You should see a safe message similar to:

```text
Relaying bearer token fingerprint=1a2b3c4d5e6f path=/api/analytics/demo/debts/1
```

## Show Eureka service discovery

Open:

```text
http://localhost:8761
```

Expected registered applications include:

```text
API-GATEWAY
CONFIG-SERVICE
USER-ACCOUNT-SERVICE
DEBT-SERVICE
SAVINGS-SERVICE
RETIREMENT-SERVICE
ANALYTICS-SERVICE
FRONTEND-SERVICE
```

The exact capitalization is controlled by Eureka.

## Scale Debt Service to three instances

Your original Compose file publishes Debt Service on host port `8082` and assigns a fixed `container_name`. Both settings prevent multiple instances.

Generate a separate scaling Compose file. This leaves your original file unchanged:

```bash
python3 upgrade-tools/make_scale_compose.py
```

Start the scaled stack:

```bash
docker compose \
  -f docker-compose.scale.yml \
  -f docker-compose.discovery.yml \
  up -d --build --scale debt-service=3
```

The generated file removes the fixed Debt Service container name and published host port. Debt Service remains reachable through Gateway port `8090`.

Refresh Eureka and confirm that `DEBT-SERVICE` has three instances.

Show requests reaching different instances:

```bash
source scripts/login-and-export.sh
REQUEST_COUNT=15 scripts/show-load-balancing.sh
```

The JSON response includes:

```json
{
  "service": "debt-service",
  "instance": "container-hostname",
  "port": "8082",
  "tokenFingerprint": "1a2b3c4d5e6f",
  "gatewayFingerprint": "1a2b3c4d5e6f"
}
```

The `instance` value should change as Spring Cloud LoadBalancer selects registered Debt Service instances.

## Show resilience

Keep Analytics Service running and stop all Debt Service instances:

```bash
docker compose \
  -f docker-compose.scale.yml \
  -f docker-compose.discovery.yml \
  stop debt-service
```

Call the cross-service endpoint several times:

```bash
for attempt in 1 2 3 4 5; do
  curl -i -s \
    -H "Authorization: Bearer $TOKEN" \
    "http://localhost:8090/api/analytics/demo/debts/$USER_ID" \
    | grep -E "HTTP/|X-Resilience-Status|DEGRADED|temporarily"
  echo
  sleep 1
done
```

Expected behavior:

- Analytics does not expose a raw connection exception.
- The response remains controlled.
- `X-Resilience-Status` changes to `DEGRADED`.
- The JSON body identifies `debt-service` as unavailable.
- After repeated failures, the Resilience4j circuit breaker opens.

Check circuit breaker health:

```bash
curl -s http://localhost:8085/actuator/health | python3 -m json.tool
```

Restart Debt Service:

```bash
docker compose \
  -f docker-compose.scale.yml \
  -f docker-compose.discovery.yml \
  up -d --scale debt-service=3 debt-service
```

Wait at least 10 seconds, then call the relay endpoint again. `X-Resilience-Status` should return to `UP` after the circuit breaker completes its half-open recovery checks.

## Run the Gatling stress test

The test performs this path for each virtual user:

1. Log in through Gateway.
2. Save the JWT and user ID.
3. Read debts through Gateway.
4. Call Analytics, which relays the JWT to Debt Service.
5. Request the Debt Service instance endpoint.

Run the default test:

```bash
mvn -f performance-tests/pom.xml gatling:test \
  -Dusername=javier \
  -Dpassword='Password123!'
```

Default load profile:

```text
Ramp: 20 users over 20 seconds
Steady: 10 users per second for 30 seconds
Peak ramp: 10 to 30 users per second over 30 seconds
```

Default assertions:

```text
Failed requests below 1 percent
95th percentile response time below 800 milliseconds
```

Use lower values for the first local run:

```bash
mvn -f performance-tests/pom.xml gatling:test \
  -DrampUsers=5 \
  -DsteadyUsersPerSecond=2 \
  -DpeakUsersPerSecond=5 \
  -Dp95Ms=1500
```

Reports are generated under:

```text
performance-tests/target/gatling/
```

Compare these two runs without changing the load profile:

```text
Run A: one Debt Service instance
Run B: three Debt Service instances
```

Record total requests, requests per second, mean response time, p95, p99, and failed-request percentage.

## Return to the original one-instance setup

```bash
docker compose \
  -f docker-compose.scale.yml \
  -f docker-compose.discovery.yml \
  down

docker compose \
  -f docker-compose.yml \
  -f docker-compose.discovery.yml \
  up -d --build
```

## Troubleshooting

### Eureka opens, but no services appear

Check a service log:

```bash
docker logs finance-debt-service --tail 100
```

Search for Eureka registration errors:

```bash
docker compose \
  -f docker-compose.yml \
  -f docker-compose.discovery.yml \
  logs | grep -iE "eureka|discovery|registration|refused"
```

### Gateway returns 503

A `503` means the gateway route matched, but Eureka did not provide a usable service instance.

Check:

```bash
curl -s http://localhost:8761/eureka/apps | head
curl -s http://localhost:8090/actuator/gateway/routes | python3 -m json.tool
```

### Gateway returns 404

Confirm that you are using Gateway port `8090` and the `/api` prefix:

```text
Correct: http://localhost:8090/api/debts/user/1
Frontend: http://localhost:8080
```

### Gateway returns 401

Refresh the token:

```bash
source scripts/login-and-export.sh
```

### Maven compatibility error

The patch uses Spring Cloud `2025.0.3` for Spring Boot `3.5.x`. Confirm the root parent version:

```bash
grep -nA2 -B2 "spring-boot-starter-parent" pom.xml
```

### Package not found during compilation

The added classes assume these package roots:

```text
com.javier.finance.analytics
com.javier.finance.debt
```

Confirm the current application classes:

```bash
find analytics-service/src/main/java -name '*Application.java' -print
find debt-service/src/main/java -name '*Application.java' -print
```

Move the added `integration` or `demo` packages under the actual application package if your local copy uses a different package name.

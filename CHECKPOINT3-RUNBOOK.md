
# Checkpoint 3 Runbook

This version adds the third checkpoint requirements to the existing Personal Finance and Debt Payoff Tracker.

## What was added

- JMS using Apache ActiveMQ locally and Amazon MQ for ActiveMQ on AWS.
- Debt Service publishes a `DebtPaymentRecorded` event after a payment is saved.
- Analytics Service consumes that event with `@JmsListener` and stores a processed-event record.
- Micrometer JMS instrumentation creates `jms.message.publish` and `jms.message.process` observations.
- Spring Boot 3.5 tracing is enabled on Gateway, User Account, Debt, and Analytics services using Micrometer Brave plus the Zipkin reporter.
- Gatling now creates a debt, records a payment, causes a JMS event, checks Analytics, and exercises the existing cross-service bearer-token path.
- The browser frontend now sends User and Debt API calls through Spring Cloud Gateway on port 8090.

## Prerequisites

- Java 17
- Maven 3.9+
- Docker Engine / Docker Desktop
- Docker Compose v2
- WSL2 is supported

## 1. Build before Docker

From the project root:

```bash
mvn clean package -DskipTests
```

## 2. Start the complete local Checkpoint 3 stack

```bash
chmod +x scripts/*.sh
scripts/checkpoint3-start-local.sh
scripts/wait-for-discovery-stack.sh
```

Or run Compose directly:

```bash
docker compose \
  -f docker-compose.yml \
  -f docker-compose.discovery.yml \
  -f docker-compose.eureka-fix.yml \
  -f docker-compose.checkpoint3-local.yml \
  up -d --build
```


Eureka note: `docker-compose.eureka-fix.yml` is required in this project because Eureka's `serviceUrl.defaultZone` map key must preserve the `defaultZone` casing. The override supplies it through `SPRING_APPLICATION_JSON` inside Docker.

Useful URLs:

- Frontend: `http://localhost:8080`
- API Gateway: `http://localhost:8090`
- Eureka: `http://localhost:8761`
- ActiveMQ web console: `http://localhost:8161/admin/` (web console default: `admin` / `admin`)
- Local broker container: Apache ActiveMQ official Docker image, configured with `finance` / `finance-demo-password` for JMS connections.
- Zipkin: `http://localhost:9411/zipkin/`
- Config Service: `http://localhost:8888`

## 3. Demonstrate JMS locally

Run:

```bash
scripts/checkpoint3-demo-jms.sh
```

The script:

1. Logs in through the Gateway.
2. Creates a temporary debt.
3. Records a $25 payment.
4. Debt Service publishes to `finance.debt.payment.recorded`.
5. Analytics Service consumes the message.
6. The script prints the stored Analytics event and relevant service logs.

You can also inspect the event through the Gateway:

```text
GET /api/analytics/events/debt-payments/user/{userId}
GET /api/analytics/events/debt-payments/user/{userId}/count
```

## 4. Demonstrate tracing

Open Zipkin at `http://localhost:9411/zipkin/`.

Use the service selector to inspect `debt-service` and `analytics-service` traces. A payment should show HTTP work plus JMS spans. Look for span names resembling:

- `http ...`
- `jms.message.publish`
- `jms.message.process`

The JMS observation instrumentation propagates the trace context in JMS headers. This lets the consumer work remain connected to the originating transaction trace.

## 5. Run the Gatling stress test

Default Checkpoint 3 load:

```bash
scripts/run-checkpoint3-stress.sh
```

The defaults are:

- 20 users ramped over 20 seconds
- 10 users/second for 30 seconds
- ramp from 10 to 30 users/second over 30 seconds
- failed requests below 1 percent
- p95 response time below 1500 ms

For a smaller first test:

```bash
RAMP_USERS=5 STEADY_USERS_PER_SECOND=2 PEAK_USERS_PER_SECOND=5 P95_MS=2000 scripts/run-checkpoint3-stress.sh
```

Reports are written under:

```text
performance-tests/target/gatling/
```

While Gatling is running, open Zipkin and refresh the trace query. The stress test deliberately records payments so JMS publish and process spans are generated under load.

## 6. Stop the local stack

```bash
docker compose \
  -f docker-compose.yml \
  -f docker-compose.discovery.yml \
  -f docker-compose.eureka-fix.yml \
  -f docker-compose.checkpoint3-local.yml \
  down
```

Add `-v` only when you intentionally want to erase MySQL and ActiveMQ local data.

## AWS deployment summary

For the course checkpoint, the simplest deployment is:

- One Amazon Linux 2023 EC2 instance running the Docker Compose application.
- Amazon MQ for ActiveMQ in the same VPC.
- Zipkin running as a container on EC2.
- MySQL remains a Docker volume on EC2 for the checkpoint demo.

Detailed AWS steps are in `AWS-DEPLOYMENT-GUIDE.md`.

## Key source files for the presentation

Producer:

```text
debt-service/src/main/java/com/javier/finance/debt/messaging/DebtPaymentEventPublisher.java
```

Publishing business flow:

```text
debt-service/src/main/java/com/javier/finance/debt/service/DebtAccountService.java
```

Consumer:

```text
analytics-service/src/main/java/com/javier/finance/analytics/messaging/DebtPaymentEventListener.java
```

JMS observation setup:

```text
debt-service/src/main/java/com/javier/finance/debt/config/JmsObservationConfig.java
analytics-service/src/main/java/com/javier/finance/analytics/config/JmsObservationConfig.java
```

Stress test:

```text
performance-tests/src/test/java/com/javier/finance/performance/FinanceGatewaySimulation.java
```

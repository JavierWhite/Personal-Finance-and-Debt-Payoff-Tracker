
# Checkpoint 3 Video Presentation Guide

Target length: about 8 to 10 minutes.

## Before recording

Have these windows ready:

1. AWS EC2 Console.
2. Amazon MQ Console.
3. Browser tab for the deployed frontend.
4. Browser tab for Eureka.
5. Browser tab for Zipkin.
6. SSH terminal connected to EC2.
7. Local WSL terminal for Gatling.
8. IDE open to the producer and consumer source files.

Do one complete test before recording.

## 0:00 to 0:45, introduce Checkpoint 3

Say that this iteration deploys the Personal Finance and Debt Payoff Tracker to AWS and adds asynchronous JMS messaging plus distributed trace collection and visualization under a Gatling stress test.

Show the architecture:

```text
Gatling / Browser
       |
       v
Spring Cloud Gateway :8090
       |
       v
Debt Service -----> MySQL
       |
       | JMS DebtPaymentRecorded
       v
Amazon MQ ActiveMQ
       |
       v
Analytics Service -> MySQL

Gateway + Debt + Analytics
       |
       v
     Zipkin
```

## 0:45 to 1:45, prove AWS deployment

Show the running EC2 instance and Amazon MQ broker in AWS Console.

In SSH run:

```bash
docker compose --env-file .env.aws \
  -f docker-compose.yml \
  -f docker-compose.discovery.yml \
  -f docker-compose.eureka-fix.yml \
  -f docker-compose.aws.yml \
  ps
```

Show that the services are running.

Open Eureka and show the registered services.

## 1:45 to 3:00, explain JMS code

Open:

```text
debt-service/src/main/java/com/javier/finance/debt/messaging/DebtPaymentEventPublisher.java
```

Point out the `JmsTemplate` call and queue name.

Then open:

```text
debt-service/src/main/java/com/javier/finance/debt/service/DebtAccountService.java
```

Point out that after a payment updates the balance, the service publishes a `DebtPaymentRecordedEvent`.

Then open:

```text
analytics-service/src/main/java/com/javier/finance/analytics/messaging/DebtPaymentEventListener.java
```

Point out `@JmsListener` and explain that Analytics stores the event independently from the original HTTP request.

## 3:00 to 4:15, live JMS demonstration

Record a payment through the UI or run from local WSL:

```bash
BASE_URL=http://EC2_PUBLIC_IP:8090 scripts/checkpoint3-demo-jms.sh
```

On the EC2 SSH terminal show:

```bash
docker logs finance-debt-service --tail 50
docker logs finance-analytics-service --tail 50
```

Show the producer log and consumer log.

In Amazon MQ Console show the queue named:

```text
finance.debt.payment.recorded
```

Explain that the queue may show zero pending messages because Analytics consumes them quickly. Producer and dequeue counts are the useful evidence.

## 4:15 to 5:00, explain tracing

Open:

```text
debt-service/src/main/java/com/javier/finance/debt/config/JmsObservationConfig.java
analytics-service/src/main/java/com/javier/finance/analytics/config/JmsObservationConfig.java
```

Explain that the `ObservationRegistry` is attached to both `JmsTemplate` and the JMS listener container. This creates JMS publish and process observations and propagates trace context through the message headers.

## 5:00 to 6:30, run the stress test

From local WSL run:

```bash
BASE_URL=http://EC2_PUBLIC_IP:8090 scripts/run-checkpoint3-stress.sh
```

Explain the scenario while it runs:

1. Login through Gateway.
2. Create a debt.
3. Record a payment.
4. Publish a JMS event.
5. Analytics consumes the event.
6. Query the Analytics event count.
7. Exercise the Analytics to Debt service call.
8. Delete the temporary Gatling debt.

## 6:30 to 7:45, visualize traces in Zipkin

Open:

```text
http://EC2_PUBLIC_IP:9411
```

Query recent traces for `debt-service` or `analytics-service`.

Open a payment-related trace. Point out:

- Gateway HTTP span.
- Debt Service request span.
- `jms.message.publish` span.
- `jms.message.process` span in Analytics.

If the asynchronous consumer is shown as a linked or continuing trace section rather than visually nested exactly under the HTTP span, explain that JMS trace headers carry the tracing context across the broker boundary.

## 7:45 to 8:45, show Gatling results

Open the generated Gatling HTML report under:

```text
performance-tests/target/gatling/
```

State the measured values from your actual run:

- total requests
- failed request percentage
- mean response time
- p95
- p99
- requests per second

Do not claim a threshold passed unless the report shows that it passed.

## 8:45 to 9:30, final explanation

Use this wording as a guide:

This checkpoint deploys the finance tracker on AWS and extends the application with asynchronous messaging. When Debt Service records a payment, it sends a JMS event through Amazon MQ. Analytics Service consumes and stores that event independently. Micrometer tracing carries trace context across the HTTP and JMS boundaries, and Zipkin displays the resulting distributed traces. Gatling generates load through the AWS Gateway so I can demonstrate both application performance and trace collection while the system is under stress.

## If something fails during the recording

Gateway 503:

```bash
scripts/diagnose-gateway-503.sh
```

JMS connection issue:

```bash
docker logs finance-debt-service --tail 100 | grep -iE 'jms|activemq|connect|ssl|exception'
docker logs finance-analytics-service --tail 100 | grep -iE 'jms|activemq|connect|ssl|exception'
```

Zipkin has no traces:

```bash
docker logs finance-zipkin --tail 100
docker exec finance-debt-service printenv | grep -E 'ZIPKIN|TRACING'
```

Eureka missing a service:

```bash
docker compose --env-file .env.aws \
  -f docker-compose.yml \
  -f docker-compose.discovery.yml \
  -f docker-compose.eureka-fix.yml \
  -f docker-compose.aws.yml \
  logs | grep -iE 'eureka|registration|refused'
```

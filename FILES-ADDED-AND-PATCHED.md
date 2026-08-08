# Checkpoint 3 files added and patched

This list is relative to the source ZIP supplied for Checkpoint 3.

## JMS producer in Debt Service

- `debt-service/src/main/java/com/javier/finance/debt/messaging/DebtPaymentRecordedEvent.java`
- `debt-service/src/main/java/com/javier/finance/debt/messaging/DebtPaymentEventPublisher.java`
- `debt-service/src/main/java/com/javier/finance/debt/config/JmsObservationConfig.java`
- `debt-service/src/main/java/com/javier/finance/debt/service/DebtAccountService.java`
- `debt-service/src/main/resources/application.yml`
- `debt-service/pom.xml`

## JMS consumer in Analytics Service

- `analytics-service/src/main/java/com/javier/finance/analytics/messaging/DebtPaymentRecordedEvent.java`
- `analytics-service/src/main/java/com/javier/finance/analytics/messaging/DebtPaymentEventListener.java`
- `analytics-service/src/main/java/com/javier/finance/analytics/config/JmsObservationConfig.java`
- `analytics-service/src/main/java/com/javier/finance/analytics/entity/ProcessedDebtPaymentEvent.java`
- `analytics-service/src/main/java/com/javier/finance/analytics/repository/ProcessedDebtPaymentEventRepository.java`
- `analytics-service/src/main/java/com/javier/finance/analytics/controller/PaymentEventController.java`
- `analytics-service/src/main/resources/application.yml`
- `analytics-service/pom.xml`

## Distributed tracing

- `api-gateway/pom.xml`
- `api-gateway/src/main/resources/application.yml`
- `user-account-service/pom.xml`
- `user-account-service/src/main/resources/application.yml`
- Debt and Analytics tracing dependencies and configuration listed above.

The Spring Boot 3.5 configuration uses Micrometer Brave, Zipkin Reporter, `management.zipkin.tracing.endpoint`, and 100 percent sampling for the classroom demonstration.

## Stress test and frontend routing

- `performance-tests/src/test/java/com/javier/finance/performance/FinanceGatewaySimulation.java`
- `frontend-service/src/main/resources/static/app.js`

## Local and AWS deployment

- `docker-compose.checkpoint3-local.yml`
- `docker-compose.aws.yml`
- `.env.aws.example`
- `.gitignore`
- `scripts/checkpoint3-start-local.sh`
- `scripts/checkpoint3-demo-jms.sh`
- `scripts/run-checkpoint3-stress.sh`
- `scripts/aws-ec2-bootstrap.sh`

## Documentation and copy helper

- `README.md`
- `AWS-DEPLOYMENT-GUIDE.md`
- `CHECKPOINT3-PRESENTATION-GUIDE.md`
- `CHECKPOINT3-RUNBOOK.md`
- `CHECKPOINT3-VALIDATION.md`
- `CHECKPOINT3-CHANGED-FILES.txt`
- `scripts/copy-checkpoint3-changes.sh`

To copy only these files into another checkout:

```bash
./scripts/copy-checkpoint3-changes.sh /path/to/existing-project
```

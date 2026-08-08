# Checkpoint 3 Validation Notes

Validation performed on the corrected package:

- All Maven `pom.xml` files parse as valid XML.
- All Docker Compose and Spring configuration files parse as valid YAML.
- All JSON files parse as valid JSON.
- All included Bash scripts pass `bash -n` syntax checking.
- Both `DebtPaymentRecordedEvent` Java records compile with Java 17+ because they use only JDK types.
- Every path in `CHECKPOINT3-CHANGED-FILES.txt` exists in the package.
- Spring Boot 3.5 tracing configuration was checked against the Spring Boot 3.5 reference. The package uses `micrometer-tracing-bridge-brave`, `zipkin-reporter-brave`, and `management.zipkin.tracing.endpoint`.
- JMS tracing configuration follows Spring Framework JMS observability requirements by attaching the `ObservationRegistry` to both `JmsTemplate` and `DefaultJmsListenerContainerFactory`.
- The Amazon MQ configuration uses the ActiveMQ OpenWire SSL endpoint format `ssl://...:61617`.
- The local Compose file uses the Apache ActiveMQ Docker image documented by the ActiveMQ project and explicitly configures both JMS and web-console credentials.
- AWS Compose commands now explicitly use `--env-file .env.aws` so the Amazon MQ credentials and endpoint are actually supplied.

A full Maven dependency build could not be executed in this packaging environment because Maven dependencies cannot be downloaded from the container.

Run this first on your WSL machine:

```bash
mvn clean package -DskipTests
```

Then start the local Checkpoint 3 stack:

```bash
scripts/checkpoint3-start-local.sh
scripts/wait-for-discovery-stack.sh
scripts/checkpoint3-demo-jms.sh
```

If Maven reports an error, save the complete output including the first `Caused by` section.

# Files added and patched

## New modules

- `eureka-server/`
- `api-gateway/`
- `performance-tests/`

## New application files

- `analytics-service/src/main/java/com/javier/finance/analytics/integration/LoadBalancedClientConfig.java`
- `analytics-service/src/main/java/com/javier/finance/analytics/integration/DebtRelayService.java`
- `analytics-service/src/main/java/com/javier/finance/analytics/integration/BearerTokenRelayController.java`
- `analytics-service/src/main/resources/application-discovery.yml`
- `debt-service/src/main/java/com/javier/finance/debt/demo/DebtInstanceController.java`

## New deployment and demo files

- `docker-compose.discovery.yml`
- `upgrade-tools/patch_repo.py`
- `upgrade-tools/make_scale_compose.py`
- `scripts/login-and-export.sh`
- `scripts/demo-token-relay.sh`
- `scripts/show-load-balancing.sh`
- `scripts/wait-for-discovery-stack.sh`

## Existing files patched by the installer

- Root `pom.xml`
- `config-service/pom.xml`
- `user-account-service/pom.xml`
- `debt-service/pom.xml`
- `savings-service/pom.xml`
- `retirement-service/pom.xml`
- `analytics-service/pom.xml`
- `frontend-service/pom.xml`

The installer creates timestamped backups under `.microservices-upgrade-backup/` before changing existing POM files.

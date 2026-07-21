#!/usr/bin/env bash
set -u

wait_for_url() {
  local label="$1"
  local url="$2"
  local attempts="${3:-90}"

  echo "Waiting for ${label}: ${url}"
  for attempt in $(seq 1 "$attempts"); do
    if curl -fsS "$url" >/dev/null 2>&1; then
      echo "UP: ${label}"
      return 0
    fi
    sleep 2
  done

  echo "FAILED: ${label}" >&2
  return 1
}

wait_for_url "Eureka health" "http://localhost:8761/actuator/health" || exit 1
wait_for_url "Gateway health" "http://localhost:8090/actuator/health" || exit 1
wait_for_url "User Account direct health" "http://localhost:8081/actuator/health" || exit 1
wait_for_url "Debt direct health" "http://localhost:8082/actuator/health" || exit 1
wait_for_url "Savings direct health" "http://localhost:8083/actuator/health" || exit 1
wait_for_url "Retirement direct health" "http://localhost:8084/actuator/health" || exit 1
wait_for_url "Analytics direct health" "http://localhost:8085/actuator/health" || exit 1

required_apps=(
  USER-ACCOUNT-SERVICE
  DEBT-SERVICE
  SAVINGS-SERVICE
  RETIREMENT-SERVICE
  ANALYTICS-SERVICE
  API-GATEWAY
)

for app in "${required_apps[@]}"; do
  wait_for_url "Eureka registration ${app}" "http://localhost:8761/eureka/apps/${app}" 90 || exit 1
done

echo "Discovery stack is ready."

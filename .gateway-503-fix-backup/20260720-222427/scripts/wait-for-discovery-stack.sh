#!/usr/bin/env bash
set -euo pipefail

urls=(
  "http://localhost:8761/actuator/health"
  "http://localhost:8090/actuator/health"
  "http://localhost:8081/actuator/health"
  "http://localhost:8082/actuator/health"
  "http://localhost:8083/actuator/health"
  "http://localhost:8084/actuator/health"
  "http://localhost:8085/actuator/health"
)

for url in "${urls[@]}"; do
  echo "Waiting for ${url}"
  for attempt in $(seq 1 60); do
    if curl -fsS "$url" >/dev/null 2>&1; then
      echo "UP: ${url}"
      break
    fi
    if [[ "$attempt" -eq 60 ]]; then
      echo "FAILED: ${url}" >&2
      exit 1
    fi
    sleep 2
  done
done

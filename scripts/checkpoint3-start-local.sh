
#!/usr/bin/env bash
set -euo pipefail

COMPOSE=(docker compose \
  -f docker-compose.yml \
  -f docker-compose.discovery.yml \
  -f docker-compose.eureka-fix.yml \
  -f docker-compose.checkpoint3-local.yml)

"${COMPOSE[@]}" down --remove-orphans
"${COMPOSE[@]}" up -d --build

echo
echo "Checkpoint 3 stack started."
echo "Frontend:        http://localhost:8080"
echo "Gateway:         http://localhost:8090"
echo "Eureka:          http://localhost:8761"
echo "ActiveMQ console:http://localhost:8161/admin/"
echo "Zipkin:          http://localhost:9411/zipkin/"
echo
echo "Run: scripts/wait-for-discovery-stack.sh"

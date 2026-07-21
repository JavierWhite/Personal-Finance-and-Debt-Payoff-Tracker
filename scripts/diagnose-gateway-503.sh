#!/usr/bin/env bash
set -u

compose_files=(-f docker-compose.yml -f docker-compose.discovery.yml)

echo "== Container status =="
docker compose "${compose_files[@]}" ps || true

echo
echo "== Health endpoints =="
for entry in \
  "Eureka|http://localhost:8761/actuator/health" \
  "Gateway|http://localhost:8090/actuator/health" \
  "User Account|http://localhost:8081/actuator/health" \
  "Debt|http://localhost:8082/actuator/health" \
  "Analytics|http://localhost:8085/actuator/health"; do
  label="${entry%%|*}"
  url="${entry#*|}"
  code="$(curl -sS -o /tmp/finance-health-body -w '%{http_code}' "$url" 2>/dev/null || true)"
  printf '%-15s HTTP %s ' "$label" "${code:-000}"
  cat /tmp/finance-health-body 2>/dev/null || true
  echo
 done
rm -f /tmp/finance-health-body

echo
echo "== Eureka applications =="
registry="$(curl -sS http://localhost:8761/eureka/apps 2>/dev/null || true)"
if [[ -z "$registry" ]]; then
  echo "Could not read the Eureka registry."
else
  printf '%s' "$registry" | grep -oE '<name>[^<]+' | sed 's/<name>/- /' | sort -u || true
fi

echo
echo "== Gateway routes =="
curl -sS http://localhost:8090/actuator/gateway/routes 2>/dev/null | python3 -m json.tool 2>/dev/null || true

echo
echo "== Direct login test =="
curl -sS -i \
  -X POST \
  -H 'Content-Type: application/json' \
  -d '{"username":"javier","password":"Password123!"}' \
  http://localhost:8081/auth/login | sed -n '1,25p'

echo
echo "== Gateway login test =="
curl -sS -i \
  -X POST \
  -H 'Content-Type: application/json' \
  -d '{"username":"javier","password":"Password123!"}' \
  http://localhost:8090/api/auth/login | sed -n '1,25p'

echo
echo "== Recent discovery logs =="
docker compose "${compose_files[@]}" logs --tail=80 \
  eureka-server api-gateway user-account-service 2>/dev/null \
  | grep -iE 'registered|registration|eureka|discovery|no servers|connection refused|exception|error' \
  | tail -80 || true

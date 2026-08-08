#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8090}"
FINANCE_USERNAME="${FINANCE_USERNAME:-javier}"
FINANCE_PASSWORD="${FINANCE_PASSWORD:-Password123!}"

json_field() {
  python3 -c 'import json,sys; data=json.load(sys.stdin); print(data[sys.argv[1]])' "$1"
}

LOGIN=$(curl -fsS -X POST "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$FINANCE_USERNAME\",\"password\":\"$FINANCE_PASSWORD\"}")
TOKEN=$(printf '%s' "$LOGIN" | json_field token)
USER_ID=$(printf '%s' "$LOGIN" | json_field id)
DEBT_NAME="Checkpoint3-$(date +%s)"

CREATE=$(curl -fsS -X POST "$BASE_URL/api/debts" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"userId\":$USER_ID,\"debtName\":\"$DEBT_NAME\",\"debtType\":\"CREDIT_CARD\",\"originalBalance\":500.00,\"currentBalance\":500.00,\"interestRate\":18.0,\"minimumPayment\":10.00,\"status\":\"ACTIVE\"}")
DEBT_ID=$(printf '%s' "$CREATE" | json_field id)

echo "Created debt ID $DEBT_ID for user $USER_ID"

curl -fsS -X POST "$BASE_URL/api/debts/$DEBT_ID/payments" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"amount":25.00,"note":"Checkpoint 3 JMS live demo"}' >/dev/null

echo "Payment recorded. Debt Service published a JMS event."
echo "Waiting briefly for the asynchronous consumer..."
sleep 2

echo
echo "Most recent Analytics JMS events:"
curl -fsS "$BASE_URL/api/analytics/events/debt-payments/user/$USER_ID" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo
echo "Debt Service JMS log:"
docker logs finance-debt-service --tail 40 2>&1 | grep -E 'Published debt payment JMS event|traceId|spanId' || true

echo
echo "Analytics JMS consumer log:"
docker logs finance-analytics-service --tail 40 2>&1 | grep -E 'Processed debt payment JMS event|traceId|spanId' || true

echo
echo "Open Zipkin and search for debt-service or analytics-service: http://localhost:9411/zipkin/"

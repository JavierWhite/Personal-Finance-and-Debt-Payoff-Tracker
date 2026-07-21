#!/usr/bin/env bash
set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8090}"

if [[ -z "${TOKEN:-}" ]]; then
  echo "ERROR: TOKEN is empty. Run: source scripts/login-and-export.sh" >&2
  exit 2
fi

for request_number in $(seq 1 "${REQUEST_COUNT:-12}"); do
  echo "Request ${request_number}"
  curl -fsS \
    -H "Authorization: Bearer ${TOKEN}" \
    "${GATEWAY_URL}/api/debts/demo/instance"
  echo
  sleep 0.25
done

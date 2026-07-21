#!/usr/bin/env bash
set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8090}"

if [[ -z "${TOKEN:-}" || -z "${USER_ID:-}" ]]; then
  echo "ERROR: TOKEN or USER_ID is empty. Run: source scripts/login-and-export.sh" >&2
  exit 2
fi

curl -i -fsS \
  -H "Authorization: Bearer ${TOKEN}" \
  "${GATEWAY_URL}/api/analytics/demo/debts/${USER_ID}"

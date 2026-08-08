#!/usr/bin/env bash
# Source this file so TOKEN and USER_ID remain available in your shell:
#   source scripts/login-and-export.sh

set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8090}"
FINANCE_USERNAME="${FINANCE_USERNAME:-javier}"
FINANCE_PASSWORD="${FINANCE_PASSWORD:-Password123!}"

LOGIN_RESPONSE="$(curl -fsS \
  -X POST \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"${FINANCE_USERNAME}\",\"password\":\"${FINANCE_PASSWORD}\"}" \
  "${GATEWAY_URL}/api/auth/login")"

TOKEN="$(LOGIN_RESPONSE="$LOGIN_RESPONSE" python3 - <<'PY'
import json
import os
body = json.loads(os.environ["LOGIN_RESPONSE"])
print(body.get("token") or body.get("accessToken") or body.get("access_token") or "")
PY
)"

USER_ID="$(LOGIN_RESPONSE="$LOGIN_RESPONSE" python3 - <<'PY'
import json
import os
body = json.loads(os.environ["LOGIN_RESPONSE"])
print(body.get("id") or body.get("userId") or body.get("user_id") or "")
PY
)"

if [[ -z "$TOKEN" ]]; then
  echo "ERROR: Login succeeded but no token field was found. Response:" >&2
  echo "$LOGIN_RESPONSE" >&2
  return 1 2>/dev/null || exit 1
fi

if [[ -z "$USER_ID" ]]; then
  echo "ERROR: Login succeeded but no user ID field was found. Response:" >&2
  echo "$LOGIN_RESPONSE" >&2
  return 1 2>/dev/null || exit 1
fi

export TOKEN USER_ID GATEWAY_URL

echo "Authenticated as ${FINANCE_USERNAME}"
echo "USER_ID=${USER_ID}"
echo "TOKEN length=${#TOKEN}, prefix=${TOKEN:0:12}..."

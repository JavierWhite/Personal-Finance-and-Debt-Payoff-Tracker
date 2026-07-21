#!/usr/bin/env bash
# Source this file so TOKEN and USER_ID remain available:
#   source scripts/login-and-export.sh
# This version does not enable `set -e` in the calling shell.

finance_login_and_export() {
  local gateway_url username password raw_response response_body http_status token user_id

  gateway_url="${GATEWAY_URL:-http://localhost:8090}"
  username="${FINANCE_USERNAME:-javier}"
  password="${FINANCE_PASSWORD:-Password123!}"

  raw_response="$(curl -sS \
    -X POST \
    -H 'Content-Type: application/json' \
    -d "{\"username\":\"${username}\",\"password\":\"${password}\"}" \
    -w $'\n%{http_code}' \
    "${gateway_url}/api/auth/login")" || {
      echo "ERROR: Could not contact ${gateway_url}/api/auth/login" >&2
      return 1
    }

  http_status="${raw_response##*$'\n'}"
  response_body="${raw_response%$'\n'*}"

  if [[ "$http_status" != "200" ]]; then
    echo "ERROR: Login returned HTTP ${http_status}." >&2
    echo "$response_body" >&2
    echo "Check Eureka registration with: scripts/diagnose-gateway-503.sh" >&2
    return 1
  fi

  token="$(LOGIN_RESPONSE="$response_body" python3 - <<'PY'
import json
import os
body = json.loads(os.environ["LOGIN_RESPONSE"])
print(body.get("token") or body.get("accessToken") or body.get("access_token") or "")
PY
)" || return 1

  user_id="$(LOGIN_RESPONSE="$response_body" python3 - <<'PY'
import json
import os
body = json.loads(os.environ["LOGIN_RESPONSE"])
print(body.get("id") or body.get("userId") or body.get("user_id") or "")
PY
)" || return 1

  if [[ -z "$token" ]]; then
    echo "ERROR: Login returned HTTP 200, but no token field was found." >&2
    echo "$response_body" >&2
    return 1
  fi

  if [[ -z "$user_id" ]]; then
    echo "ERROR: Login returned HTTP 200, but no user ID field was found." >&2
    echo "$response_body" >&2
    return 1
  fi

  export TOKEN="$token"
  export USER_ID="$user_id"
  export GATEWAY_URL="$gateway_url"

  echo "Authenticated as ${username}"
  echo "USER_ID=${USER_ID}"
  echo "TOKEN length=${#TOKEN}, prefix=${TOKEN:0:12}..."
}

finance_login_and_export
login_status=$?
unset -f finance_login_and_export
return "$login_status" 2>/dev/null || exit "$login_status"

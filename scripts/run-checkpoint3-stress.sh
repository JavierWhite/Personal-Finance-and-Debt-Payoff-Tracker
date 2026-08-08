#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8090}"
FINANCE_USERNAME="${FINANCE_USERNAME:-javier}"
FINANCE_PASSWORD="${FINANCE_PASSWORD:-Password123!}"
RAMP_USERS="${RAMP_USERS:-20}"
STEADY_USERS_PER_SECOND="${STEADY_USERS_PER_SECOND:-10}"
PEAK_USERS_PER_SECOND="${PEAK_USERS_PER_SECOND:-30}"
P95_MS="${P95_MS:-1500}"

mvn -f performance-tests/pom.xml gatling:test \
  -DbaseUrl="$BASE_URL" \
  -Dusername="$FINANCE_USERNAME" \
  -Dpassword="$FINANCE_PASSWORD" \
  -DrampUsers="$RAMP_USERS" \
  -DsteadyUsersPerSecond="$STEADY_USERS_PER_SECOND" \
  -DpeakUsersPerSecond="$PEAK_USERS_PER_SECOND" \
  -Dp95Ms="$P95_MS"

echo
echo "Gatling reports: performance-tests/target/gatling/"

#!/usr/bin/env bash
set -euo pipefail
export SPRING_PROFILES_ACTIVE=dev
docker compose up --build

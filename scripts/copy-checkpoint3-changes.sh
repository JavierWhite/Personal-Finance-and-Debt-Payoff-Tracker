#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 /path/to/existing-project"
  exit 1
fi

SOURCE_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_ROOT="$(realpath "$1")"
MANIFEST="$SOURCE_ROOT/CHECKPOINT3-CHANGED-FILES.txt"

if [[ ! -f "$TARGET_ROOT/pom.xml" ]]; then
  echo "Target does not look like the finance tracker project: $TARGET_ROOT"
  exit 1
fi

rsync -avc --itemize-changes \
  --files-from="$MANIFEST" \
  "$SOURCE_ROOT/" \
  "$TARGET_ROOT/"

echo
echo "Checkpoint 3 new and edited files copied to: $TARGET_ROOT"
echo "Next: cd '$TARGET_ROOT' && mvn clean package -DskipTests"

#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

for deb in release-assets/*.deb; do
  echo "Uploading $deb..."

  curl -f -u "$NEXUS_USER:$NEXUS_PASSWORD" \
    -H "Content-Type: multipart/form-data" \
    --data-binary "@$deb" \
    "$NEXUS_URL"

done
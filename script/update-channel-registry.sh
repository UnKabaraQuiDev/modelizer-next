#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -lt 8 ]; then
  echo "Usage: $0 <registry-file> <channel> <version> <tag> <release-url> <asset-url> <platforms-json> <notes>" >&2
  exit 1
fi

REGISTRY_FILE="$1"
CHANNEL="$2"
VERSION="$3"
TAG="$4"
RELEASE_URL="$5"
ASSET_URL="$6"
PLATFORMS_JSON="$7"
NOTES="$8"

mkdir -p "$(dirname "${REGISTRY_FILE}")"
cat > "${REGISTRY_FILE}" <<JSON
{
  "channel": "${CHANNEL}",
  "version": "${VERSION}",
  "tag": "${TAG}",
  "releaseUrl": "${RELEASE_URL}",
  "assetUrl": "${ASSET_URL}",
  "platforms": ${PLATFORMS_JSON},
  "notes": "${NOTES}"
}
JSON

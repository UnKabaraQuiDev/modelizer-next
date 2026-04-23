#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -lt 6 ]; then
  echo "Usage: $0 <registry-file> <channel> <version> <tag> <release-url> <assets-json> [notes]" >&2
  exit 1
fi

REGISTRY_FILE="$1"
CHANNEL="$2"
VERSION="$3"
TAG="$4"
RELEASE_URL="$5"
ASSETS_JSON="$6"
NOTES="${7:-}"
MAX_ENTRIES="${MAX_ENTRIES:-20}"
PUBLISHED_AT="${PUBLISHED_AT:-$(date -u +%Y-%m-%dT%H:%M:%SZ)}"

mkdir -p "$(dirname "${REGISTRY_FILE}")"

python3 - "$REGISTRY_FILE" "$CHANNEL" "$VERSION" "$TAG" "$RELEASE_URL" "$ASSETS_JSON" "$NOTES" "$MAX_ENTRIES" "$PUBLISHED_AT" <<'PY'
import json
import sys
from pathlib import Path

registry_file = Path(sys.argv[1])
channel = sys.argv[2]
version = sys.argv[3]
tag = sys.argv[4]
release_url = sys.argv[5]
assets = json.loads(sys.argv[6])
notes = sys.argv[7]
max_entries = int(sys.argv[8])
published_at = sys.argv[9]

if registry_file.exists():
    data = json.loads(registry_file.read_text(encoding='utf-8'))
else:
    data = {}

platforms = []
for asset in assets:
    platform = asset.get('platform')
    if platform and platform not in platforms:
        platforms.append(platform)

primary_asset_url = ''
for preferred_kind in ('app-standalone-jar', 'bootstrap-jar', 'app-standalone-native', 'bootstrap-native'):
    for asset in assets:
        if asset.get('kind') == preferred_kind and asset.get('url'):
            primary_asset_url = asset.get('url', '')
            break
    if primary_asset_url:
        break
if not primary_asset_url and assets:
    primary_asset_url = assets[0].get('url', '')

entry = {
    'version': version,
    'tag': tag,
    'releaseUrl': release_url,
    'assetUrl': primary_asset_url,
    'url': primary_asset_url,
    'platforms': platforms,
    'notes': notes,
    'publishedAt': published_at,
    'assets': assets,
}

entries = []
for existing in data.get('entries', []):
    if existing.get('version') == version or existing.get('tag') == tag:
        continue
    entries.append(existing)

entries.insert(0, entry)
entries = entries[:max_entries]
latest = entries[0] if entries else entry

output = {
    'channel': channel,
    'version': latest.get('version', ''),
    'tag': latest.get('tag', ''),
    'releaseUrl': latest.get('releaseUrl', ''),
    'assetUrl': latest.get('assetUrl', ''),
    'url': latest.get('url', ''),
    'platforms': latest.get('platforms', []),
    'notes': latest.get('notes', ''),
    'updatedAt': published_at,
    'entries': entries,
}

registry_file.write_text(json.dumps(output, indent=2) + '\n', encoding='utf-8')
PY

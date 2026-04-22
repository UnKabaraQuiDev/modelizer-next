#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <registry-dir> <output-file>" >&2
  exit 1
fi

REGISTRY_DIR="$1"
OUTPUT_FILE="$2"
mkdir -p "$(dirname "$OUTPUT_FILE")"

python3 - "$REGISTRY_DIR" "$OUTPUT_FILE" <<'PY'
import json
import sys
from pathlib import Path

registry_dir = Path(sys.argv[1])
output_file = Path(sys.argv[2])

channels = {}
for channel in ('release', 'snapshot', 'nightly'):
    path = registry_dir / f'{channel}.json'
    if path.exists():
        data = json.loads(path.read_text(encoding='utf-8'))
    else:
        data = {}
    channels[channel] = {
        'version': data.get('version', ''),
        'url': data.get('url', data.get('assetUrl', '')),
        'releaseUrl': data.get('releaseUrl', ''),
        'notes': data.get('notes', ''),
        'tag': data.get('tag', ''),
    }

output_file.write_text(json.dumps(channels, indent=2) + '\n', encoding='utf-8')
PY

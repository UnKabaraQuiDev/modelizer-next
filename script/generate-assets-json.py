#!/usr/bin/env python3
import json
import os
import sys
from pathlib import Path

if len(sys.argv) != 2:
    raise SystemExit("Usage: generate-assets-json.py <assets-directory>")

repo = os.environ["REPOSITORY"]
tag = os.environ["RELEASE_TAG"]
assets_dir = Path(sys.argv[1])
assets = []
for path in sorted(assets_dir.iterdir()):
    if not path.is_file():
        continue
    name = path.name
    platform = 'linux' if '-linux-' in name else 'windows' if '-windows-' in name else 'standalone'
    kind = 'bootstrap'
    if name.startswith('modelizer-next-app-standalone-'):
        kind = 'app-standalone-native'
    elif name.startswith('modelizer-next-app-') and name.endswith('.jar'):
        kind = 'app-standalone-jar'
    elif name.startswith('modelizer-next-bootstrap-') and name.endswith('.jar'):
        kind = 'bootstrap-jar'
    elif name.startswith('modelizer-next-bootstrap-'):
        kind = 'bootstrap-native'
    assets.append({
        'name': name,
        'platform': platform,
        'kind': kind,
        'url': f'https://github.com/{repo}/releases/download/{tag}/{name}'
    })
print(json.dumps(assets))

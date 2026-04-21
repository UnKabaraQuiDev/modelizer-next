#!/usr/bin/env bash
set -euo pipefail

REMOTE="${1:-origin}"

MVN_VERSION=$(mvn -B help:evaluate -Dexpression=project.version -q -DforceStdout)
MVN_VERSION=$(echo "$MVN_VERSION" | tr -d '\r\n')

MVN_VERSION="${MVN_VERSION%-SNAPSHOT}"

BASE_TS=$(date -u -d "2026-01-01 00:00:00" +%s)
NOW_TS=$(date -u +%s)

MINUTES=$(( (NOW_TS - BASE_TS) / 60 ))

TAG="${MVN_VERSION}-SNAPSHOT-${MINUTES}"

echo "Using tag: $TAG"

git fetch

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Working tree is not clean. Commit or stash changes first."
  exit 1
fi

git tag -f -a "$TAG" -m "Snapshot build $TAG"

git push "$REMOTE" "$TAG" --force

echo "Tag pushed (force): $TAG"
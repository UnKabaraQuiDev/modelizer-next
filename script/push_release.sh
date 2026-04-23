#!/usr/bin/env bash
set -euo pipefail

REMOTE="${1:-origin}"

MVN_VERSION=$(mvn -B help:evaluate -Dexpression=project.version -q -DforceStdout)
MVN_VERSION=$(echo "$MVN_VERSION" | tr -d '\r\n')

if [[ "$MVN_VERSION" == *-SNAPSHOT ]]; then
  echo "Refusing to create a release tag from a snapshot version: $MVN_VERSION"
  exit 1
fi

BASE_TS=$(date -u -d "2026-01-01 00:00:00" +%s)
NOW_TS=$(date -u +%s)
MINUTES=$(( (NOW_TS - BASE_TS) / 60 ))

TAG="${MVN_VERSION}-RELEASE-${MINUTES}"

echo "Using tag: $TAG"

git fetch "$REMOTE" --tags

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Working tree is not clean. Commit or stash changes first."
  exit 1
fi

git tag -f -a "$TAG" -m "$TAG"
git push "$REMOTE" "$TAG" --force

echo "Tag pushed (force): $TAG"

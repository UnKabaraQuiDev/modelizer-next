#!/usr/bin/env bash
set -euo pipefail

REMOTE="${1:-origin}"

MVN_VERSION=$(mvn -B help:evaluate -Dexpression=project.version -q -DforceStdout)
MVN_VERSION=$(echo "$MVN_VERSION" | tr -d '\r\n')
MVN_VERSION="${MVN_VERSION%-SNAPSHOT}"

RELEASE_TAG="$(git describe --tags --match='*-RELEASE-*' --abbrev=0)" || exit 1
COMMIT_COUNT="$(git rev-list --count "$RELEASE_TAG"..HEAD)"

TAG="${MVN_VERSION}-SNAPSHOT-${COMMIT_COUNT}"

echo "Using tag: $TAG"

git fetch "$REMOTE" --tags

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Working tree is not clean. Commit or stash changes first."
  exit 1
fi

git tag -f -a "$TAG" -m "$TAG"
git push "$REMOTE" "$TAG" --force

echo "Tag pushed (force): $TAG"

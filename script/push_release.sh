#!/usr/bin/env bash
set -euo pipefail

REMOTE="${1:-origin}"

MVN_VERSION=$(mvn -B help:evaluate -Dexpression=project.version -q -DforceStdout)
MVN_VERSION=$(echo "$MVN_VERSION" | tr -d '\r\n')

if [[ "$MVN_VERSION" == *-SNAPSHOT ]]; then
  echo "Refusing to create a release tag from a snapshot version: $MVN_VERSION"
  exit 1
fi

TAG_PREFIX="${MVN_VERSION}-RELEASE"

LAST_TAG=$(git tag -l "${TAG_PREFIX}-0" --sort=-version:refname | head -n1)

if [[ -z "$LAST_TAG" ]]; then
  COMMIT_COUNT=0
else
  COMMIT_COUNT=$(git rev-list --count "${LAST_TAG}..HEAD")
fi

TAG="${TAG_PREFIX}-${COMMIT_COUNT}"

echo "Last release tag: ${LAST_TAG:-none}"
echo "Commits since last release: $COMMIT_COUNT"
echo "Release tag: $TAG"

git fetch "$REMOTE" --tags

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Working tree is not clean. Commit or stash changes first."
  exit 1
fi

git tag -f -a "$TAG" -m "$TAG"
git push "$REMOTE"
git push "$REMOTE" "$TAG" --force

echo "Tag pushed (force): $TAG"

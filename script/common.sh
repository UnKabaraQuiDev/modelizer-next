#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ARTIFACTS_ROOT="${ROOT_DIR}/artifacts"

compute_build_metadata() {
  local channel="$1"
  local platform="$2"

  local date
  date="$(date +%Y%m%d)"

  local base_version
  base_version="$(mvn -B help:evaluate -Dexpression=project.version -q -DforceStdout)"
  base_version="${base_version%-SNAPSHOT}"

  local version
  local app_version
  local tag_prefix
  local prerelease
  case "${channel}" in
    nightly)
      version="${base_version}-NIGHTLY${date}"
      app_version="${base_version}.78.${date}"
      tag_prefix="nightly"
      prerelease="true"
      ;;
    snapshot)
      version="${base_version}-SNAPSHOT${date}"
      app_version="${base_version}.115.${date}"
      tag_prefix="snapshot"
      prerelease="true"
      ;;
    release)
      version="${base_version}-RELEASE${date}"
      app_version="${base_version}.114.${date}"
      tag_prefix="release"
      prerelease="false"
      ;;
    *)
      echo "Unsupported channel: ${channel}" >&2
      exit 1
      ;;
  esac

  BUILD_DATE="${date}"
  BASE_VERSION="${base_version}"
  VERSION="${version}"
  APP_VERSION="${app_version}"
  CHANNEL="${channel}"
  PLATFORM="${platform}"
  TAG_PREFIX="${tag_prefix}"
  PRERELEASE="${prerelease}"

  export BUILD_DATE BASE_VERSION VERSION APP_VERSION CHANNEL PLATFORM TAG_PREFIX PRERELEASE
}

release_tag_for_channel() {
  local channel="$1"
  local version="$2"
  case "${channel}" in
    release)
      printf 'v%s' "${version}"
      ;;
    *)
      printf '%s-%s' "${channel}" "${version}"
      ;;
  esac
}

stage_artifacts() {
  local channel="$1"
  local platform="$2"
  local out_dir="${ARTIFACTS_ROOT}/${channel}/${platform}"

  rm -rf "${out_dir}"
  mkdir -p "${out_dir}"

  local bootstrap_src="modelizer-next-bootstrap/target/modelizer-next-bootstrap-${VERSION}.jar"
  if [ ! -f "${bootstrap_src}" ]; then
    bootstrap_src="modelizer-next-bootstrap/target/modelizer-next-bootstrap-${VERSION}-shaded.jar"
  fi
  cp "${bootstrap_src}" "${out_dir}/modelizer-next-bootstrap-${platform}-${VERSION}.jar"

  local app_src="modelizer-next-app/target/modelizer-next-app-${VERSION}.jar"
  if [ ! -f "${app_src}" ]; then
    app_src="modelizer-next-app/target/modelizer-next-app-${VERSION}-shaded.jar"
  fi
  cp "${app_src}" "${out_dir}/modelizer-next-app-${platform}-${VERSION}.jar"
  
  if [ "${platform}" = "windows" ]; then
    cp modelizer-next-bootstrap/target/dist/windows/*.exe "${out_dir}/"
    cp modelizer-next-app/target/dist/windows/*.exe "${out_dir}/"
  else
    tar -C modelizer-next-bootstrap/target/dist/linux -czf \
      "${out_dir}/modelizer-next-bootstrap-linux-${VERSION}-app-image.tar.gz" .
    tar -C modelizer-next-app/target/dist/linux -czf \
      "${out_dir}/modelizer-next-app-linux-${VERSION}-app-image.tar.gz" .
  fi

  cat > "${out_dir}/build-info.json" <<JSON
{
  "channel": "${CHANNEL}",
  "platform": "${PLATFORM}",
  "baseVersion": "${BASE_VERSION}",
  "version": "${VERSION}",
  "appVersion": "${APP_VERSION}",
  "releaseTag": "$(release_tag_for_channel "${CHANNEL}" "${VERSION}")",
  "prerelease": ${PRERELEASE}
}
JSON
}

run_build() {
  local channel="$1"
  local platform="$2"
  local extra_profiles
  local discriminator

  compute_build_metadata "${channel}" "${platform}"

  if [ "${platform}" = "windows" ]; then
    extra_profiles="native-windows,standalone-native-windows"
  else
    extra_profiles="native-linux,standalone-native-linux"
  fi

  discriminator="Automated ${channel} build ${BUILD_DATE} (${platform})"

  echo "Starting ${platform} ${channel} build"
  echo "Version [${channel^^}]: ${VERSION} (${BASE_VERSION}) = ${APP_VERSION}"

  mvn -B -DskipTests -Drevision="${VERSION}" -DappVersion="${APP_VERSION}" \
    -Ddistributor="${discriminator}" \
    -Pall,${extra_profiles} clean package

  stage_artifacts "${channel}" "${platform}"

  echo "${VERSION}" > "${ARTIFACTS_ROOT}/${channel}/${platform}/version.txt"
  echo "${APP_VERSION}" > "${ARTIFACTS_ROOT}/${channel}/${platform}/app-version.txt"
  release_tag_for_channel "${channel}" "${VERSION}" > "${ARTIFACTS_ROOT}/${channel}/${platform}/release-tag.txt"
}

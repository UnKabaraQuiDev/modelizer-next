#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ARTIFACTS_ROOT="${ROOT_DIR}/artifacts"

sanitize_base_for_app_version() {
  local base_version="$1"
  local sanitized
  sanitized="${base_version%%-*}"
  if [[ ! "${sanitized}" =~ ^[0-9]+(\.[0-9]+)*$ ]]; then
    sanitized="0.0"
  fi
  echo "${sanitized}"
}

sanitize_base_for_public_version() {
  sanitize_base_for_app_version "$1"
}

channel_code() {
  case "$1" in
    nightly) echo "1" ;;
    snapshot) echo "2" ;;
    release) echo "3" ;;
    *)
      echo "Unsupported channel: $1" >&2
      return 1
      ;;
  esac
}

channel_upper() {
  case "$1" in
    nightly) echo "NIGHTLY" ;;
    snapshot) echo "SNAPSHOT" ;;
    release) echo "RELEASE" ;;
    *)
      echo "Unsupported channel: $1" >&2
      return 1
      ;;
  esac
}

channel_prerelease() {
  case "$1" in
    nightly|snapshot) echo "true" ;;
    release) echo "false" ;;
    *)
      echo "Unsupported channel: $1" >&2
      return 1
      ;;
  esac
}

compute_build_metadata() {
  local channel="$1"
  local platform="${2:-any}"
  local version_override="${3:-${VERSION_OVERRIDE:-}}"

  local base_version
  local raw_base_version
  local timestamp_date
  local timestamp_time
  local channel_name
  channel_name="$(channel_upper "${channel}")"

  if [[ -n "${version_override}" ]]; then
    if [[ ! "${version_override}" =~ ^([0-9]+(\.[0-9]+)*)-(${channel_name})-([0-9]{4}-[0-9]{2}-[0-9]{2})_([0-9]{2}-[0-9]{2}-[0-9]{2})$ ]]; then
      echo "Version '${version_override}' does not match expected format '<x.y>- ${channel_name} -YYYY-MM-DD_HH-MM-SS'" >&2
      exit 1
    fi
    base_version="${BASH_REMATCH[1]}"
    raw_base_version="${BASH_REMATCH[1]}"
    timestamp_date="${BASH_REMATCH[4]}"
    timestamp_time="${BASH_REMATCH[5]}"
  else
    raw_base_version="$(mvn -B help:evaluate -Dexpression=project.version -q -DforceStdout)"
    base_version="$(sanitize_base_for_public_version "${raw_base_version}")"
    timestamp_date="$(date -u +%Y-%m-%d)"
    timestamp_time="$(date -u +%H-%M-%S)"
    version_override="${base_version}-${channel_name}-${timestamp_date}_${timestamp_time}"
  fi

  local app_version_base
  app_version_base="$(sanitize_base_for_app_version "${base_version}")"

  local build_timestamp_utc
  build_timestamp_utc="${timestamp_date} ${timestamp_time//-/:}"

  local epoch_timestamp
  epoch_timestamp="$(date -u -d '2026-01-01 00:00:00' +%s)"

  local build_timestamp_seconds
  build_timestamp_seconds="$(date -u -d "${build_timestamp_utc}" +%s)"

  if (( build_timestamp_seconds < epoch_timestamp )); then
    echo "Build timestamp '${build_timestamp_utc}' is before 2026-01-01 00:00:00 UTC" >&2
    exit 1
  fi

  local minutes_since_epoch
  minutes_since_epoch="$(( (build_timestamp_seconds - epoch_timestamp) / 60 ))"

  local app_version="${app_version_base}.$(channel_code "${channel}").${minutes_since_epoch}"
  local prerelease="$(channel_prerelease "${channel}")"

  BUILD_DATE="${timestamp_date}"
  BUILD_TIME="${timestamp_time}"
  BUILD_TIMESTAMP="${timestamp_date}_${timestamp_time}"
  BASE_VERSION="${base_version}"
  RAW_BASE_VERSION="${raw_base_version}"
  VERSION="${version_override}"
  APP_VERSION="${app_version}"
  CHANNEL="${channel}"
  PLATFORM="${platform}"
  PRERELEASE="${prerelease}"
  RELEASE_TAG="${VERSION}"

  export BUILD_DATE BUILD_TIME BUILD_TIMESTAMP BASE_VERSION RAW_BASE_VERSION VERSION APP_VERSION CHANNEL PLATFORM PRERELEASE RELEASE_TAG
}

emit_github_outputs() {
  local output_file="$1"
  cat >> "${output_file}" <<OUT
version=${VERSION}
app_version=${APP_VERSION}
channel=${CHANNEL}
platform=${PLATFORM}
release_tag=${RELEASE_TAG}
base_version=${BASE_VERSION}
prerelease=${PRERELEASE}
build_date=${BUILD_DATE}
build_time=${BUILD_TIME}
OUT
}

stage_shared_artifacts() {
  local channel="$1"
  local out_dir="${ARTIFACTS_ROOT}/${channel}/shared"

  rm -rf "${out_dir}"
  mkdir -p "${out_dir}"

  local bootstrap_src="modelizer-next-bootstrap/target/modelizer-next-bootstrap-${VERSION}-with-dependencies.jar"
  if [ ! -f "${bootstrap_src}" ]; then
    bootstrap_src="modelizer-next-bootstrap/target/modelizer-next-bootstrap-${VERSION}-with-dependencies.jar"
  fi
  cp "${bootstrap_src}" "${out_dir}/modelizer-next-bootstrap-${VERSION}.jar"

  local app_src="modelizer-next-app/target/modelizer-next-app-${VERSION}-with-dependencies.jar"
  if [ ! -f "${app_src}" ]; then
    app_src="modelizer-next-app/target/modelizer-next-app-${VERSION}-with-dependencies.jar"
  fi
  cp "${app_src}" "${out_dir}/modelizer-next-app-${VERSION}.jar"
}

find_single_file() {
  local directory="$1"
  local pattern="$2"
  local file
  file="$(find "${directory}" -maxdepth 1 -type f -name "${pattern}" | head -n 1 || true)"
  if [ -z "${file}" ]; then
    echo "Missing file matching ${pattern} in ${directory}" >&2
    exit 1
  fi
  echo "${file}"
}

stage_platform_artifacts() {
  local channel="$1"
  local platform="$2"
  local out_dir="${ARTIFACTS_ROOT}/${channel}/${platform}"

  rm -rf "${out_dir}"
  mkdir -p "${out_dir}"

  if [ "${platform}" = "windows" ]; then
    local bootstrap_exe
    bootstrap_exe="$(find_single_file "modelizer-next-bootstrap/target/dist/windows" '*.exe')"
    local app_exe
    app_exe="$(find_single_file "modelizer-next-app/target/dist/windows" '*.exe')"

    cp "${bootstrap_exe}" "${out_dir}/modelizer-next-bootstrap-${platform}-${VERSION}.exe"
    cp "${app_exe}" "${out_dir}/modelizer-next-app-portable-${platform}-${VERSION}.exe"
  else
    local bootstrap_dir
    bootstrap_dir="$(find modelizer-next-bootstrap/target/dist/linux -mindepth 1 -maxdepth 1 -type d | head -n 1 || true)"
    local app_dir
    app_dir="$(find modelizer-next-app/target/dist/linux -mindepth 1 -maxdepth 1 -type d | head -n 1 || true)"

    if [ -z "${bootstrap_dir}" ] || [ -z "${app_dir}" ]; then
      echo "Missing Linux app-image directory in target/dist/linux" >&2
      exit 1
    fi

    tar -C "$(dirname "${bootstrap_dir}")" -czf \
      "${out_dir}/modelizer-next-bootstrap-${platform}-${VERSION}.tar.gz" \
      "$(basename "${bootstrap_dir}")"
    tar -C "$(dirname "${app_dir}")" -czf \
      "${out_dir}/modelizer-next-app-portable-${platform}-${VERSION}.tar.gz" \
      "$(basename "${app_dir}")"
  fi
}

run_shared_build() {
  local channel="$1"
  compute_build_metadata "${channel}" "shared"

  echo "Starting shared ${channel} build"
  echo "Version [$(channel_upper "${channel}")]: ${VERSION} (${BASE_VERSION}) = ${APP_VERSION}"

  mvn -B -DskipTests -Drevision="${VERSION}" -DappVersion="${APP_VERSION}" \
    -Ddistributor="Automated ${channel} build ${BUILD_TIMESTAMP} (shared)" \
    -Pall clean package

  stage_shared_artifacts "${channel}"
}

run_platform_build() {
  local channel="$1"
  local platform="$2"
  local extra_profiles
  compute_build_metadata "${channel}" "${platform}"

  if [ "${platform}" = "windows" ]; then
    extra_profiles="native-windows,standalone,standalone-native-windows"
  else
    extra_profiles="native-linux,standalone,standalone-native-linux"
  fi

  echo "Starting ${platform} ${channel} native build"
  echo "Version [$(channel_upper "${channel}")]: ${VERSION} (${BASE_VERSION}) = ${APP_VERSION}"

  mvn -B -DskipTests -Drevision="${VERSION}" -DappVersion="${APP_VERSION}" \
    -Ddistributor="Automated ${channel} build ${BUILD_TIMESTAMP} (${platform})" \
    -Pall,${extra_profiles} clean package

  stage_platform_artifacts "${channel}" "${platform}"
}

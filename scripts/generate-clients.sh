#!/usr/bin/env bash
set -euo pipefail

OPENAPI_DIR="${1:?target/openapi}"
# OUT_ROOT="${2:?target/generated-sources}"
OUT_ROOT="${2:-src/main/java}"   # default to src/gen/java if not passed 
GEN_VER="${3:?7.12.0}"

mkdir -p "$OUT_ROOT"
TOOLS_DIR="$(dirname "$OUT_ROOT")/tools"
CLI_JAR="$TOOLS_DIR/openapi-generator-cli-${GEN_VER}.jar"
LOG_DIR="$(dirname "$OUT_ROOT")/openapi-gen-logs"
SRC_FOLDER="/"    # <— single, shared source root
mkdir -p "$TOOLS_DIR" "$LOG_DIR"

# Download CLI if missing
if [ ! -f "$CLI_JAR" ]; then
  echo "==> Downloading openapi-generator-cli ${GEN_VER}"
  curl -fsSL -o "$CLI_JAR" \
    "https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/${GEN_VER}/openapi-generator-cli-${GEN_VER}.jar"
fi

shopt -s nullglob
SPECS=("$OPENAPI_DIR"/*.json "$OPENAPI_DIR"/*.yaml "$OPENAPI_DIR"/*.yml)
if [ ${#SPECS[@]} -eq 0 ]; then
  echo "No OpenAPI specs found in ${OPENAPI_DIR}"
  exit 0
fi

OK=0
FAIL=0
FAILED_LIST=()

for SPEC in "${SPECS[@]}"; do
  SVC="$(basename "$SPEC")"
  SVC="${SVC%.*}"

  OUT_DIR="$OUT_ROOT"                          # <— single root (NOT per service)
  LOG_FILE="$LOG_DIR/${SVC}.log"

  # lowercase + safe
  SVC_PKG="$(echo "$SVC" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9]/_/g')"
  API_SUFFIX="$(echo "$SVC" | sed -E 's/[^A-Za-z0-9]+//g' | sed -E 's/^([a-z])/\U\1/')Api"

  echo "==> Generating client for ${SVC}"

  # Optional: wipe this service’s package before regenerating to remove stale files
  rm -rf "${OUT_DIR}/${SRC_FOLDER}/com/ticketsystem/${SVC_PKG}/client" || true
  rm -rf "${OUT_DIR}/${SRC_FOLDER}/com/ticketsystem/${SVC_PKG}/client/model" || true
  rm -rf "${OUT_DIR}/${SRC_FOLDER}/com/ticketsystem/${SVC_PKG}/client/invoker" || true

  if java -jar "$CLI_JAR" generate \
      -g spring \
      --library spring-cloud \
      -i "$SPEC" \
      -o "$OUT_DIR" \
      -p sourceFolder="${SRC_FOLDER}" \
      -p apiPackage="com.ticketsystem.${SVC_PKG}.client.api" \
      -p modelPackage="com.ticketsystem.${SVC_PKG}.client.model" \
      -p invokerPackage="com.ticketsystem.${SVC_PKG}.client.invoker" \
      --additional-properties useSpringBoot3=true,useJakartaEe=true,dateLibrary=java8,interfaceOnly=true,useTags=true,hideGenerationTimestamp=true,apiNameSuffix="${API_SUFFIX}",modelNameSuffix=Dto \
      --global-property models,apis,modelDocs=false,apiDocs=false,supportingFiles=false \
      --skip-validate-spec \
      --enable-post-process-file \
      >"$LOG_FILE" 2>&1; then
    echo "    OK: ${SVC} (logs: ${LOG_FILE})"
    OK=$((OK+1))
  else
    echo "    FAIL: ${SVC} (see ${LOG_FILE})"
    FAIL=$((FAIL+1))
    FAILED_LIST+=("$SVC")
  fi
done

echo
echo "==> Generation summary: OK=${OK} / FAIL=${FAIL}"
if [ "$FAIL" -gt 0 ]; then
  printf 'Failed services: %s\n' "${FAILED_LIST[*]}"
  echo "Continuing despite failures. Check logs under: ${LOG_DIR}"
  # exit 1
fi

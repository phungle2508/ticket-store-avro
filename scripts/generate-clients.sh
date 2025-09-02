#!/usr/bin/env bash
set -euo pipefail

OPENAPI_DIR="${1:?target/openapi}"
# OUT_ROOT="${2:?target/generated-sources}"
OUT_ROOT="${2:-src/main/java}" # default to src/main/java if not passed
GEN_VER="${3:?7.12.0}"

mkdir -p "$OUT_ROOT"
TOOLS_DIR="$(dirname "$OUT_ROOT")/tools"
CLI_JAR="$TOOLS_DIR/openapi-generator-cli-${GEN_VER}.jar"
LOG_DIR="$(dirname "$OUT_ROOT")/openapi-gen-logs"
SRC_FOLDER="/" # <— single, shared source root

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
    OUT_DIR="$OUT_ROOT" # <— single root (NOT per service)
    LOG_FILE="$LOG_DIR/${SVC}.log"
    
    # lowercase + safe
    SVC_PKG="$(echo "$SVC" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9]/_/g')"
    API_SUFFIX="$(echo "$SVC" | sed -E 's/[^A-Za-z0-9]+//g' | sed -E 's/^([a-z])/\U\1/')Api"
    
    echo "==> Generating Feign client for ${SVC}"
    
    # Optional: wipe this service's package before regenerating to remove stale files
    rm -rf "${OUT_DIR}/${SRC_FOLDER}/com/ticketsystem/${SVC_PKG}/client" || true
    rm -rf "${OUT_DIR}/${SRC_FOLDER}/com/ticketsystem/${SVC_PKG}/client/model" || true
    rm -rf "${OUT_DIR}/${SRC_FOLDER}/com/ticketsystem/${SVC_PKG}/client/invoker" || true
    # Also remove any test files that might have been generated in weird paths
    rm -rf "${OUT_DIR}/src/test/java/com/ticketsystem/${SVC_PKG}" || true
    rm -rf "${OUT_DIR}/${SRC_FOLDER}/src/test" || true
    
    if java -jar "$CLI_JAR" generate \
        -g java \
        --library feign \
        -i "$SPEC" \
        -o "$OUT_DIR" \
        -p sourceFolder="${SRC_FOLDER}" \
        -p apiPackage="com.ticketsystem.${SVC_PKG}.client.api" \
        -p modelPackage="com.ticketsystem.${SVC_PKG}.client.model" \
        -p invokerPackage="com.ticketsystem.${SVC_PKG}.client.invoker" \
        --additional-properties useJakartaEe=true,dateLibrary=java8,interfaceOnly=true,useTags=true,hideGenerationTimestamp=true,apiNameSuffix="${API_SUFFIX}" \
        --global-property models,apis,supportingFiles,modelTests=false,apiTests=false,modelDocs=false,apiDocs=false \
        --skip-validate-spec \
        --enable-post-process-file \
        >"$LOG_FILE" 2>&1; then
        
        # Post-generation cleanup: remove unwanted files but keep essential supporting classes
               # Post-generation cleanup
        # Keep ONLY the top-level 'com' package under the java source dir; delete anything else
        # SAFETY GUARD: only do this when OUT_DIR ends with /src/main/java
        if [[ "$OUT_DIR" =~ /src/main/java$ ]]; then
          # remove test/docs/build files the generator may create alongside src
          rm -rf "${OUT_DIR}/../test" 2>/dev/null || true
          rm -rf "$(dirname "$OUT_DIR")/docs" 2>/dev/null || true
          rm -f  "$(dirname "$OUT_DIR")/pom.xml" 2>/dev/null || true
          rm -f  "$(dirname "$OUT_DIR")/build.gradle" 2>/dev/null || true
          rm -f  "$(dirname "$OUT_DIR")/README.md" 2>/dev/null || true
          rm -f  "$(dirname "$OUT_DIR")/.openapi-generator-ignore" 2>/dev/null || true
          rm -rf "$(dirname "$OUT_DIR")/.openapi-generator" 2>/dev/null || true

          # now purge everything in src/main/java except 'com'
          find "$OUT_DIR" -mindepth 1 -maxdepth 1 ! -name com -exec rm -rf {} +
        else
          echo "WARN: Skipping 'keep-only-com' cleanup because OUT_DIR doesn't end with /src/main/java: $OUT_DIR"
        fi

        echo "   OK: ${SVC} (logs: ${LOG_FILE})"

        
        OK=$((OK+1))
    else
        echo " FAIL: ${SVC} (see ${LOG_FILE})"
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
#!/usr/bin/env bash
# Phase-1 CI build harness (hosted runner only; no local builds anywhere).
#
# Usage: ci_build.sh <project-dir> <output-dir> <result-prefix>
#
# Runs the three Phase-1 verification tasks one at a time on one Gradle project, records the
# honest pass/fail and wall-clock duration per task, fails only if a task fails, and exports
# the resolved dependency graph of the app module for the licence/dependency audit.
set -u

PROJECT_DIR=$1
OUT_DIR=$2
PREFIX=$3

cd "$PROJECT_DIR" || exit 2
chmod +x gradlew

mkdir -p "$OUT_DIR"
./gradlew --version > "$OUT_DIR/$PREFIX-gradle-version.txt" 2>&1 || true

STATUS=0
printf 'task,result,seconds\n' > "$OUT_DIR/$PREFIX-results.csv"
for task in :app:assembleDebug :app:testDebugUnitTest :app:lintDebug; do
  LOG="$OUT_DIR/$PREFIX${task//[:]/_}.log"
  START=$SECONDS
  if ./gradlew --stacktrace "$task" > "$LOG" 2>&1; then
    RESULT=PASS
  else
    RESULT=FAIL
    STATUS=1
  fi
  seconds=$((SECONDS - START))
  printf '%s,%s,%s\n' "$task" "$RESULT" "$seconds" | tee -a "$OUT_DIR/$PREFIX-results.csv"
  if [ "$RESULT" = "FAIL" ]; then
    tail -n 120 "$LOG"
  fi
done

# Resolved classpath for :app (configuration cache safe, informational only).
./gradlew :app:dependencies --configuration debugRuntimeClasspath \
  > "$OUT_DIR/$PREFIX-deps.txt" 2>&1 || true

# Gate-I evidence: raw JUnit XMLs + lint report (small files).
if [ -d app/build/test-results/testDebugUnitTest ]; then
  mkdir -p "$OUT_DIR/test-xml"
  cp app/build/test-results/testDebugUnitTest/*.xml "$OUT_DIR/test-xml/" 2>/dev/null || true
fi
for f in app/build/reports/lint-results-debug.html app/build/reports/lint-results-debug.txt; do
  [ -f "$f" ] && cp "$f" "$OUT_DIR/" || true
done

# APK delivery: verify assembleDebug actually emitted APKs, copy them, and dump their package
# metadata with the SDK's own aapt2 so reports can cite exact package/version/minSdk — never
# assumptions from a green task.
if compgen -G "app/build/outputs/apk/debug/*.apk" >/dev/null; then
  mkdir -p "$OUT_DIR/apk"
  cp app/build/outputs/apk/debug/*.apk "$OUT_DIR/apk/"
  ls -la app/build/outputs/apk/debug >> "$OUT_DIR/$PREFIX-apk-dir.txt"
  AAPT2="$(ls "$ANDROID_HOME"/build-tools/*/aapt2 2>/dev/null | sort -V | tail -1)"
  if [ -n "$AAPT2" ]; then
    for apk in "$OUT_DIR"/apk/*.apk; do
      "$AAPT2" dump badging "$apk" >> "$OUT_DIR/$PREFIX-apk-badging.txt" 2>&1 || true
    done
  else
    echo "aapt2 not found under ANDROID_HOME=$ANDROID_HOME" > "$OUT_DIR/$PREFIX-apk-badging.txt"
  fi
else
  echo "NO APK EMITTED under app/build/outputs/apk/debug/" > "$OUT_DIR/$PREFIX-apk-dir.txt"
fi

echo "--- $OUT_DIR ---"
ls -la "$OUT_DIR"
exit $STATUS

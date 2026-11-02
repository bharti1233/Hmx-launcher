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

echo "--- $OUT_DIR ---"
ls -la "$OUT_DIR"
exit $STATUS

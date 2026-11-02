# PHASE 1 — BUILD COMPATIBILITY

**Status: COMPLETE — PASS on the hosted runner (run 41485053843, run #3 vs baseline side-by-side).** The owner forbids local builds ("don't run or
build apk locally"), so no `gradlew` command was executed in the isolated workspace on this
machine. Hosted GitHub Actions is the only build executor. Both verification jobs are wired in
`.github/workflows/phase1.yml`; results are collected from the run artifacts.

## Method

Two jobs run the same three Gradle tasks on JDK 21 (Temurin; Temurin 17 installed as toolchain
insurance), each recorded per-task with duration and pass/fail into a CSV (`build_results.csv`
name: prefix-results.csv), plus the resolved `:app` classpath (`*-deps.txt`):

| Job | Gradle project | Tasks |
| --- | --- | --- |
| `baseline-ark` | upstream ARK at `803b66d7561cdd8cc45cff44a026624be919cef7`, fresh clone, **no modifications** | `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` |
| `spike-ark-backdrop` | this tree (`source/ark-launcher/`, = ARK + Backdrop 2.0.1 + spike additions) | same |

Expected evidence for Gate A:

1. Baseline builds (proves the pinned upstream builds on JDK 21 at all).
2. Spike builds (proves the added Backdrop dependency + spike code do not break that build).
3. `spike-deps.txt` shows `io.github.kyant0:backdrop:2.0.1` + `io.github.kyant0:shapes:1.2.1`
   and the resolved JB-compose→androidx mapping under BOM 2026.09.00.

## Results

Populated from the CI run recorded in `artifacts/` (durations are wall-clock from the runner):

## Results (run 41485053843, commit f6e624f5 — both jobs green)

Durations are wall-clock seconds from the runner (matches `artifacts/ci/*-results.csv`):

| Project | Task | Result | Duration | — | Task | Result | Duration |
| --- | --- | --- | --- | --- | --- | --- | --- |
| baseline-ark | :app:assembleDebug | **PASS** | 254 s | / | spike-ark-backdrop | :app:assembleDebug | **PASS** | 253 s |
| baseline-ark | :app:testDebugUnitTest | **PASS** | 58 s | / | spike-ark-backdrop | :app:testDebugUnitTest | **PASS** | 58 s |
| baseline-ark | :app:lintDebug | **PASS** | 156 s | / | spike-ark-backdrop | :app:lintDebug | **PASS** | 158 s |

Key readings:

- **Backdrop 2.0.1 added zero measurable build cost** (assemble 253 s vs baseline 254 s, tests
  58 s vs 58 s, lint 158 s vs 156 s) and resolved cleanly — no conflict, no forced exclamation
  needed in the Gradle reports (`artifacts/ci/spike-deps.txt`).
- History (honest): CI run #1 (commit `92deb7f5`) failed — spike-only Kotlin errors
  (`missing Composable import`, `Backdrop` vs `LayerBackdrop` typing, `Dp.roundToPx()` → Int
  instead of `toPx()` Float). One controlled fix commit (`40f4bc89`), run #2 green; final
  evidence run #3 (commit `f6e624f5`) green with the test-XML exporter enabled. Baseline was green
  on every run (upstream compiles on JDK 21 unmodified).
- Full logs of every task live in run artifacts (`phase1-spike-ci`, `phase1-baseline-ci`);
  per-run XMLs exported to `app/build/test-results/testDebugUnitTest` are preserved in
  `artifacts/ci/test-xml/`.

**Gate A verdict: PASS.** ARK + Backdrop 2.0.1 resolves and builds, tests and lint included.

## Known / controlled diffs from upstream

1. `app/build.gradle.kts`: + `implementation("io.github.kyant0:backdrop:2.0.1")`.
2. Spike sources under `com.hmx.launcher.spike` (4 Kotlin files, 1 vector drawble, 3 test files).
3. One manifest activity entry ("HMX Spike") in the isolated copy only.
4. No Gradle/AGP/Kotlin/SDK version changes; no dependency removed; nothing else touched.

Any build failure on CI will be reported in this report with the offending log excerpt, and a
single targeted fix will be applied (one controlled change at a time per the task instructions).

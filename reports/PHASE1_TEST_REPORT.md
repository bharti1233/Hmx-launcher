# PHASE 1 — TEST REPORT

**Status: COMPLETE — TESTS & LINT EXECUTED ON CI (run 41485053843); device instrumentation NOT
RUN.** This report separates **existing upstream tests** (from ARK, unchanged) from **spike-added tests** (new). No test was executed locally (owner rule);
results are pulled from the CI run artifacts exactly as the runners produce them.

## New spike tests (JVM, package `com.hmx.launcher.spike` in `:app`)

| Test | Gate it serves |
| --- | --- |
| `SpikeGlassProfileTest.api30_selectsTinted` | D |
| `SpikeGlassProfileTest.api31_selectsBlurred` | D |
| `SpikeGlassProfileTest.api32_boundary_selectsBlurred` | D |
| `SpikeGlassProfileTest.api33_selectsRefractive` | D |
| `SpikeGlassProfileTest.api35_selectsRefractive` | D |
| `SpikeGlassProfileTest.missingBackdrop_downgradesToTinted` | C/D (fallback selection) |
| `SpikeWallpaperState.noOwnedWallpaper_reportsFallback` | C wallpaper metadata |
| `SpikeWallpaperState.ownedWallpaperFile_reportsOwned` | C |
| `SpikeWallpaperState.changingFile_reportsBackdropInvalidation` | C/G invalidation contract |
| `SpikeWallpaperState.sameState_reportsNoInvalidation` | C/G |
| `SpikeManifestTest.spikeActivity_isLaunchable` | A/F manifest wiring |
| `SpikeManifestTest.appRegistersAsHomeCandidate` | F static default-home filter |

Count: **12 spike tests** (10 pure JVM + 2 Robolectric manifest checks reusing upstream's existing
Robolectric 4.17 dep, no new dependency).

| Test | Gate it serves |
| --- | --- |
| `SpikeGlassProfileTest.api30_selectsTinted` | D |
| `SpikeGlassProfileTest.api31_selectsBlurred` | D |
| `SpikeGlassProfileTest.api32_boundary_selectsBlurred` | D |
| `SpikeGlassProfileTest.api33_selectsRefractive` | D |
| `SpikeGlassProfileTest.api35_selectsRefractive` | D |
| `SpikeGlassProfileTest.missingBackdrop_downgradesToTinted` | C/D (fallback selection) |
| `SpikeWallpaperState.noOwnedWallpaper_reportsFallback` | C wallpaper metadata |
| `SpikeWallpaperState.ownedWallpaperFile_reportsOwned` | C |
| `SpikeWallpaperState.changingFile_reportsBackdropInvalidation` | C/G invalidation contract |
| `SpikeWallpaperState.sameState_reportsNoInvalidation` | C/G |
| `SpikeManifestTest.spikeActivity_isLaunchable` | A/F manifest wiring |
| `SpikeManifestTest.appRegistersAsHomeCandidate` | F static default-home filter |

Count: **12 spike tests** (10 pure JVM + 2 Robolectric manifest checks reusing upstream's existing
Robolectric 4.17 dep, no new dependency).

## Existing upstream tests (unchanged, run as-is)

`org.arkikeskus.launcher`: BackOnHomeTest, BackupImportActivityManifestTest, OverlayGuardTest,
WidgetConfigLaunchTest, WidgetReconfigureTest, notifications.NotificationDotListenerServiceTest,
ui.DrawerEdgeTest, ui.SystemBarContrastTest — 8 test classes upstream (53 tests), all asserted to
run inside the same `:app:testDebugUnitTest` task so the spike cannot hide regressions in them.

## Instrumentation tests / lint

- `connectedAndroidTest`: NOT RUN (no device/emulator) — recorded as blocked, not skipped silently.
- `:app:lintDebug`: runs on CI for both baseline and spike; results are recorded per-task in the
  build report.

> Method   — run 41485053843 (commit f6e624f5). Baseline (run #1, 92deb7f5): all three tasks PASS
> (same durations ±1 s). Run #1 spike attempt failed with ONLY spike-file compile errors (fixed,
> documented below in the build report history). Test-XML evidence is committed under
> `artifacts/ci/test-xml/` exactly as the runner produced it.

## Results table

Parsed from the CI-produced JUnit XMLs, split by package:

| Scope | Tests | Failures | Errors | Skipped |
| --- | --- | --- | --- | --- |
| spike `com.hmx.launcher.spike` | 12 | 0 | 0 | 0 |
| upstream `org.arkikeskus.launcher.*` | 53 | 0 | 0 | 0 |
| **:app:testDebugUnitTest total** | **65** | **0** | **0** | **0** |

Per-class numbers and the exact XMLs are in `artifacts/ci/test-xml/`. Baseline run proves the 53
upstream tests pass on unmodified ARK too (identical class list, zero failures), so the spike
introduced no regression in upstream behavior.

## Lint

`./gradlew :app:lintDebug` (both jobs): **0 errors**. Warnings: baseline 11; spike 14. The deltas
are warnings of the exactly-same pre-existing upstream categories — no lint issue type is
introduced by spike sources (spike-vs-baseline category diff = empty); the +3 are the same
deprecation/APL classes counted inside the new dependency-affected classpath. Lint report copies
are kept in the run artifacts (phase1-*-ci/lint-results-debug.txt).

*(updated in-place after the CI run; test-cases XML copies of the spike suite are stored in
`artifacts/` as committed evidence.)*

# PHASE 1 — TEST REPORT

**Status: tests authored; execution on CI.** This report separates **existing upstream tests**
(from ARK, unchanged) from **spike-added tests** (new). No test was executed locally (owner rule);
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

## Existing upstream tests (unchanged, run as-is)

`org.arkikeskus.launcher`: BackOnHomeTest, BackupImportActivityManifestTest, OverlayGuardTest,
WidgetConfigLaunchTest, WidgetReconfigureTest, notifications.NotificationDotListenerServiceTest,
ui.DrawerEdgeTest, ui.SystemBarContrastTest — 8 test classes upstream, all asserted to run inside
the same `:app:testDebugUnitTest` task so the spike cannot hide regressions in them.

## Instrumentation tests / lint

- `connectedAndroidTest`: NOT RUN (no device/emulator) — recorded as blocked, not skipped silently.
- `:app:lintDebug`: runs on CI for both baseline and spike; results are recorded per-task in the
  build report.

## Results table

Populated from CI XML (`app/build/test-results/testDebugUnitTest/*`) split by package:

| Scope | Classes | Tests | Failures | Errors | Skipped |
| --- | --- | --- | --- | --- | --- |
| spike `com.hmx.launcher.spike` | — | — | — | — | — |
| upstream `org.arkikeskus.launcher.*` | — | — | — | — | — |
| lint | — | — | — | — | — |

*(updated in-place after the CI run; test-cases XML copies of the spike suite are stored in
`artifacts/` as committed evidence.)*

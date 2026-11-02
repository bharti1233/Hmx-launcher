# PHASE 1 — SOURCE AUDIT

**Status: COMPLETE (source inspection only; no build, no runtime).**
Audit target: isolated spike copy of ARK upstream; production workspace untouched.

## Upstream identity

| Field | Value |
| --- | --- |
| Repository URL | https://github.com/jrs8205/ARK-launcher.git |
| Pinned commit | `803b66d7561cdd8cc45cff44a026624be919cef7` (2026-10-31, "fix(home): a placed widget keeps its minimum size with the widget inset") |
| Pinned commit verified at | clone HEAD of `/tmp/opencode/research/ARK` matches the pin exactly |
| Working tree | copied without `.git` into `source/ark-launcher/` (spike-owned tree) |
| Liquid Glass reference | production `third_party/AndroidLiquidGlass` @ `65ab177e90e5c1d8c62e70cf7755841982da65f6`, read-only, not modified |
| Licence (upstream) | Apache License 2.0 (`sdk LICENSE` verified) |

## Build system

| Item | Value | Source |
| --- | --- | --- |
| Gradle wrapper | 9.8.2 (`gradle-9.8.2-bin.zip`) | `gradle/wrapper/gradle-wrapper.properties` |
| JVM arguments | `-Xmx4g -Dfile.encoding=UTF-8 -XX:+UseParallelGC` | `gradle.properties` |
| Gradle features | parallel, build cache, configuration cache all enabled | `gradle.properties` |
| Lint | runs out-of-process by upstream decision (`android.experimental.runLintInProcess=false`) | `gradle.properties` |
| AGP | 9.5.0 | `gradle/libs.versions.toml` |
| Kotlin | 2.4.21 (root `buildscript` forces `kotlin-gradle-plugin:2.4.21` over AGP-9 built-in Kotlin, documented by upstream comments) | `build.gradle.kts`, toml |
| KSP | 2.3.12 (with the upstream `kotlin-metadata-jvm` 2.4 force-fix inside the Hilt convention plugin) | toml + `AndroidHiltConventionPlugin` |
| JDK requirement | Java 21 (`compileOptions` source/target = VERSION_21 in the application convention; **no global JDK pin found elsewhere**) | `AndroidApplicationConventionPlugin.kt` |
| SDK policy | compileSdk 37, targetSdk 37, minSdk 30 | `AndroidApplicationConventionPlugin.kt` |

The convention plugins apply: `arkikeskus.android.application` (app), `arkikeskus.android.library`
+ `arkikeskus.android.compose` (+ feature extras) to modules. App is `arkikeskus.android.application
+ arkikeskus.android.compose + arkikeskus.android.hilt`.

Conclusion on JDK 21: **matches owner's preference**; no source evidence contradicts it. CI uses
Temurin 21 with Temurin 17 installed as toolchain insurance.

## Module structure

```
:app                    (arkikeskus.launcher application, launcher UI wiring)
:core:model             :core:designsystem   :core:data   :core:launcher   :core:ui
:feature:home           :feature:appdrawer   :feature:settings   :feature:backup
```

Settings: `rootProject.name = "arkikeskus-launcher"`, repos = google() + mavenCentral(), included
build `build-logic`.

## Dependency versions (app classpath, from catalog)

| Family | Coordinates (artifact) | Version |
| --- | --- | --- |
| Compose | `androidx.compose:compose-bom` | 2026.09.00 (all ui/foundation/material3 versions BOM-managed) |
| AndroidX core | `androidx.core:core-ktx` 1.19.1, `core-splashscreen` 1.2.0, `activity-compose` 1.13.0 | above |
| Lifecycle | runtime-ktx / runtime-compose / viewmodel-compose 2.11.0 | above |
| Hilt | `com.google.dagger:hilt-android` 2.60.1 + `androidx.hilt:hilt-navigation-compose` 1.4.0 | above |
| Room | runtime/ktx/compiler + `androidx.room` Gradle plugin 2.8.5 | above |
| DataStore | `androidx.datastore:datastore-preferences` 1.3.0 | above |
| Coroutines | kotlinx-coroutines-android 1.11.0 | above |
| Images | coil 3.6.3 (compose/core/network-okhttp) | above |
| Widgets | Glance 1.2.0 (`androidx.glance:glance-appwidget`, glance-material3) | above |
| Navigation | Navigation 3 1.2.0 (runtime + ui) | above |
| Tests | JUnit 4.13.2, Google Truth 1.4.5, Robolectric 4.17, androidx test ext/runner/core 1.3.0/1.7.0/1.7.0, coroutines-test, room-testing | above |

Existing test deps **already include Robolectric** with `testOptions.unitTests.isIncludeAndroidResources = true`
in `:app` — spike manifest tests reuse this: no new dependency was added.

## Android app facts

- `applicationId org.arkikeskus.launcher`, versionCode 65, versionName 0.9.8; debug adds
  applicationIdSuffix `.debug` and app label "ARK-launcher debug"; release minifies (R8) and keeps
  F-Droid-agnostic packaging upstream details (keystore.properties optional, gitignored upstream).
- Manifest (`app/src/main/AndroidManifest.xml`):
  - `LauncherActivity` is the system entry: intent-filter MAIN + HOME + DEFAULT + LAUNCHER,
    `singleTask`, `stateNotNeeded`, portrait-locked, excluded from recents.
  - `SettingsActivity`, `PinRequestActivity` (CONFIRM_PIN_SHORTCUT / CONFIRM_PIN_APPWIDGET —
    upstream launcher already supports user-mediated pinning of shortcuts / widgets),
    `BackupImportActivity` (VIEW/SEND json), notification-listener service for dots.
  - Permissions all launcher-functional (EXPAND_STATUS_BAR, READ_CONTACTS, READ_CALENDAR,
    ACCESS_COARSE_LOCATION, READ_PHONE_STATE, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE, BLUETOOTH
    ≤ API 30, QUERY_ALL_PACKAGES, REQUEST_DELETE_PACKAGES, HIDE_OVERLAY_WINDOWS) — no spike
    permission was added; the spike adds no permissions.
- `LauncherApplication` is `@HiltAndroidApp`, provides Coil `ImageLoader`, `CustomIconStore`,
  `PendingWidgetIds` (SharedPreferences-backed) — confirms Hilt + widget-host wiring exists upstream.
- `LauncherActivity` uses `androidx.activity.compose.setContent` → `activity-compose` dependency
  exists on the app classpath, so the spike demo can reuse it.

## Existing tests (upstream, JVM via Robolectric where needed)

`app/src/test/kotlin/org/arkikeskus/launcher/`: `BackOnHomeTest`, `BackupImportActivityManifestTest`,
`OverlayGuardTest`, `WidgetConfigLaunchTest`, `WidgetReconfigureTest`,
`notifications/NotificationDotListenerServiceTest`, `ui/DrawerEdgeTest`, `ui/SystemBarContrastTest`.
These run as part of `:app:testDebugUnitTest` alongside the spike tests; results are reported
separately in `PHASE1_TEST_REPORT.md`.

## Backdrop 2.0.1 facts (verified directly from Maven Central, 2026-11-02)

- Coordinates `io.github.kyant0:backdrop:2.0.1`; name "Backdrop"; description
  "Compose Multiplatform Liquid Glass effects"; upstream project
  https://github.com/Kyant0/AndroidLiquidGlass (the same repo as the audited Glass reference).
- **Licence: The Apache License, Version 2.0** (POM).
- Kotlin-stdlib 2.4.10, JB-Compose foundation/ui/ui-graphics 1.12.0, `io.github.kyant0:shapes` 1.2.1
  (POM; itself Apache-2.0, "iOS-like shapes for Compose Multiplatform" — the `kyant-shapes` transitive
  dependency), org.jetbrains:annotations 26.1.0 — everything Apache/ASL-family licences.
- API surface verified by reading the published **sources jar** (`backdrop-2.0.1-sources.jar`):
  package `com.kyant.backdrop` with `drawBackdrop(backdrop, shape, effects, highlight, shadow, ...)`,
  `backdrops` (`rememberLayerBackdrop()`, `Modifier.layerBackdrop(...)`), `effects.blur(radius)`,
  `effects.lens(refractionHeight, refractionAmount, depthEffect, chromaticAberration)`,
  `Highlight.Default`, `Shadow.Default`; expect/actual `isRenderEffectSupported()`, 
  `isRuntimeShaderSupported()` with library-internal guarded no-ops (blur returns when unsupported;
  lens throws only for unsupported shapes — `RoundedCornerShape` is a supported `CornerBasedShape`).
- The spike code's API usage in `com/hmx/launcher/spike/*` was written against that verified
  surface (checked before writing, so the first CI compile is a real compatibility test, not a
  guess).

## Compose version conflict analysis

Backdrop ships JB-Multiplatform artifacts pinned at 1.12.0; the app uses the androidx Compose BOM
2026.09.00. Gradle resolves JB multiplatform artifacts to per-target android variants that
delegate to androidx artifacts, so the *effective* androidx artifact versions become the higher of
(BOM) vs (JB-mapped). Expected outcome: androidx compose libs resolve bright and BOM-aligned; no
incompatible-scope conflict known. **Resolved-graph evidence is captured on CI
(`spike-deps.txt`) and compared in the licence audit.** No kotlin-stdlib conflict: upstream 2.4.21
std lib is newer than backdrop's 2.4.10 runtime pin — resolution takes the higher (2.4.21).

## Phase-1 spike decision: where spike code lives

Earlier planning sketched a `:feature:spike` Gradle module. The final decision (kept small and
reversible): **spike code stays inside `:app` under package `com.hmx.launcher.spike`** with a
single Backdrop dependency added only to `:app`, plus one manifest activity (label "HMX Spike").

Reasons: (1) the strongest Gate-A evidence is producing a build of the actual ARK application
together with Backdrop — a detached library module would prove less; (2) zero new Gradle/AGP
wiring to maintain, so no risk of mis-wiring a new module without local builds; (3) the isolation
requirement is about the *production workspace*, which remains untouched; the whole spike tree is
already isolated. Module extraction is Phase-2 work.

Spike additions summary:

| File | Content |
| --- | --- |
| `app/src/main/kotlin/com/hmx/launcher/spike/SpikeGlassProfile.kt` | `SdkCapability` + `SpikeGlassProfile` + resolver (pure JVM) |
| `.../SpikeBackdrop.kt` | one-window backdrop host + glass surface + dock + search bar + sheet |
| `.../SpikeWallpaper.kt` | wallpaper state, owned wallpaper decode (inSampleSize), tinted fallback |
| `.../SpikeActivity.kt` | demo screen (photo-picker import, sheet show/hide, profile banner) |
| `app/src/main/res/drawable/spike_ic_search.xml` | Apache-2.0 Material "search" glyph (no new lib) |
| `app/src/main/AndroidManifest.xml` | one added spike activity entry (isolated copy only) |
| `app/build.gradle.kts` | one added line: `implementation("io.github.kyant0:backdrop:2.0.1")` |
| `app/src/test/kotlin/com/hmx/launcher/spike/*` | 12 spike unit tests (profile 6, wallpaper-state 4, manifest 2) |

No other upstream file was modified. Production workspace untouched (hash snapshot in
`artifacts/pristine/`, compared at the end of the phase).

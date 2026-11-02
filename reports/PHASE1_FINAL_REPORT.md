# PHASE 1 — FINAL REPORT (isolated compatibility spike)

**Phase-1 owner decision referenced throughout:** ARK foundation, Kotlin+Compose, Hilt, Room,
DataStore, minSdk 30, compile/target baseline 37 (upstream convention already exactly matches),
Backdrop 2.0.1 glass, HMX-owned wallpaper scene, Apache-2.0.

## Executive summary

Phase 1 implemented a complete, verifyable rendering-to-wallpaper spike inside an **isolated**
copy of ARK at the pinned commit and wired the only feasible execution channel (GitHub-hosted CI)
to produce build/test/lint evidence. Runtime-dependent gates (visual rendering, OPPO A38
measurements, emulator-level API checks, live widget/RoleManager flows) are marked **NOT RUN**
everywhere they were not executed — nothing was invented.

## What was implemented

- Source isolation: full ARK @ `803b66d…` copy with no nested `.git`; production workspace
  untouched (pristine hash snapshot kept).
- Backdrop 2.0.1 wired into `:app` (single dependency line), API surface verified against the
  published sources jar before writing code (no guessed signatures).
- Three glass surfaces (dock / search bar / sheet) with the budget rules enforced by structure:
  one shared backdrop per window, ≤2 blur + ≤1 lens, no per-frame blur animation.
- HMX-owned wallpaper scene with Photo-Picker import → app-private copy → sampled decode →
  recorded backdrop provider, plus an intentional tinted fallback when no image is set — and zero
  system-wallpaper/screenshot/accessibility reads, zero new permissions.
- Explicit per-API profile policy (resolver + Capability model) not relying on library internals.
- 12 spike unit tests, all pure-JVM + reuse of upstream Robolectric for manifest checks.
- Ten evidence reports + CI workflow + snapshot/push scripts.

## Gate status (final, evidence from CI run 41485053843)

| Gate | Criteria | Status |
| --- | --- | --- |
| A Build | ARK+Backdrop resolves and builds | **PASS** — see `reports/PHASE1_BUILD_COMPATIBILITY.md` (both baseline & spike green, ~253s assemble) |
| B Rendering | three glass surfaces render | **PARTIAL** — code implements + compiles & lint-clean against Backdrop's verified API; pixel rendering NOT RUN (no runtime) |
| C Wallpaper | owned wallpaper + tinted fallback work | **PARTIAL** — implemented + state-tested; runtime NOT RUN |
| D API | 30/31/33 profiles behave | **PARTIAL** — policy unit-tested (12 tests green); pixels NOT RUN |
| E Device | OPPO A38 measurements | **BLOCKED / NOT RUN** (no device access) |
| F Platform | default-home + widget viability | **PARTIAL** — static manifest facts + CI JVM test (HOME filter resolves); runtime flows NOT RUN |
| G Stability | process recreation + wallpaper changes | **PARTIAL** — invalidation contract tested; runtime NOT RUN |
| H Licence | no blocking licence issue | **PASS** — static audit + resolved-graph verified on CI (Apache-family only; `artifacts/ci/*deps.txt`) |
| I Tests | tests + lint documented | **PASS** — 65/65 tests green on CI, 0 lint errors (details: `reports/PHASE1_TEST_REPORT.md`) |
| J Isolation | production repo untouched | **PASS** — byte-level `artifacts/pristine/` snapshot re-diffed at the end; zero diffs |

Overall Phase-1 verdict: **PARTIAL PASS (honest)** — Gates A, H, I, J pass with CI evidence;
C/D/F/G pass at the code+test level but their runtime halves stay open; B/E cannot be judged without
a device/emulator. This is exactly what an isolated spike without hardware can honestly prove.

## What worked (already evidenced)

- Upstream SDK policy matches the owner's decisions exactly (compileSdk/targetSdk 37, minSdk 30,
  Java 21) — the "API 37 subject to build compatibility" question is resolved by source inspection.
- Backdrop 2.0.1 is Apache-2.0 with an Apache transitive chain (kyant-shapes 1.2.1); no licence
  blocker.
- Upstream already has launcher essentials (HOME filter, widget pin entry, pending-widget storage,
  Glance, Hilt, activity-compose) — the spike adds only spike-scoped files.

## What failed / was blocked

- Local builds: prohibited by the owner; CI executes instead.
- Physical device and emulator: unavailable in Phase 1 — gates B/E/D-runtime/F-runtime/G-runtime
  cannot pass on evidence this round; recorded NOT RUN/BLOCKED, not assumed.

## Known limitations

- Spike stays inside `:app` (documented decision); module extraction deferred to Phase 2.
- No bundled test wallpaper asset (deliberate: gradient fallback + photo picker input is licence-
  clean; screenshots will carry the runtime imagery later).
- DataStore/Hilt wiring unused by the demo beyond what upstream needs (spike is UI-behaviour only).

## Required fixes / follow-ups (not blockers for CI)

1. Device pass for gates B/E/D-runtime/F-runtime/G-runtime (Perfetto, FrameMetrics, meminfo).
2. Robolectric-free "app-compatible Jackson decode" check on real device for picked wallpaper.
3. Phase-2: extract `:feature:spike`, HMX rebrand + NOTICE file, DataStore-backed wallpaper state.

## Recommendation

**PROCEED WITH CONDITIONS** — Gates A (build), H (licence), I (tests+lint), J (isolation) PASS on
CI evidence. Conditions before the product decision is made final: (1) one device pass (OPPO A38 or
an emulator) to cover gates B/E and the runtime halves of C/D/F/G; (2) keep the spike code isolated
as it is now (it is; the module extraction is Phase-2 hygiene); (3) re-run the resolved-licence
audit when new dependencies land. Everything Phase 1 had to prove without hardware is proven.

## APK build status (Phase-1 delivery run 41488191035, commit b225f1c1)

- **Build: PASS on GitHub-hosted runner** (same validated spike configuration, no upgrades).
  `assembleDebug` re-ran from a fresh checkout and tests stayed green in the same run:
  `:app:assembleDebug` PASS 253 s, `:app:testDebugUnitTest` PASS 58 s (65 tests, 0 failures),
  `:app:lintDebug` PASS 158 s (0 errors).
- Existence verified from the runner filesystem, not the task status:
  `app/build/outputs/apk/debug/app-debug.apk` (runner list + `output-metadata.json` in
  `artifacts/ci/spike-apk-dir.txt`), file size **40,704,990 bytes**, valid zip (248 entries,
  21 dex files, manifest present). SHA-256 (recomputed after download from GitHub):
  `4862f700a807b8de4d39d38bd6403dea721ffa86d345b3199dce952015d524ad`.
- `aapt2 dump badging` (committed as `artifacts/ci/spike-apk-badging.txt`):

| Field | Value |
| --- | --- |
| Package | `org.arkikeskus.launcher.debug` (upstream applicationId + upstream `.debug` suffix — the ARK spike build, installed beside any release launcher) |
| Version | versionCode 65 / versionName 0.9.8 |
| Min SDK | 30 (satisfies HMX's API-30 floor) |
| Target/Compile SDK | 37 / 37 |
| Label | "ARK-launcher debug" (upstream debug label; HMX rebrand is Phase 2) |
| ABIs (native-code) | arm64-v8a, armeabi-v7a, x86, x86_64 — universal APK; OPPO A38 (Helio G85) uses arm64-v8a ✓ |

## APK artifact upload status: DELIVERED ✅

- Workflow: `.github/workflows/phase1.yml` gained a dedicated upload step (`if-no-files-found:
  error` — the workflow FAILS if no APK is emitted, so a delivery cannot be claimed silently).
- Artifact name: **`HMX-Launcher-Phase1-Debug-APK`** (retention 30 days).
- Run URL: https://github.com/bharti1233/Hmx-launcher/actions/runs/41488191035 (both baseline and
  spike jobs green).
- Download location: run page → Artifacts → `HMX-Launcher-Phase1-Debug-APK`; direct API path
  `https://api.github.com/repos/bharti1233/Hmx-launcher/actions/runs/41488191035/artifacts`
  (artifact id 13162928075). Agent-side check: artifact ZIP downloaded and unzipped successfully —
  contents = `app-debug.apk` (40,704,990 bytes, matches the runner-side size exactly).
- Build logs + test results retained in the `phase1-spike-ci` / `phase1-baseline-ci` artifacts of
  the same run (7-day retention, as before).
- The APK is intentionally NOT committed to the repository — delivery is via the Actions artifact
  (keeps the repo clean and the artefacts traceable to one verified run).

## Installation instructions for OPPO A38 (Android 15 / ColorOS 15)

1. Download `HMX-Launcher-Phase1-Debug-APK` from the run URL above → unzip → `app-debug.apk`.
2. Transfer the APK to the phone (USB / cloud / `adb push app-debug.apk /sdcard/`).
3. ColorOS: open the file (Files app) → allow **"Install apps from external sources"** for the
   installer when prompted (ColorOS 15 path: Settings → Apps → App management → … → Special
   access → Install unknown apps, or via the installer prompt directly).
4. This build is a **debug build of the upstream ARK launcher** (`org.arkikeskus.launcher.debug`):
   it installs alongside existing launchers and keeps its own home screen. You get two icons in
   the app list: the launcher itself ("ARK-launcher debug" — the real home with widgets etc.),
   and **"HMX Spike"** — the Phase-1 glass demo (dock / search bar / sheet + wallpaper picker).
   The glass demo is the first thing to open for the device pass.
5. Default-home switch for the device pass: Settings → Apps → Default apps → Home app →
   "ARK-launcher debug" — or RoleManager's prompt when first pressing Home, as long as it was set.
6. Optional ADB route (if the Files app install is refused): `adb install -r app-debug.apk`
   with USB debugging enabled (ColorOS: Settings → About device → Version → tap Build number →
   Developer options → USB debugging).
7. Uninstall afterwards: Settings remove, or `adb uninstall org.arkikeskus.launcher.debug`.

## Remaining device compatibility tests (after installing this APK)

1. Three glass surfaces render visually (banner shows the resolved profile — expected
   `Refractive` on Android 15 / API 35).
2. Fallback check: first launch (no HMX wallpaper set) must show the tinted gradient + glass over
   it; then Photo-Picker import must swap the backdrop.
3. Frame timing on G85: swipe/sheet open/close via `dumpsys gfxinfo` / Perfetto; measured refresh
   mode (90 vs 60 Hz) first.
4. Memory: `dumpsys meminfo org.arkikeskus.launcher.debug` idle vs after wallpaper swaps (PSS/gfx
   budgets in PHASE1_PERFORMANCE_REPORT.md).
5. Cold/warm start (`am start -W`), process recreation, thermal soak.
6. Default-home RoleManager flow + widget pinning flows (CONFIRM_PIN_*) + notification-dot binding.
7. ColorOS-specific behaviours (background kill aggressiveness, gesture-home animations, widget
    pinning dialogs).

## Evidence index

| Run | Commit | Baseline | Spike | Notes |
| --- | --- | --- | --- | --- |
| 41480614719 | 92deb7f5 | PASS | FAIL | run-#1 spike compile errors (spike-only; fixed) |
| 41482779958 | 40f4bc89 | PASS | PASS | fix validated |
| 41485053843 | f6e624f5 | PASS | PASS | evidence run — test XMLs + lint reports exported (`artifacts/ci/`) |
| 41488191035 | b225f1c1 | PASS | PASS | **APK delivery run** — `HMX-Launcher-Phase1-Debug-APK` artifact |

CI logs/artifacts: https://github.com/bharti1233/Hmx-launcher/actions (runs above; artifact names
`phase1-baseline-ci` / `phase1-spike-ci` / `HMX-Launcher-Phase1-Debug-APK`).

*(This file is now final: every table above is populated from the actual CI runs.)*

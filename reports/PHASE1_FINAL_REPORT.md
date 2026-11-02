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

## Gate status (as of this revision — will be re-verified against CI)

| Gate | Criteria | Status |
| --- | --- | --- |
| A Build | ARK+Backdrop resolves and builds | **NOT RUN** → CI pending |
| B Rendering | three glass surfaces render | **NOT RUN** (compile-verified only; pixel proof needs runtime) |
| C Wallpaper | owned wallpaper + tinted fallback work | **PARTIAL** (implemented + state-tested; runtime NOT RUN) |
| D API | 30/31/33 profiles behave | **PARTIAL** (policy unit-tested; pixels NOT RUN) |
| E Device | OPPO A38 measurements | **BLOCKED / NOT RUN** (no device access) |
| F Platform | default-home + widget viability | **PARTIAL** (static manifest facts verified + CI JVM test; runtime NOT RUN) |
| G Stability | process recreation + wallpaper changes | **PARTIAL** (invalidation contract tested; runtime NOT RUN) |
| H Licence | no blocking licence issue | **PARTIAL** (static audit PASS; resolved-graph cross-check pending CI) |
| I Tests | tests + lint documented | **NOT RUN** → CI pending |
| J Isolation | production repo untouched | **PASS** (hash snapshot `artifacts/pristine/`, re-verified at end) |

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

**PROCEED WITH CONDITIONS** — conditional on the two CI-required gates (A, I) returning PASS from
the runner, and on the owner understanding that B/E and all runtime gates stay OPEN until hardware
is available. Phase 1 does not complete on code alone.

*(This file is finalized — with every table above populated from the actual CI run — immediately
after the workflow completes; the run artifacts are copied into `artifacts/` as the evidence pack.)*

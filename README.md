# HMX Launcher — Phase 1 isolated compatibility spike

**Status: spike only. Not the product. Do not build a release from this.**

The production research workspace lives elsewhere and was never modified. Everything here is a
disposable Phase-1 experiment that answers one question: *can ARK-launcher + Backdrop 2.0.1
(Kyant0 Liquid Glass) serve as the technical foundation of the HMX Launcher?*

## Layout

- `source/ark-launcher/` — ARK upstream at pinned commit `803b66d…` (no `.git`), plus the
  Phase-1 spike additions in `app/src/main/kotlin/com/hmx/launcher/spike/`.
- `source/liquid-glass-reference/` — placeholder; the AndroidLiquidGlass audit clone remains
  read-only in the production workspace (`third_party/AndroidLiquidGlass`, commit `65ab177…`).
- `spike/` — scripts: `ci_build.sh` (hosted-runner build harness), `push.sh` (owner-run push),
  `snapshot_prod.sh` (byte-level proof that the production workspace is untouched).
- `reports/` — the ten Phase-1 evidence reports (`PHASE1_*.md`).
- `artifacts/` — small evidence files copied from CI runs (logs stay in CI artifacts).

## CI

`.github/workflows/phase1.yml` runs on GitHub-hosted runners (no local builds, no emulator):

1. `baseline-ark` — builds the **unmodified** ARK upstream at the pinned commit
   (`:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug`) on JDK 21.
2. `spike-ark-backdrop` — builds this tree, i.e. ARK + `io.github.kyant0:backdrop:2.0.1`,
   runs the upstream unit tests + the 12 spike unit tests, and lints.

Both jobs export durations, per-task pass/fail, and the resolved `:app` dependency graph.

## Spike scope

Three glass surfaces (dock, search bar, sheet) over an HMX-owned wallpaper scene, with a
documented tinted fallback and explicit per-API profiles (30 tinted / 31 blurred / 33+
refractive). No device/emulator was available during Phase 1; runtime measurements and
screenshots are marked `NOT RUN` in the reports rather than invented.

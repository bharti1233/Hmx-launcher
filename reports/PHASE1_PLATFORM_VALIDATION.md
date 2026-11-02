# PHASE 1 — PLATFORM VALIDATION (launcher integration viability)

**Status: static evidence VERIFIED from the manifest/source; all runtime behaviour NOT RUN (no
device/emulator).** The spike inherits ARK's launcher integration unchanged — the isolated copy of
the manifest is byte-identical upstream plus one demo activity.

## Default home

Verified statically (and re-verified on CI in JVM tests):

- `LauncherActivity` declares `MAIN + HOME + DEFAULT + LAUNCHER` (intent-filter),
  `singleTask`, `stateNotNeeded=true`, `excludeFromRecents`, task-affinity empty — textbook
  launcher configuration. So a spike build releases a *default-home candidate* out of the box.
- CI test `SpikeManifestTest.appRegistersAsHomeCandidate` (green on run 41485053843) resolves the
  HOME intent-filter through Robolectric's merged manifest.
- CI test `SpikeManifestTest.spikeActivity_isLaunchable` (green) confirms the spike demo keeps a
  launchable entry after the manifest addition.
- RoleManager home-role availability, user-mediated selection flow, denied/cancelled handling:
  **NOT RUN** (requires UI device). Note: safe-by-design — `RoleManager` prompts are the upstream
  launcher's standard flow and the spike adds no custom code here.

## Widgets

Verified upstream facts (code inspection):

- `PendingWidgetIds` (SharedPreferences-backed) is wired in `LauncherApplication` — the app already
  hosts widgets and knows how to track pending widget allocation.
- `PinRequestActivity` handles `CONFIRM_PIN_SHORTCUT` / `CONFIRM_PIN_APPWIDGET` — the app already
  participates in user-mediated "Add widget" flows.
- Glance 1.2.0 dependencies exist (smartspace widget support upstream).
- CI test asserts the spike itself keeps launchability intact.

Runtime "discover provider → allocate id → bind → display one real widget → cleanup on cancel":
**NOT RUN** (needs a device with real providers). Phase-1 verdict: architecture plausible
  (upstream already allocates/drops pending widget ids and supports user-mediated pinning), runtime
  unproven.

## Process recreation and wallpaper-change handling

Implemented for the spike scope and test-relevant:

- Wallpaper state lives in `SpikeWallpaperState` (file path only) — on process recreation the path
  is recovered by re-reading the app-private file (no external state needed). Metadata-driven
  recovery beyond the file path is intentionally absent (spike).
- Change invalidation contract tested (`SpikeWallpaperStateTest`).
- Runtime kill/recreate + reopen on device: **NOT RUN**.
- No stale graphics layer: the single-layer structure (`layerBackdrop` on the wallpaper node only)
  means a wallpaper change re-records the same layer — no extra layer can survive (verified by
  design review; runtime visual check NOT RUN).

## Permissions posture

Spike adds **zero** permissions (manifest diff = 1 activity entry). Upstream launcher permissions
carry over unchanged in the copy (contacts/calendar/location are runtime-gated upstream rules we
do not exercise in the demo).

## ColorOS-specific behaviour

Unverified: ColorOS aggressive default-home locking/animations and ColorOS-specific widget pinning
behaviour. Per the task instructions these are recorded as **unverified**, to be probed on the A38.

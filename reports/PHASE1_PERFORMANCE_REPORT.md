# PHASE 1 — PERFORMANCE REPORT

**Status: NOT RUN — RESPECTFULLY BLOCKED (no physical device, no emulator in Phase 1; CI runners
cannot run Android UI).** No number in this report is a measurement; every target below stays an
unmeasured budget until the device pass. Honesty rule per the task: never invent numbers.

## Target device

OPPO A38 — Helio G85 (2×A75 + 6×A55), Mali-G52-class GPU, Android 15 / ColorOS 15, 720×1612,
90 Hz capable. **The actual refresh mode will be measured (`adb shell dumpsys display` /
`SurfaceFlinger` state) instead of assumed** — A38 ColorOS defaults to 90 Hz but may drop to 60 Hz
on low battery / power saver, so the frame budget active at test time is the measured one.

## Budgets to verify in the device pass

| Metric | Target | Status |
| --- | --- | --- |
| Cold start | ≤ 1.5 s P90 | NOT RUN |
| Warm return | ≤ 250 ms P90 | NOT RUN |
| Frame budget 90 Hz / 60 Hz | 11.1 ms / 16.6 ms | NOT RUN |
| Glass first draw | ≤ 16 ms | NOT RUN |
| PSS after 5 min idle | ≤ 180 MB | NOT RUN |
| Graphics memory | ≤ 64 MB | NOT RUN |

## Measurement plan (so the device pass is turnkey)

- Cold/warm start: Microbenchmark + manual `am start -W` cross-check.
- Frame pacing (swipe, sheet open/close, dock render, wallpaper swap): `FrameMetrics` listener +
  Perfetto traces; classify dropped frames against the measured refresh mode.
- Memory: `adb shell dumpsys meminfo <pkg>` snapshots at idle 0/5 min and after N wallpaper swaps.
- Thermal: battery-optimized OFF, 10-min repeated sheet/dock interaction trace; report throttling
  signals if any.
- Wallpaper bitmap: decode target 1080×2400 (spike constant, over-allocates by design for
  720-wide device) — flag for measurement, not silent assumption.

## Rendering budget (implemented this phase)

| Rule | Spike actual |
| --- | --- |
| One wallpaper backdrop per window | 1 (structural, see glass report) |
| Max 2 blurred surfaces | 2 (dock + search bar; sheet adds lens on top of its own blur) |
| Max 1 refractive surface | 1 (sheet) |
| Max 3 glass surfaces | 3 |
| No per-frame blur animation | honored (fade/slide toggling only) |
| No full-screen blur combined with large sheet | honored (sheet ≪ screen; no full-screen blur) |

These rules exist because the G85's GPU is the known bottleneck: three simultaneous sampled glass
layers over one backdrop is the intended *maximum* demo, not the production target.

## If performance is poor on the device, first levers (already enumerated)

Blur radius ↓; backdrop resolution (reduce decoded wallpaper size); fewer simultaneous glass
surfaces; avoid recomposition storms (banner/state hoisting already done); icon decoding already
inSampleSize'd. Text contrast is NEVER traded for benchmarks.

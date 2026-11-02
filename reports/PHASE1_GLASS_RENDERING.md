# PHASE 1 — GLASS RENDERING

**Status: IMPLEMENTED; COMPILE + LINT-VERIFIED VIA CI (runs 41482779958, 41485053843);
RUNTIME RENDERING NOT RUN (no emulator/device in this phase).** Screenshots of the three surfaces: NOT RUN (no runtime). What *is* verified:
the exact Backdrop 2.0.1 API surface (from the published sources jar) and the implementation's
conformance to it; successful compilation is confirmed by CI (see build report).

## Implementation (`source/ark-launcher/app/src/main/kotlin/com/hmx/launcher/spike/`)

All glass compositing is HMX-owned under `com.hmx.launcher.spike`; **no Backdrop import appears
outside this package.** Backdrop objects flow through one `LocalSpikeBackdrop` provider:
`SpikeBackdropHost` creates a single `rememberLayerBackdrop()` per window, the wallpaper layer
records into it via `Modifier.layerBackdrop(backdrop)`, and every glass surface samples that one
instance via `drawBackdrop(backdrop, shape, effects, highlight, shadow)`. There are no nested
`layerBackdrop` recorders and no second backdrop anywhere in the spike.

### C1 — Glass dock (`SpikeGlassDock`)
Rounded floating dock (`RoundedCornerShape(28.dp)`); translucent glass with `Highlight.Default`
rim highlight + `Shadow.Default` depth; four 48dp rounded icon blocks (placeholder icons, rendered
with clear colors — no image-dependent blending). Touch surface: container semantics only (nothing
to tap), which keeps the dock strictly a layout probe.

### C2 — Glass search bar (`SpikeGlassSearchBar`)
52dp rounded pill with its own softer blur (radius 20dp→px) below API-33 refraction budgets;
Apache-2.0 Material "search" glyph loaded from a bundled vector (`spike_ic_search.xml`, no
material-icons dependency added); readable `bodyLarge` placeholder; touch handling is a plain
`Modifier.clickable` on the full pill (opens/closes the sheet in the demo), satisfying "correct
touch handling" without extra machinery.

### C3 — Glass sheet (`SpikeGlassSheet`)
Floating panel: blur (18dp) **and** lens refraction (`refractionHeight=16dp→px`,
`refractionAmount=10dp→px`, `depthEffect=true`, `chromaticAberration=false` — optional channel not
used this phase). Refraction is requested only where the API allows it: `lens()` internally no-ops
below API 33 (verified in `effects/Lens.kt`: `if (!isRuntimeShaderSupported()) return`), and for
unsupported shapes it *throws* — the spike only ever pairs lens with `RoundedCornerShape`/family
`CornerBasedShape` inputs, so that path is unreachable. Show/hide animates with
`AnimatedVisibility` (fade + 1/4-slide), no per-frame blur animation.

## Rendering budgets honored

| Rule of Phase 1 | Actual |
| --- | --- |
| One wallpaper backdrop per window | 1 (`SpikeBackdropHost`), enforced by a single provider value |
| ≤2 blurred surfaces at once | 2 (search bar + dock share one screen with the sheet) |
| ≤1 refractive surface | 1 (sheet only) |
| ≤3 simultaneous glass surfaces | 3 (sheet, search bar, dock) |
| No per-frame blur animation | sheet animates fade/scale only; blur radius is constant |
| No full-screen blur + large sheet | sheet is not full-screen; no full-screen blur exists |
| Text/icons stay sharp | text/icons draw *on* the surface; the backdrop blur affects the background layer only |

## API-level behaviour contract

The library self-guards unsupported effects (`blur` returns early without RenderEffect support;
`lens` returns early without RuntimeShader support). This spike still isolates its **explicit**
profile decision in `SpikeGlassProfileResolver` (documented in the API-compatibility report), so
HMX never "assumes the library handles it". The on-screen banner prints the resolved profile +
API level for any future device evidence.

## What runtime verification would add (when hardware is available)

- Visual confirmation of blur/lens/refraction rendering vs the profile banner.
- Fallback check: removing the backdrop (backdropAvailable=false path) actually switches surfaces
  to the tinted material (unit tests cover the transition logic, not the pixels).
- Frame-timing evidence for the performance report (glass first draw ≤16ms etc.). All currently
  **NOT RUN**.

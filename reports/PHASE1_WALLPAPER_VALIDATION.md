# PHASE 1 — WALLPAPER VALIDATION

**Status: COMPILE + TEST-VERIFIED VIA CI (run 41485053843); RUNTIME WALLPAPER RENDERING NOT RUN
(no device).**

## Architecture

The HMX-owned wallpaper scene lives entirely inside the launcher window and is the **single
backdrop source** for the three glass surfaces:

```
SpikeBackdropHost                     (creates 1 rememberLayerBackdrop per window)
└── Box
    ├── SpikeWallpaperHost(...)  Modifier.layerBackdrop(backdrop)   ← recorded source
    │     ├── HMX-owned decoded image (photo picker → app-private storage → inSampleSize decode)
    │     └── OR intentional tinted gradient fallback when no HMX image exists
    └── demo content Column (glass sheet, search bar, dock sample the recorded layer)
```

Owner's app-owned-wallpaper decisions are honored exactly: isolating from the system wallpaper;
tinted/translucent fallback when the HMX wallpaper was never set; invalidation on wallpaper or
window-size change (the wallpaper `Image` re-lays out with `ContentScale.Crop` on window resize,
and the layer is re-recorded on redraw — structure guarantees no stale layer, and the state tests
cover the invalidation contract).

## Import path (implemented)

1. `ActivityResultContracts.PickVisualMedia` (Android **Photo Picker**) — zero storage permission.
2. Selected URI is copied into `filesDir/wallpapers/` (app-private, isolated).
3. Decode uses `inJustDecodeBounds` + power-of-two `inSampleSize` targeting 1080×2400 headroom
   (over the OPPO A38's 720×1612, documented in code as a Phase-1 proxy).
4. Decode runs on `Dispatchers.IO` (keeps the launcher window jank-free).
5. The recorded backdrop (and thus every glass surface) refreshes automatically when new state
   renders (produceState keys on the file), and `SpikeWallpaperState.hasBackdropChanged` defines
   the invalidation contract used by the tests.

## What is NEVER done (per owner rules)

- No `WallpaperManager.getDrawable()` / any system-wallpaper pixel read (would throw on Android 14+
  anyway).
- No MediaProjection, no screenshots, no Accessibility capture.
- No `MANAGE_EXTERNAL_STORAGE`, no `READ_WALLPAPER_INTERNAL`, no hidden APIs. Manifest unchanged
  in this respect (spike adds zero permissions — picker input yields a content URI grant only).

## Fallback rendering

When no HMX wallpaper exists the fallback is a **visually intentional** dark vertical gradient
(`#1B2430 → #51607A → #9AA7BD`) over the full window, and the glass surfaces still sample it
(the fallback still records into the shared layerBackdrop, so glass stays glass over the
fallback, not a floating white block). If the owned decode *fails* (corrupt file), the composable
also shows this fallback rather than crashing.

## True-glass mode vs fallback differences (design note)

| Aspect | Owned wallpaper | Tinted fallback |
| --- | --- | --- |
| Source | decoded HMX image | fixed gradient colors |
| Ink cost | bitmap memory (~1080×2400 ARGB ≈ 16.6 MB surface + decode) | none |
| Backdrop recorded | recorded from the image layer | recorded from the gradient layer |
| Readability risks | light-sky image can wash out glass highlights; fallback always contrast-safe | constant |
| API 30 behaviour | tint profile keeps blur off; image visible | tint profile; gradient visible |

## Test coverage

- `SpikeWallpaperStateTest` (4 tests, all green on CI): owned-vs-fallback classification, change
  detection (invalidation contract), no-op stability.
- Runtime items (actual displayed wallpaper, picker result flow, crop behaviour, wallpaper-change
  visual continuity, Glass first-draw latency): **NOT RUN** — need a device/emulator; they are the
  top backlog items of the follow-on device pass.
- CI cross-evidence: `SpikeManifestTest.spikeActivity_isLaunchable` green (demo reachable on a
  merged-manifest level) — see the platform report.

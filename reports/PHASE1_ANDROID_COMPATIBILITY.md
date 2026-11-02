# PHASE 1 — ANDROID API COMPATIBILITY

**Status: PROFILE POLICY IMPLEMENTED + UNIT-TESTED; RUNTIME BEHAVIOUR PER API LEVEL NOT RUN (no
device/emulator available in Phase 1).**

## Capability model (code: `com.hmx.launcher.spike.SpikeCapability`-in-`SpikeGlassProfile.kt`)

| Capability | Android | API | Used for |
| --- | --- | --- | --- |
| `isRenderEffectSupported(sdkInt)` | 12+ | ≥31 | backdrop blur chains |
| `isRuntimeShaderSupported(sdkInt)` | 14+ | ≥33 | AGSL lens/refraction shader |

Both are invoked against an **explicit** `sdkInt` so the policy is a plain-JVM unit-testable
decision (device-independent). The library additionally self-guards inside its own effect builders
(verified in the released sources), so unsupported requests degrade instead of crashing — but HMX
policy never *relies* on silently-degrading effects: the resolver is the contract.

## Profile policy (source of truth: `SpikeGlassProfileResolver`)

| sdkInt | backdrop available | Profile | Glass asks for |
| --- | --- | --- | --- |
| < 31 | no | Tinted | no effects — surfaces render as tinted material with highlight/shadow |
| 31–32 | yes | Blurred | `blur(24dp)` on surface, `blur(20dp)` on search bar; no lens |
| ≥ 33 | yes | Refractive | blur + `lens(...)` on the sheet, blur elsewhere |
| any with `backdropAvailable=false` | n/a | Tinted | full fallback material (no backdrop path at all) |

Expected API behaviour (from the owner's feasibility table):

- **API 30** — No RenderEffect; surfaces use gradient/tint material, rim highlights, shadows in
  Compose drawing (library guard returns early; the profile also *requests* nothing). High-contrast
  fallback = the intentional tinted wallpaper + tinted surfaces.
- **API 31–32** — blur where supported, no refraction; fallback path unchanged.
- **API 33+** — blur + lens/refraction on the sheet; chromatic aberration intentionally left OFF
  this phase (an optional extra); safe degradation remains in place if a particular OEM GPU
  mis-renders (perf pass will check).

## Unit evidence (CI-executed, run 41485053843): ALL GREEN

`SpikeGlassProfileTest` — 6 tests green: API 30→Tinted, 31→Blurred, 32→Blurred, 33→Refractive,
35→Refractive, missing-backdrop→Tinted (see `artifacts/ci/test-xml/`).

## Runtime verification (NOT RUN — blocked on hardware)

| Planned device check | Status |
| --- | --- |
| OPPO A38 (Android 15 / ColorOS / Helio G85) actual profile banner = Refractive | NOT RUN |
| API 30 emulator: surfaces render without any RenderEffect | NOT RUN |
| API 31 emulator: blur visible, no lens | NOT RUN |
| API 33+ emulator: lens visible on sheet | NOT RUN |
| Skill of fallback (backdropAvailable=false simulation) on device | NOT RUN |

The unit-verified policy plus the library-internal guards give high confidence here, but the
Phase-1 verdict for gate D stays honest: policy tested, pixels not.

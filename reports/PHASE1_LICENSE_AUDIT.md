# PHASE 1 — LICENCE AND OPEN-SOURCE COMPLIANCE

**Status: static licence SET VERIFIED (POM/license files read directly); resolved-graph evidence
pending CI; no blocking issue found in the audited set.**

## Direct dependencies sources of truth

| Component | Source | Licence | Copy |
| --- | --- | --- | --- |
| ARK-launcher upstream | LICENSE file in the pinned tree | Apache-2.0 | yes (header + full LICENSE present) |
| Backdrop 2.0.1 | Maven POM (io.github.kyant0:backdrop) | Apache-2.0 | Apache licence URL in POM |
| Backdrop's `kyant-shapes` transitive (io.github.kyant0:shapes:1.2.1) | Maven POM (io.github.kyant0:shapes) | Apache-2.0 | "iOS-like shapes for Compose Multiplatform" |
| Backdrop runtime deps: kotlin-stdlib 2.4.10, JB compose foundation/ui/ui-graphics 1.12.0, org.jetbrains:annotations 26.1.0 | POM | Apache-2.0 / ASL-2.0 family | — |
| Next layer: Kotlin (ASL-2.0 + LLVM exception noted in JetBrains licences), AndroidX (Apache-2.0), Hilt/Dagger (Apache-2.0), Room, DataStore, Coroutines, Coil (Apache-2.0), Glance (Apache-2.0), Navigation 3, androidx test | catalog + upstream | Apache/ASL family | — |
| Test-only: JUnit 4.13.2 (EPL-1.0), Robolectric 4.17 (MIT), Google Truth 1.4.5 (Apache) | catalog | EPL/MIT/Apache | test scope only, never ships in an APK |
| `org.json:json` (upstream dep for backup import) | catalog | JSON licence (public-domain style with the historical "no evil" clause) | upstream-shipped; non-copyleft, commonly treated as a permissive/public-domain grant; flagged for notice completeness |

Notes:
- Everything that ships in the HMX APK is Apache/ASL-family or public-domain-style. No copyleft
  (GPL/LGPL/AGPL) dependency exists in the catalog (the owner's known GPL-3.0 launcher projects
  were never sourced into this spike; nothing was copied from Lawnchair).
- JUnit is build/test-time only; its EPL licence does not constrain the APK binary at all.

## Evidence pending CI

The `.module`-resolved *runtime classpath* can only claim the POM-declared set (above). The actual
resolved graph is exported by CI (`spike-deps.txt`, configuration `debugRuntimeClasspath`), and
this report will be annotated with the encountered `kyant:*`, JB-compose→androidx mapping, and any
additional group-distribution surprises. Until then: "resolved-graph verification: pending CI run."

## Distribution hygiene (already satisfied this phase)

- Upstream copyright headers and LICENSE preserved verbatim in `source/ark-launcher/` (no rebrand
  — the spike keeps upstream strings; the future HMX rebrand is Phase-2 work with its own notice
  file).
- No ARK trademark/branding reuse as HMX: the pushed repo is `Hmx-launcher` and the spike app keeps
  upstream labels; the spike demo labels itself "HMX Spike" only.
- Test wallpaper: **no bundled bitmap asset at all** in Phase 1 — the wallpaper default is the
  original generated gradient, and images enter via the Photo Picker at runtime. Nothing built
  into the repo has licence questions.
- The bundled `spike_ic_search.xml` glyph is a Google Material Icons path (Apache-2.0), attributed
  in-file.

## Upstream LGPL closure for reference

The AndroidLiquidGlass audit clone (`65ab177…`) shares its upstream with Backdrop (both Kyant0 /
AndroidLiquidGlass) and is Apache-2.0 as well; the production copy remains untouched (hash-checked
at the end of the phase).

## Verdict

No unresolved blocking licence issue in everything reachable from the spike as coded. The only
items to finish during the CI run: resolved-graph cross-check and a `NOTICE`-file plan at
production (Phase 2). Open-source release remains feasible on Apache-2.0.

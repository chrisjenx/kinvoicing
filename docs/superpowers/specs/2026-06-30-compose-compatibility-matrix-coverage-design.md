# Compose Compatibility Matrix — Coverage + Multi-CMP Support (Reflective Driver)

**Date:** 2026-06-30 (revised 2026-07-01 after evaluating compose2pdf 1.2.0)
**Status:** Approved direction; implementation sequenced below
**Author:** Chris Jenkins (with Claude)

> **Supersedes the original recompile-per-cell design in this file.** The first draft proposed
> broadening the matrix to recompile every module per CMP cell and bumping compose2pdf to 1.1.3.
> Investigation proved that wrong: (1) compose2pdf 1.1.3's *published* artifact was compiled against
> CMP 1.10.3 and `NoSuchMethodError`s on 1.12 at consumer runtime; (2) render-html's own
> `ComposeToSvg.kt` calls the `@InternalComposeUiApi` `CanvasLayersComposeScene` directly and won't
> even compile against 1.12. The real fix mirrors what compose2pdf **1.2.0** did: a single-binary
> **reflective scene driver** plus publish-then-consume compat testing and generated version-support docs.

## Problem

The `Compose Compatibility` workflow only runs `:render-compose:*` tasks. `render-compose` doesn't use
compose2pdf and recompiles cleanly against any CMP, so the matrix passed green even though the two
compose2pdf-consuming modules break on CMP 1.12:

- **`render-pdf`** — drives compose2pdf's `renderToPdf`; the pre-1.2.0 compose2pdf binary
  `NoSuchMethodError`s on CMP 1.12 (its bytecode calls the reshaped `CanvasLayersComposeScene` API).
- **`render-html`** — its `render-html/.../internal/ComposeToSvg.kt` calls `CanvasLayersComposeScene`
  **directly** (pre-1.12 4-arg overload), so it fails to *compile* against CMP 1.12.

CMP 1.12 reshaped the `@InternalComposeUiApi androidx.compose.ui.scene` API: pre-1.12 used
`coroutineContext`/`invalidate` factory params + `render(canvas, nanoTime)`; 1.12 uses a host-owned
`FrameRecomposer` + a 7-arg factory + `performFrame`→`measureAndLayout`→`draw(canvas)`.

## What compose2pdf 1.2.0 did (the model to mirror) — verified

- **Single published JVM jar + runtime reflection.** Deleted the `cmpLegacy`/`cmpNext` build-time
  source sets. `internal object ComposeSceneRenderer` resolves the scene API by reflection, detecting
  the shape *structurally* — `if (Class.forName("androidx.compose.ui.platform.FrameRecomposer") != null) NextDriver else LegacyDriver`,
  cached in `by lazy`. Factory matched by static + name-prefix `CanvasLayersComposeScene` + returnType
  `ComposeScene` + arity (excluding `$default`); other methods by name + param-type list with `"*"`
  wildcards; `InvocationTargetException` unwrapped to preserve real exception types; every miss →
  fail-loud `Compose2PdfException("…file an issue including your CMP version")`. Verified facts:
  the factory takes a **boxed `IntSize`**; `PlatformContext.Empty` is a plain **no-arg-ctor class** on
  both 1.11 and 1.12.
- **"Publish once, consume many" testing.** CI publishes the one binary to mavenLocal, then a
  standalone `compat-consumer` Gradle build (not in root `settings.gradle.kts`) forces
  `org.jetbrains.compose*` to each matrix cell's version via
  `resolutionStrategy.eachDependency` and runs a real `renderToPdf` smoke test — so it tests the
  **shipped bytecode**, not a per-cell recompile.
- **Generated version-support docs.** `.github/compose-versions.json` is the single source of truth;
  `.github/scripts/render-compat-tables.py` injects a `| CMP | Kotlin | Status |` table between
  `<!-- BEGIN cmp-matrix -->`/`<!-- END cmp-matrix -->` markers in `docs/compatibility.md` + `README.md`,
  with a `--check` docs-sync CI gate; the weekly updater auto-bumps the build base to latest stable.
- **One verified caveat:** the newest **prerelease** cell (`1.12.0-beta01`) is exercised but marked
  `continue-on-error: contains(version, '-')`, so it is *not enforced green* — only the two stable
  cells are blocking.

## Design for kinvoicing

**1. `render-html` reflective scene driver (load-bearing).**
New `render-html/.../internal/ComposeSceneRenderer.kt`, a near-verbatim port of compose2pdf 1.2.0's,
with: package `com.chrisjenx.kinvoicing.html.internal`; a render-html-local `ComposeSceneException`
(render-html doesn't depend on core, so it can't use core/compose2pdf exceptions); and a small
hardening so the `PlatformContext.Empty` no-arg-ctor miss routes through the fail-loud path (compose2pdf
lets a `NoSuchMethodException` propagate raw there). `ComposeToSvg.kt` replaces its direct
`CanvasLayersComposeScene` block with `ComposeSceneRenderer.drawContent(recordCanvas, w, h, density, content)`
(`recordCanvas` is the Skia `Canvas` from `PictureRecorder.beginRecording`), keeping the
`PictureRecorder`→`SVGCanvas` flow. Porting the full-arity factory call fixes the critique's point that
render-html currently relies on default factory args (`layoutDirection`, `platformContext`) it never
constructs.

**2. `render-pdf` inherits multi-CMP for free.** It goes through compose2pdf's `renderToPdf`; once
`compose2pdf` is bumped to 1.2.0, its reflective driver handles the 1.12 break — zero render-pdf source
change.

**3. Version alignment.** `gradle/libs.versions.toml`: `compose2pdf 1.1.2 → 1.2.0`; align the build base
`compose-multiplatform 1.10.3 → 1.11.1` and `kotlin 2.3.20 → 2.4.0` (compose2pdf 1.2.0 is built against
1.11.1/2.4.0; matches the matrix's Kotlin). **Risk gate:** the `build-logic/wasmjs-node-compose` plugin
reaches `KotlinJsTest.nodeJsArgs` reflectively against a specific KGP version — re-verify
`:build-logic:wasmjs-node-compose:test` and `:render-compose:wasmJsNodeTest` on Kotlin 2.4.0 before the
bump lands. This is an M-effort task, not a free S bump.

**4. `compat-consumer` harness (publish-then-consume).** New standalone Gradle build mirroring
compose2pdf's: own `settings.gradle.kts` (not in root includes), reads `-PcomposeVersion`/`-PkotlinVersion`,
`mavenLocal()` first, requires `-PrenderHtmlVersion`/`-PrenderPdfVersion` (a fixed publish version string,
e.g. `-Pversion=0.0.0-compat`, decided up front — kinvoicing has no `gradle.properties version=` SSOT),
forces `org.jetbrains.compose*` to the cell version via `resolutionStrategy.eachDependency`, and a
`Smoke.kt` that renders a PDF (asserts `%PDF-` + size) **and** the render-html SVG path (asserts `<svg`) —
driving both the c2p-inherited and kinvoicing-owned reflective paths so a wrong dispatch fails at runtime.

**5. Matrix CI rewrite.** `.github/workflows/compatibility.yml`: publish render-html/render-pdf to
mavenLocal (at the pinned base), then run `compat-consumer` per cell (xvfb on Linux). **Keep** the
existing `perl`-override recompile legs for `render-compose` (`:render-compose:jvmTest`/`wasmJsNodeTest`/
`iosSimulatorArm64Test`) — a multi-target KMP klib can't be reflectively bridged and its per-cell
recompile is the normal KMP consumer contract. Extend the `paths:` trigger to `render-html/**`,
`render-pdf/**`, `compat-consumer/**`. Keep `compat-gate`.

**6. Version-support docs.** Port `.github/scripts/render-compat-tables.py`; add a `docs/compatibility.md`
Jekyll page + `README.md` `## Compatibility` marker block + a `docs-sync --check` gate + extend
`update-compose-versions.yml` to auto-bump the base and regenerate. A **renderer-scope note** must state
what the matrix actually reaches: `render-html` (own reflective driver, directly tested), `render-pdf`
(via compose2pdf, directly tested), `render-compose` (source-recompiled per cell — normal KMP contract),
`core`/`render-html-email` (Compose-independent — **verify by grep** before asserting this). Put every
version number inside the generated block (avoid compose2pdf's stale hand-written deps-table drift).

## Prerelease enforcement decision

Match compose2pdf: the two stable cells are blocking; the newest prerelease cell (kinvoicing's JSON has
`1.12.0-alpha01`) is `continue-on-error`. Once render-html's driver + compose2pdf 1.2.0 make 1.12 pass,
we may flip it to enforced — but the docs must not claim "all green" while `continue-on-error` remains.
Pick one: enforce the prerelease cell, or document it as best-effort. (Also resync kinvoicing's JSON
prerelease id with compose2pdf's, or confirm the drift is intended.)

## Verification

- **render-html driver:** RED (current code fails to compile on CMP 1.12) → GREEN (`:render-html:test`
  passes at base 1.10.3 AND under a 1.12.0-alpha01/2.4.0 + compose2pdf 1.2.0 override). `:render-pdf:jvmTest`
  green under the same 1.12 override (proves the compose2pdf 1.2.0 inheritance).
- **base bump:** `:build-logic:wasmjs-node-compose:test` + `:render-compose:wasmJsNodeTest` green on Kotlin 2.4.0.
- **compat harness/CI:** each cell's `compat-consumer` smoke green (prerelease per the decision above).
- **docs:** `render-compat-tables.py --check` passes; grep confirms core/render-html-email Compose-free.

## Out of scope

- Golden-image fidelity tests stay in `build.yml` at the base CMP (pixel diffs false-positive across versions).
- `render-compose` stays source-recompiled per cell (KMP klib; not reflectively bridgeable) — documented as such.
- Native/Android legs beyond what already runs; no change to release/snapshot workflows.

## Blockers / risks (verified)

1. ~~compose2pdf 1.2.0 not published~~ — **resolved: 1.2.0 is live on Maven Central** (`<release>1.2.0`).
2. Kotlin 2.4.0 base bump may break the reflective `wasmjs-node-compose` plugin — gated re-verify required.
3. The `-Pversion`/publish-read-back mechanism for `compat-consumer` must be decided before the harness (fixed string).
4. `PlatformContext.Empty`/`close()`/invoke fail paths: port must route them through the actionable fail-loud message, not copy compose2pdf's raw-exception paths verbatim.
5. 1.10.3's factory shape is only smoke-verified (never javap-verified); keep the 1.10.3 compat cell blocking as the proof.

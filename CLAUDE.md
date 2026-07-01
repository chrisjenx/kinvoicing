# Kinvoicing

Kotlin Multiplatform invoicing library with sealed IR, DSL builder, and four renderers (Compose, PDF, HTML, HTML-Email).

## Commands

```bash
./gradlew build                              # Build all modules, all targets
./gradlew assemble                           # Compile all targets without tests
./gradlew :core:jvmTest                      # Core IR + DSL tests on JVM
./gradlew :core:allTests                     # Core tests on all platforms (JVM, iOS sim, wasmJs, macOS, etc.)
./gradlew :render-compose:jvmTest            # Compose renderer tests on JVM
./gradlew :render-compose:iosSimulatorArm64Test  # Compose renderer tests on iOS
./gradlew :render-compose:wasmJsTest         # Compose renderer tests on wasmJs
./gradlew :render-html-email:allTests        # HTML email renderer tests on all platforms
./gradlew :fidelity-test:jvmTest             # Invoice renderer visual regression tests (JVM only)
./gradlew :kinvoicing-fidelity-test:test     # compose2pdf fidelity tests (JVM only)
./gradlew :kinvoicing-docs:run               # Generate docs site assets (section preview HTML)
cd docs && bundle exec jekyll serve           # Run docs site locally
gh workflow run release.yml -f version=X.Y.Z  # Trigger Maven Central release + GitHub Release + version bump
./gradlew :build-logic:wasmjs-node-compose:test  # Smoke-test the wasmJs Node test-runner plugin
gh workflow run "Compose Compatibility"       # Run the CMP × OS compatibility matrix on demand
```

Fidelity report after tests: `kinvoicing-fidelity-test/build/reports/fidelity/index.html`

HTML fidelity tests require Playwright: `npx playwright install chromium` (skips gracefully if missing).

## Architecture

```
:core                    com.chrisjenx.kinvoicing            Invoice IR (sealed classes) + DSL
:render-compose          com.chrisjenx.kinvoicing.compose    InvoiceDocument → Compose UI
:render-html-email       com.chrisjenx.kinvoicing.html.email InvoiceDocument → email-safe HTML (kotlinx.html)
:render-pdf              com.chrisjenx.kinvoicing.pdf        InvoiceDocument → PDF (via compose2pdf)
:render-html             com.chrisjenx.kinvoicing.html       Compose → HTML rendering (via compose2pdf)
:kinvoicing-examples     com.chrisjenx.kinvoicing.examples   Invoice example fixtures for docs/tests
:kinvoicing-fidelity-test com.chrisjenx.kinvoicing.fidelity.compose  Visual fidelity tests
:kinvoicing-docs         com.chrisjenx.kinvoicing.docs       Docs site generator (section previews via render-html-email)
:fidelity-test           com.chrisjenx.kinvoicing.fidelity   Invoice renderer visual regression
```

Composite included build (not a library module):
```
build-logic/wasmjs-node-compose   com.chrisjenx.wasmjsnode   Gradle plugin id "com.chrisjenx.wasmjs-node-compose"
```
Applied by `render-compose`; patches Skiko's Emscripten loader + injects a DOM/canvas shim so `wasmJsNodeTest` runs on Node.js instead of Karma+Chrome. Wired via `pluginManagement { includeBuild("build-logic") }` in root `settings.gradle.kts`.

**Dependency flow:** core → render-compose → render-pdf / render-html (both via compose2pdf) ; core → render-html-email (standalone)

**⚠ render-html does NOT depend on core.** Shared utilities from core must be duplicated in render-html or made `public`. render-html-email DOES depend on core.

### Platform Targets

| Module | Targets |
|--------|---------|
| core | jvm, android, iosArm64, iosSimulatorArm64, wasmJs, macosArm64, macosX64, linuxX64, linuxArm64, mingwX64 |
| render-compose | jvm, android, iosArm64, iosSimulatorArm64, wasmJs |
| render-html-email | jvm, android, iosArm64, iosSimulatorArm64, wasmJs, macosArm64, macosX64, linuxX64, linuxArm64, mingwX64 |
| render-pdf | jvm only (compose2pdf) |
| render-html | jvm only (compose2pdf) |
| kinvoicing-examples | jvm only |

## Key Patterns

- **Sealed IR:** `InvoiceDocument` contains `List<InvoiceSection>` where `InvoiceSection` is sealed with 9 variants (Header, BillFrom, BillTo, LineItems, Summary, PaymentInfo, Footer, Custom, MetaBlock). Renderers use exhaustive `when` blocks.
- **DSL entry point:** `invoice { header { ... } lineItems { ... } summary { ... } }`
- **Shared composable:** `InvoiceContent(document)` is used by both Compose preview and PDF renderer — PDF matches Compose by construction.
- **compose2pdf** (`com.chrisjenx:compose2pdf:1.1.2`) is an external dependency providing `renderToPdf`, `PdfPageConfig`, `RenderMode`, `PdfLink`. Its `PdfLinkAnnotation` is `internal` — render-html defines its own link types for the HTML pipeline.
- **Image decoding expect/actual:** `decodeImageBytes()` in render-compose uses Skia on JVM/iOS/wasmJs and `BitmapFactory` on Android. Actuals live in `jvmMain`, `nativeMain`, `wasmJsMain`, `androidMain`.
- **runBlockingCompat expect/actual:** Wraps `runBlocking` for JVM/native/Android; throws on wasmJs (wasmJs consumers use Compose renderer path via `painterResource`, not `bytes`).
- **URL/CSS sanitization:** `core/.../util/Sanitize.kt` provides `requireSafeUrl` (public), `sanitizeFontFamily` (internal), `requireFinite` (internal). render-html has its own `sanitizeUrl` in `SvgToSemanticHtmlConverter.kt` since it can't import from core.
- **Headless Compose rasterization (test-only):** When converting an `ImageComposeScene.render()` result to a `BufferedImage`, **encode the image while the scene is alive, then close** — `image.encodeToData()` returns owned bytes; `image.peekPixels()` returns a `Pixmap` that aliases the surface's native memory and goes dangling once `scene.close()` runs. macOS hides the bug (freed bytes stay readable briefly); Linux glibc SIGSEGVs in `SkPixmap::getColor`. Reference impl: `kinvoicing-fidelity-test/.../TestHelpers.kt` and `fidelity-test/.../RasterizeCompose.kt`. Background: issue #6.
- **wasmJs tests on Node:** all three KMP modules (`core`, `render-compose`, `render-html-email`) declare `wasmJs { nodejs() }`. The `com.chrisjenx.wasmjs-node-compose` plugin (`build-logic/`) is applied by `render-compose` to make Compose/Skiko bootable under Node: it flips Skiko's Emscripten DCE gate (`if (false) {` → `if (ENVIRONMENT_IS_NODE) {`) and injects a `node --import` shim. The plugin reaches `KotlinJsTest.nodeJsArgs` reflectively to avoid coupling the build-logic classpath to a specific KGP version. `PixelChecksumTest` in render-compose validates the decoded-bitmap byte-layout matches across JVM/native/wasmJs (baselines in per-target `PixelChecksumExpected.<target>.kt`).
- **Compose compatibility matrix:** `.github/compose-versions.json` declares `(compose-version, kotlin-version)` pairs (currently 3 entries: two stable minors + the newest pre-release). The internal `@InternalComposeUiApi CanvasLayersComposeScene` reshaped in CMP 1.12 (`1.12.0-alpha01` is still the legacy shape; `alpha02`/`beta01` are reshaped — host-owned `FrameRecomposer` + `measureAndLayout`/`draw`). Kinvoicing survives this with a **single-binary reflective scene driver** mirroring compose2pdf 1.2.0:
  - `render-html/.../internal/ComposeSceneRenderer.kt` drives the scene by reflection, detecting the shape structurally (`FrameRecomposer` present → `NextDriver` ≥1.12 else `LegacyDriver`). It's the only kinvoicing code touching that API directly; keep it in lockstep with compose2pdf's driver. `render-pdf` inherits CMP compatibility for free through `compose2pdf.renderToPdf` (bump the `compose2pdf` catalog pin). `render-compose` needs no driver (uses stable public Compose APIs).
  - `.github/workflows/compatibility.yml` proves the **shipped** JVM renderer binaries per cell: publish `render-html`/`render-pdf` (+ transitive `core`/`render-compose`, JVM publications only, `-PVERSION_NAME=0.0.0-compat-SNAPSHOT` so signing is skipped) to mavenLocal at the pinned base, then run the standalone `compat-consumer/` build forcing the Compose group to the cell version (`resolutionStrategy.eachDependency`, an allowlist of `org.jetbrains.compose*` subgroups). `render-compose` keeps a recompile-per-cell leg (`:render-compose:jvmTest`/`wasmJsNodeTest`/`iosSimulatorArm64Test` via the `perl` catalog override) — a multi-target KMP klib can't be reflectively bridged. Prerelease cells are non-blocking (`continue-on-error: contains(version, '-')`).
  - Supported versions are documented by `.github/scripts/render-compat-tables.py`, which injects a `| CMP | Kotlin | Status |` table (from `compose-versions.json` + the `libs.versions.toml` pins) between `<!-- BEGIN/END cmp-matrix -->` markers in `docs/compatibility.md` + `README.md`; a `--check` docs-sync gate (in `build.yml`'s CI Gate) fails on drift. `.github/workflows/update-compose-versions.yml` refreshes the JSON weekly, auto-bumps the build base to the latest stable, regenerates the docs, and opens a PR via App token. Mirrors compose2pdf 1.2.0 intentionally — keep them in lockstep.

## Code Style

- Kotlin Multiplatform: core/render-compose/render-html-email target all KMP/CMP platforms with `commonMain` source sets
- render-compose has platform-specific source sets: `jvmMain`, `nativeMain`, `wasmJsMain`, `androidMain` for image decoding and blocking compat
- render-html/examples/fidelity-test are JVM-only modules
- `explicitApi()` enabled on published modules (core, render-*)
- `explicitApi()` + KMP module boundaries: `internal` functions in core are NOT visible to render-* modules. Cross-module utilities must be `public`.
- Test fixtures in `core`: `InvoiceFixtures.all` → `List<InvoiceDocument>` (unnamed, 7 fixtures)
- Example fixtures in `kinvoicing-examples`: `InvoiceExamples.all` → `List<Pair<String, InvoiceDocument>>` (named, 15 fixtures)
- Dependencies managed via `gradle/libs.versions.toml` version catalog; reference as `libs.<name>`
- HTML safety tests: `EmailSafetyTest` (render-html-email safety, jsoup), `IframeSafetyTest` (render-html iframe safety, jsoup)
- Cross-platform tests in `commonTest` run on all targets; use `allTests` task to verify
- **CHANGELOG.md is reconciled at release time, not per-PR.** The `[Unreleased]` section stays empty between releases; do not add entries as PRs land. GitHub Release notes are auto-generated by `gh release create --generate-notes` from PR titles, independent of CHANGELOG.

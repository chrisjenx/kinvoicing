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
```

Fidelity report after tests: `kinvoicing-fidelity-test/build/reports/fidelity/index.html`

HTML fidelity tests require Playwright: `npx playwright install chromium` (skips gracefully if missing).

`:fidelity-test:jvmTest` is **macOS-only in CI** — libskiko-linux-x64.so SIGSEGVs in `SkPixmap::getColor` on ubuntu-latest, even with system fonts/fontconfig installed (issue #6). It's excluded from the Linux `jvm-tests` job and runs only in the macOS `fidelity-tests` job. Locally on Linux, expect the same crash.

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

**Dependency flow:** core → render-compose → render-pdf / render-html (both via compose2pdf) ; core → render-html-email (standalone)

**⚠ render-html does NOT depend on core.** Shared utilities from core must be duplicated in render-html or made `public`. render-html-email DOES depend on core.

### Platform Targets

| Module | Targets |
|--------|---------|
| core | jvm, android, iosArm64, iosSimulatorArm64, iosX64, wasmJs, macosArm64, macosX64, linuxX64, linuxArm64, mingwX64 |
| render-compose | jvm, android, iosArm64, iosSimulatorArm64, iosX64, wasmJs |
| render-html-email | jvm, android, iosArm64, iosSimulatorArm64, iosX64, wasmJs, macosArm64, macosX64, linuxX64, linuxArm64, mingwX64 |
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

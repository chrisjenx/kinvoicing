# Kinvoicing

Kotlin Multiplatform invoicing library with sealed IR, DSL builder, and three renderers (Compose, PDF, HTML).

## Commands

```bash
./gradlew build                              # Build all modules
./gradlew :core:jvmTest                      # Core IR + DSL tests (KMP module — use jvmTest)
./gradlew :fidelity-test:jvmTest             # Invoice renderer visual regression tests (KMP module — use jvmTest, not test)
./gradlew :kinvoicing-fidelity-test:test     # compose2pdf fidelity tests (Compose vs PDF vs HTML)
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
:fidelity-test           com.chrisjenx.kinvoicing.fidelity   Invoice renderer visual regression
```

**Dependency flow:** core → render-compose → render-pdf (via compose2pdf) / render-html-email

## Key Patterns

- **Sealed IR:** `InvoiceDocument` contains `List<InvoiceSection>` where `InvoiceSection` is sealed with 9 variants (Header, BillFrom, BillTo, LineItems, Summary, PaymentInfo, Footer, Custom, MetaBlock). Renderers use exhaustive `when` blocks.
- **DSL entry point:** `invoice { header { ... } lineItems { ... } summary { ... } }`
- **Shared composable:** `InvoiceContent(document)` is used by both Compose preview and PDF renderer — PDF matches Compose by construction.
- **compose2pdf** (`com.chrisjenx:compose2pdf:1.0.0`) is an external dependency providing `renderToPdf`, `PdfPageConfig`, `RenderMode`, `PdfLink`. Its `PdfLinkAnnotation` is `internal` — render-html defines its own link types for the HTML pipeline.

## Code Style

- Kotlin Multiplatform: core/render-compose/render-html-email use `commonMain`/`jvmMain` source sets
- render-html/examples/fidelity-test are JVM-only modules
- `explicitApi()` enabled on published modules (core, render-*)
- Test fixtures in `core`: `InvoiceFixtures.all` → `List<InvoiceDocument>` (unnamed, 6 fixtures)
- Example fixtures in `kinvoicing-examples`: `InvoiceExamples.all` → `List<Pair<String, InvoiceDocument>>` (named, 15 fixtures)
- Dependencies managed via `gradle/libs.versions.toml` version catalog; reference as `libs.<name>`
- HTML safety tests: `EmailSafetyTest` (render-html-email safety, jsoup), `IframeSafetyTest` (render-html iframe safety, jsoup)

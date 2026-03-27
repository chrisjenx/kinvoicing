# Kinvoicing

Kotlin Multiplatform invoicing library with sealed IR, DSL builder, and three renderers (Compose, PDF, HTML).

## Commands

```bash
./gradlew build                              # Build all modules
./gradlew :core:test                         # Core IR + DSL tests
./gradlew :fidelity-test:test                # Invoice renderer visual regression tests
./gradlew :kinvoicing-fidelity-test:test     # compose2pdf fidelity tests (Compose vs PDF vs HTML)
```

Fidelity report after tests: `kinvoicing-fidelity-test/build/reports/fidelity/index.html`

HTML fidelity tests require Playwright: `npx playwright install chromium` (skips gracefully if missing).

## Architecture

```
:core                    com.chrisjenx.kinvoicing            Invoice IR (sealed classes) + DSL
:render-compose          com.chrisjenx.kinvoicing.compose    InvoiceDocument → Compose UI
:render-html             com.chrisjenx.kinvoicing.html       InvoiceDocument → HTML (kotlinx.html)
:render-pdf              com.chrisjenx.kinvoicing.pdf        InvoiceDocument → PDF (via compose2pdf)
:kinvoicing-html         com.chrisjenx.kinvoicing.composehtml HTML rendering extensions on compose2pdf
:kinvoicing-examples     com.chrisjenx.kinvoicing.examples   Invoice example fixtures for docs/tests
:kinvoicing-fidelity-test com.chrisjenx.kinvoicing.fidelity.compose  Visual fidelity tests
:fidelity-test           com.chrisjenx.kinvoicing.fidelity   Invoice renderer visual regression
```

**Dependency flow:** core → render-compose → render-pdf (via compose2pdf) / render-html

## Key Patterns

- **Sealed IR:** `InvoiceDocument` contains `List<InvoiceSection>` where `InvoiceSection` is sealed with 9 variants (Header, BillFrom, BillTo, LineItems, Summary, PaymentInfo, Footer, Custom, MetaBlock). Renderers use exhaustive `when` blocks.
- **DSL entry point:** `invoice { header { ... } lineItems { ... } summary { ... } }`
- **Shared composable:** `InvoiceContent(document)` is used by both Compose preview and PDF renderer — PDF matches Compose by construction.
- **compose2pdf** (`com.chrisjenx:compose2pdf:1.0.0`) is an external dependency providing `renderToPdf`, `PdfPageConfig`, `RenderMode`, `PdfLink`. Its `PdfLinkAnnotation` is `internal` — kinvoicing-html defines its own link types for the HTML pipeline.

## Code Style

- Kotlin Multiplatform: core/render-compose/render-html use `commonMain`/`jvmMain` source sets
- kinvoicing-html/examples/fidelity-test are JVM-only modules
- `explicitApi()` enabled on published modules (core, render-*)
- Test fixtures in `core`: `InvoiceFixtures.basic`, `.fullFeatured`, `.minimal`, etc.
- Example fixtures in `kinvoicing-examples`: `InvoiceExamples.basic`, `.subItems`, `.adjustments`, etc.

# Technical Spec: compose2pdf Rendering Engine (Dependency Reference)

## Overview

Kinvoicing uses the [compose2pdf](https://github.com/nicferrier/compose2pdf) library to render Compose Desktop UI to PDF. compose2pdf uses Skia's SVGCanvas as an intermediate serialization format, then converts that SVG to PDFBox vector drawing commands. This avoids reimplementing Compose's layout and rendering engine — Skia does the hard work of capturing exactly what Compose draws, and the library replays it into a PDF.

## Pipeline

```
renderToPdf { Text("Hello"); Image(...); Box(...) }
    │
    ▼
 1. COMPOSE — Execute composable tree, capture Skia draw commands
    │
    ▼
 2. SERIALIZE — Replay captured commands onto SVGCanvas → SVG string
    │
    ├──── VECTOR (default) ──────────────────────┐
    │                                             │
    ▼                                             ▼
 3a. CONVERT — Walk SVG elements,             3b. RASTER — Render to bitmap,
     emit PDFBox drawing commands                  embed as single image
    │                                             │
    └─────────────────┬───────────────────────────┘
                      ▼
 4. ANNOTATE — Add link annotations + form fields with Y-flip coordinate transform
                      │
                      ▼
                 ByteArray (PDF)
```

## Step 1: Composition

The library creates a `CanvasLayersComposeScene` (internal Compose API) to execute the user's composable content. A `PictureRecorder` wraps the scene's canvas, capturing every Skia draw command into a replayable `Picture` object.

During composition, two `CompositionLocal` collectors are injected:
- **`LocalPdfLinkCollector`** — `PdfLink` composables register their href + bounds
- **`LocalPdfElementCollector`** — form composables (`PdfButton`, `PdfTextField`, `PdfCheckbox`, etc.) register their type + bounds

If `useBundledFont = true` (default), the content is wrapped in `ProvideTextStyle` with the bundled Inter font family, ensuring Compose and PDFBox use the exact same static font files.

## Step 2: SVG Serialization

The captured `Picture` is replayed onto Skia's `SVGCanvas`:

```
SVGCanvas.make(bounds, stream, convertTextToPaths=false, prettyXML=false)
picture.playback(svgCanvas)
→ SVG XML string
```

`convertTextToPaths = false` is critical — it preserves `<text>` elements so text remains selectable in the final PDF.

### What Skia's SVGCanvas actually produces

This is important to understand because it shapes every downstream decision:

- **Positions via transforms** — elements use `translate(x,y)` rather than x/y attributes
- **Per-glyph text positioning** — `<text>` elements have character-level `x` position arrays
- **Rounded rects become bezier paths** — non-uniform corner radii serialize as dozens of Q segments, not `<rect rx>`
- **Images as base64** — `Image` composables become `<image>` with inline PNG data URIs
- **System font names** — font-family uses whatever Skia resolves (e.g., ".SF NS" on macOS)

## Step 3a: SVG → PDF Conversion (Vector Mode)

`SvgToPdfConverter` walks the SVG and emits PDFBox `PDPageContentStream` commands.

**Coordinate flip**: PDF is Y-up (bottom-left origin), SVG is Y-down (top-left). A single page-level matrix handles this:
```
Matrix(1/density, 0, 0, -1/density, 0, pageHeight)
```

**Element mapping**:
- Shapes (rect, circle, ellipse) → PDFBox path primitives (circles use 4 cubic beziers with KAPPA≈0.5523)
- SVG `<path>` → full path command parser supporting M/L/H/V/C/S/Q/T/A/Z (arcs converted to cubics via SVG spec F.6)
- Text → per-glyph `showText()` with `newLineAtOffset()` for exact character positioning
- Images → base64 decoded → `ImageIO` → `LosslessFactory.createFromImage()` → `PDImageXObject` (lossless re-embedding)
- Groups → save/restore graphics state with transforms, opacity (`PDExtendedGraphicsState`), and clip paths

## Step 3b: Raster Mode (Fallback)

When `RenderMode.RASTER`: renders to `BufferedImage` via `ImageComposeScene`, embeds as a single `PDImageXObject`. Pixel-perfect but no text selectability and larger files.

## Step 4: Annotations

After page content is drawn, collected annotations are applied with coordinate transform (Compose Y-down → PDF Y-up):

- **Links** → `PDAnnotationLink` with `PDActionURI`, invisible border
- **Form fields** → AcroForm entries (push buttons, text fields, checkboxes, radio groups, dropdowns)

## Font Resolution

Three-tier system (`FontResolver`):

1. **Bundled Inter** (default) — 4 static variants loaded from classpath resources. Guarantees font match between Compose and PDFBox.
2. **System fonts** — platform-specific search paths, two-phase lookup (exact filename → recursive walk). Variable fonts (`fvar` table) are excluded.
3. **Standard14** — Helvetica/Times/Courier built into every PDF reader.

Fonts are cached per-document (shared across pages) and globally (file path lookups). Embedding uses `PDType0Font.load()` which automatically subsets to glyphs used.

## Multi-Page

A single `PDDocument` with shared font cache. For each page: clear collectors → render content(pageIndex) → fresh SVG → addPage → apply annotations.

## Gotchas & Things to Watch For

### Compose API Stability
- **`CanvasLayersComposeScene` is `@InternalComposeUiApi`** — the entire rendering pipeline depends on this internal API. Compose updates may break it without warning. There's no stable alternative for headless Compose rendering.

### SVG Intermediate Format Quirks
- **Skia's SVG output is not standard SVG** — it's a serialization of Skia's internal draw commands. Element structure, attribute usage, and transform nesting reflect Skia internals, not what a human would write. The converter must handle Skia's specific idioms.
- **Non-uniform rounded rects serialize as complex bezier paths** — Skia emits dozens of Q segments instead of `<rect rx>`. This causes minor anti-aliasing artifacts in the PDF. Use `PdfRoundedCornerShape` or `.asPdfSafe()` to work around this.
- **Images round-trip through base64** — Compose images → Skia base64 PNG in SVG → decoded → re-embedded as PDImageXObject. No quality loss (lossless) but adds processing overhead and memory pressure for image-heavy documents.

### Font Handling
- **Variable fonts silently produce wrong output** — PDFBox renders variable fonts at default axis values, so bold text renders as regular weight. The library detects and excludes `fvar`-containing fonts, but if a new variable font slips through, text will look wrong with no error.
- **System font names may not match** — Skia may emit ".SF NS" or other platform-specific names. The font resolver must map these back to the intended font family. Bundled fonts avoid this entirely.
- **Font subsetting is automatic but opaque** — `PDType0Font.load()` subsets on save. If a glyph is missing, you get a silent substitution or blank, not an error.

### Coordinate System
- **Y-flip is the #1 source of annotation bugs** — the page-level flip handles content, but annotations (links, form fields) need their own coordinate transform: `pdfY = pageHeight - marginTop - composeY - elementHeight`. Getting this wrong puts annotations in the wrong place.
- **Density scaling compounds** — SVG is rendered at `density * pageDimensions` pixels, then scaled back by `1/density` in the PDF. Mismatched density between SVG generation and PDF conversion will silently produce wrong-sized output.

### Performance & Size
- **Large documents with many images** can consume significant memory due to base64 encoding/decoding in the SVG intermediate step.
- **Font embedding adds size** — each unique font variant adds its subset to the PDF. 4 Inter variants are modest, but using many system fonts can bloat output.
- **Per-glyph text positioning** is accurate but verbose in the PDF content stream — every character gets its own positioning command.

### Testing
- **Fidelity diff images use 10x amplification** — bright colors in diff images are expected for vector mode. Check RMSE values, not visual appearance of diff images.
- **HTML fidelity tests need Playwright** — `npx playwright install chromium`. If not installed, HTML tests skip gracefully.
- **PDF vs Compose comparison is inherently approximate** — the SVG intermediate, bezier approximations, and font rendering differences mean vector PDFs will never be pixel-identical to Compose. The fidelity test suite defines acceptable thresholds.

## File Map

```
kinvoicing/
├── core/                          # Invoice IR + DSL (com.chrisjenx.kinvoicing)
├── render-compose/                # InvoiceDocument → Compose (com.chrisjenx.kinvoicing.compose)
├── render-html/                   # InvoiceDocument → HTML (com.chrisjenx.kinvoicing.html)
├── render-pdf/                    # InvoiceDocument → PDF via compose2pdf (com.chrisjenx.kinvoicing.pdf)
├── kinvoicing-html/               # HTML rendering extensions on compose2pdf (com.chrisjenx.kinvoicing.composehtml)
│   ├── ComposeHtml.kt             # Public API: renderToHtml()
│   ├── PdfLink.kt                 # Link composable + collector (for HTML pipeline)
│   ├── PdfElementCollector.kt     # Element annotation collector
│   ├── Pdf{Button,TextField,Image,...}.kt  # Semantic element composables
│   └── internal/
│       ├── ComposeToSvg.kt        # Compose → SVG (shared with compose2pdf)
│       ├── SvgParser.kt           # SVG XML → SvgNode tree
│       ├── HtmlRenderer.kt        # Orchestrator: SVG → HTML
│       └── CssEmitter.kt          # HTML document generation
├── kinvoicing-examples/           # Invoice example fixtures (com.chrisjenx.kinvoicing.examples)
├── kinvoicing-fidelity-test/      # Fidelity tests: Compose vs PDF vs HTML (com.chrisjenx.kinvoicing.fidelity.compose)
└── fidelity-test/                 # Invoice renderer visual tests (com.chrisjenx.kinvoicing.fidelity)
```

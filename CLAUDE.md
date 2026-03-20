# CLAUDE.md

## Project Overview

**compose-pdf** is a Kotlin JVM library that renders Compose Desktop content to PDF.

**Branch**: `skiko-render` — experimental, building toward vector PDF output via Skia's native PDF backend.

**Current state (v0.1)**: Raster rendering via ImageComposeScene + PDFBox. The API surface is final; the rendering backend will be swapped to Skia's PDF canvas.

## Module Map

```
compose-pdf/
├── compose-pdf-core/    # Library: public API + renderer
├── compose-pdf-test/    # Integration + fidelity tests
├── sample-desktop/      # Compose Desktop demo app
└── sample-ktor/         # Headless server demo (planned)
```

## Tech Stack

- **Kotlin** 2.1.21, JVM target only
- **Compose Multiplatform** 1.7.3 (Desktop)
- **Apache PDFBox** 3.0.7 (v0.1 PDF creation — will be replaced by Skia PDF backend)
- **Gradle** 8.14

## Build Commands

```bash
./gradlew :compose-pdf-core:build      # Build core library
./gradlew :compose-pdf-core:test       # Run unit tests
./gradlew :sample-desktop:run          # Run desktop demo
```

## Public API

```kotlin
// Single page
renderToPdf(config, density) { Text("Hello") } → ByteArray

// Multi-page
renderToPdf(pages = 3, config, density) { pageIndex → content } → ByteArray
```

Types: `PdfPageConfig` (A4/Letter/A3 presets), `PdfMargins`, `Density`.

## Architecture

```
renderToPdf { content() }
  → ImageComposeScene (offscreen Compose → raster bitmap)
  → PDFBox (embed bitmap into PDF page)
  → ByteArray
```

**Future (vector path)**:
```
renderToPdf { content() }
  → ComposeScene.render(pdfCanvas)  // @InternalComposeUiApi
  → Skia PDF Document (SkPDF::MakeDocument)
  → ByteArray (vector PDF)
```

## Key Research Findings

- Skia's PDF symbols ARE compiled into Skiko's native binary (skia_enable_pdf defaults to true)
- LoadingByte's PR #775 adds Kotlin/JNI wrappers but is unmerged (binary size concerns)
- `ComposeScene.render(canvas)` exists (`@InternalComposeUiApi`) for direct canvas rendering
- `ImageComposeScene` works fully headless (CPU-only, no display/GPU needed)

## Code Conventions

- Package: `com.chrisjenx.composepdf`
- Internal implementation in `com.chrisjenx.composepdf.internal`
- Page dimensions in `Dp` (mapping to PDF points)
- Standard JVM source layout: `src/main/kotlin`, `src/test/kotlin`

## Group ID

`com.chrisjenx`

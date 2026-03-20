# compose-pdf

A Kotlin JVM library that renders Compose Desktop content directly to PDF. Supports vector rendering (selectable text, crisp scaling) via Skia's SVGCanvas and PDFBox, with a raster fallback mode for pixel-perfect output.

## Quick Start

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.chrisjenx:compose-pdf-core:<version>")
}
```

## Usage

### Single page

```kotlin
val pdfBytes: ByteArray = renderToPdf {
    Text("Hello, PDF!")
}
File("output.pdf").writeBytes(pdfBytes)
```

### Multi-page

```kotlin
val pdfBytes = renderToPdf(pages = 3) { pageIndex ->
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Page ${pageIndex + 1}")
    }
}
```

### Page configuration

```kotlin
val pdfBytes = renderToPdf(
    config = PdfPageConfig.Letter.copy(margins = PdfMargins.Normal),
    density = Density(2f),
) {
    Text("US Letter with margins")
}
```

Built-in page sizes: `PdfPageConfig.A4`, `PdfPageConfig.Letter`, `PdfPageConfig.A3`.

### Link annotations

```kotlin
renderToPdf {
    PdfLink(href = "https://example.com") {
        Text("Click me", color = Color.Blue)
    }
}
```

Links are clickable in the generated PDF. Outside of `renderToPdf`, `PdfLink` is a no-op wrapper.

### PDF-safe rounded corners

Skia's SVGCanvas serializes `RoundRect` clip paths as `<rect rx="..." ry="...">`, which only supports uniform corner radii. Use `PdfRoundedCornerShape` for per-corner radii in vector PDFs:

```kotlin
Box(
    Modifier.clip(PdfRoundedCornerShape(topStart = 24.dp, bottomEnd = 24.dp))
        .background(Color.Blue)
)
```

For uniform corners, standard `RoundedCornerShape` works fine. You can also wrap any existing shape: `myShape.asPdfSafe()`.

### Bundled fonts

By default, `renderToPdf` uses bundled Inter fonts (`useBundledFont = true`) so that Compose and PDFBox render with the exact same font file. Pass `useBundledFont = false` to use system fonts instead.

```kotlin
renderToPdf(useBundledFont = false) {
    Text("Rendered with system fonts")
}
```

The bundled font family is also available as `PdfFontFamily` for use in your composables.

## Render Modes

| Mode | Description |
|------|-------------|
| `RenderMode.VECTOR` (default) | Compose → Skia SVGCanvas → SVG → PDFBox vector commands. Produces small PDFs with selectable text and sharp scaling. |
| `RenderMode.RASTER` | Compose → ImageComposeScene → embedded bitmap. Pixel-perfect but larger files, no text selection. |

```kotlin
renderToPdf(mode = RenderMode.RASTER) { content() }
```

## Modules

```
├── compose-pdf-core/    # Library: public API + renderers + SVG→PDF converter
├── compose-pdf-test/    # Fidelity comparison tests (34 fixtures, HTML report)
├── sample-desktop/      # Compose Desktop demo app (invoice with links + image)
└── sample-ktor/         # Headless server demo (PDF endpoint on port 8080)
```

## Build & Test

```bash
./gradlew :compose-pdf-core:build          # Build core library
./gradlew :compose-pdf-core:test           # Run unit tests
./gradlew :compose-pdf-test:test           # Run fidelity comparison tests (generates HTML report)
./gradlew :sample-desktop:run              # Run desktop demo
./gradlew :sample-ktor:run                 # Run headless server (port 8080)
```

## Tech Stack

- Kotlin 2.1.21, JVM target
- Compose Multiplatform 1.7.3 (Desktop)
- Apache PDFBox 3.0.7
- Skiko (Skia for Kotlin)

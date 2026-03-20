# CLAUDE.md

## Project Overview

**compose-pdf** is a Kotlin JVM library that renders Compose Desktop content to PDF.

**Branch**: `skiko-render` — hybrid SVG + PDFBox vector rendering.

**Current state**: Dual-mode rendering (vector default, raster fallback) with selectable text, font embedding, image support, and link annotations.

## Module Map

```
├── compose-pdf-core/    # Library: public API + renderers + SVG→PDF converter
├── compose-pdf-test/    # Fidelity comparison tests (9 fixtures, HTML report)
├── sample-desktop/      # Compose Desktop demo app (invoice with links)
└── sample-ktor/         # Headless server demo (PDF endpoint)
```

## Tech Stack

- **Kotlin** 2.1.21, JVM target only
- **Compose Multiplatform** 1.7.3 (Desktop)
- **Apache PDFBox** 3.0.7 (SVG→PDF conversion, image embedding, font subsetting)
- **Gradle** 8.14

## Build Commands

```bash
./gradlew :compose-pdf-core:build      # Build core library
./gradlew :compose-pdf-core:test       # Run unit tests (19 tests)
./gradlew :compose-pdf-test:test       # Run fidelity comparison tests
./gradlew :sample-desktop:run          # Run desktop demo
./gradlew :sample-ktor:run             # Run headless server (port 8080)
```

## Public API

```kotlin
// Single page (vector by default)
renderToPdf(config, density, mode) { Text("Hello") } → ByteArray

// Multi-page
renderToPdf(pages = 3, config, density, mode) { pageIndex → content } → ByteArray

// Link annotations
PdfLink(href = "https://example.com") { Text("Click me") }
```

Types: `PdfPageConfig` (A4/Letter/A3 presets), `PdfMargins`, `Density`, `RenderMode` (VECTOR/RASTER), `PdfLink`, `PdfLinkAnnotation`.

## Architecture

```
renderToPdf { content() }
  │
  ├─ VECTOR mode (default):
  │   CanvasLayersComposeScene (@InternalComposeUiApi)
  │     → PictureRecorder canvas (records all Skia draw ops)
  │     → Picture.playback(svgCanvas) → SVG string
  │     → SvgToPdfConverter → PDFBox vector drawing commands
  │       ├─ Shapes: rect, circle, ellipse, line, polyline, polygon, path
  │       ├─ Path commands: M/L/H/V/C/S/Q/T/A/Z (all variants)
  │       ├─ Images: base64 data URI → LosslessFactory → PDImageXObject
  │       ├─ Fonts: FontResolver → PDType0Font (embedded+subsetted) or Standard14
  │       ├─ Opacity: PDExtendedGraphicsState
  │       ├─ Clipping: clipPath via PDFBox clip()
  │       └─ Transforms: translate/scale/rotate/matrix/skewX/skewY
  │     → Link annotations from PdfLinkCollector → PDAnnotationLink
  │     → ByteArray (vector PDF)
  │
  └─ RASTER mode (fallback):
      ImageComposeScene → Image → PDFBox embedded bitmap
      → Link annotations → ByteArray (raster PDF)
```

## Key Research Findings

- Skia's PDF symbols are NOT in Skiko's native binary (checked via `nm` on 0.8.18 and 0.9.47)
- Skia's SVGCanvas IS available and produces clean vector SVG from Compose content
- SVGCanvas emits `<image>` with base64 PNG data URIs for Image composables
- `CanvasLayersComposeScene` accepts any Skia Canvas for rendering
- PDFBox handles font subsetting automatically via `PDType0Font.load(doc, file)`
- The public API is backend-agnostic; if Skiko merges PDF support, `RenderMode.SKIA_PDF` can be added

## Code Conventions

- Package: `com.chrisjenx.composepdf`
- Internal implementation in `com.chrisjenx.composepdf.internal`
- Page dimensions in `Dp` (mapping to PDF points)
- Standard JVM source layout: `src/main/kotlin`, `src/test/kotlin`

## Group ID

`com.chrisjenx`

# context-mode — MANDATORY routing rules

You have context-mode MCP tools available. These rules are NOT optional — they protect your context window from flooding. A single unrouted command can dump 56 KB into context and waste the entire session.

## BLOCKED commands — do NOT attempt these

### curl / wget — BLOCKED
Any Bash command containing `curl` or `wget` is intercepted and replaced with an error message. Do NOT retry.
Instead use:
- `ctx_fetch_and_index(url, source)` to fetch and index web pages
- `ctx_execute(language: "javascript", code: "const r = await fetch(...)")` to run HTTP calls in sandbox

### Inline HTTP — BLOCKED
Any Bash command containing `fetch('http`, `requests.get(`, `requests.post(`, `http.get(`, or `http.request(` is intercepted and replaced with an error message. Do NOT retry with Bash.
Instead use:
- `ctx_execute(language, code)` to run HTTP calls in sandbox — only stdout enters context

### WebFetch — BLOCKED
WebFetch calls are denied entirely. The URL is extracted and you are told to use `ctx_fetch_and_index` instead.
Instead use:
- `ctx_fetch_and_index(url, source)` then `ctx_search(queries)` to query the indexed content

## REDIRECTED tools — use sandbox equivalents

### Bash (>20 lines output)
Bash is ONLY for: `git`, `mkdir`, `rm`, `mv`, `cd`, `ls`, `npm install`, `pip install`, and other short-output commands.
For everything else, use:
- `ctx_batch_execute(commands, queries)` — run multiple commands + search in ONE call
- `ctx_execute(language: "shell", code: "...")` — run in sandbox, only stdout enters context

### Read (for analysis)
If you are reading a file to **Edit** it → Read is correct (Edit needs content in context).
If you are reading to **analyze, explore, or summarize** → use `ctx_execute_file(path, language, code)` instead. Only your printed summary enters context. The raw file content stays in the sandbox.

### Grep (large results)
Grep results can flood context. Use `ctx_execute(language: "shell", code: "grep ...")` to run searches in sandbox. Only your printed summary enters context.

## Tool selection hierarchy

1. **GATHER**: `ctx_batch_execute(commands, queries)` — Primary tool. Runs all commands, auto-indexes output, returns search results. ONE call replaces 30+ individual calls.
2. **FOLLOW-UP**: `ctx_search(queries: ["q1", "q2", ...])` — Query indexed content. Pass ALL questions as array in ONE call.
3. **PROCESSING**: `ctx_execute(language, code)` | `ctx_execute_file(path, language, code)` — Sandbox execution. Only stdout enters context.
4. **WEB**: `ctx_fetch_and_index(url, source)` then `ctx_search(queries)` — Fetch, chunk, index, query. Raw HTML never enters context.
5. **INDEX**: `ctx_index(content, source)` — Store content in FTS5 knowledge base for later search.

## Subagent routing

When spawning subagents (Agent/Task tool), the routing block is automatically injected into their prompt. Bash-type subagents are upgraded to general-purpose so they have access to MCP tools. You do NOT need to manually instruct subagents about context-mode.

## Output constraints

- Keep responses under 500 words.
- Write artifacts (code, configs, PRDs) to FILES — never return them as inline text. Return only: file path + 1-line description.
- When indexing content, use descriptive source labels so others can `ctx_search(source: "label")` later.

## ctx commands

| Command | Action |
|---------|--------|
| `ctx stats` | Call the `ctx_stats` MCP tool and display the full output verbatim |
| `ctx doctor` | Call the `ctx_doctor` MCP tool, run the returned shell command, display as checklist |
| `ctx upgrade` | Call the `ctx_upgrade` MCP tool, run the returned shell command, display as checklist |

---
title: Installation
parent: Getting Started
nav_order: 1
---

# Installation

Add the core module and the renderer(s) you need:

```kotlin
dependencies {
    // Required
    implementation("com.chrisjenx.kinvoicing:core:<version>")

    // Pick your renderer(s):
    implementation("com.chrisjenx.kinvoicing:render-pdf:<version>")
    implementation("com.chrisjenx.kinvoicing:render-html-email:<version>")
    implementation("com.chrisjenx.kinvoicing:render-compose:<version>")
    implementation("com.chrisjenx.kinvoicing:render-html:<version>")
}
```

## Modules

| Module | Description | Key Dependency | Platforms |
|--------|-------------|----------------|-----------|
| `core` | Invoice IR, DSL builder, currency formatting | kotlinx-datetime | JVM, Android, iOS, wasmJs, macOS, Linux, Windows |
| `render-compose` | Compose Multiplatform UI | Compose | JVM, Android, iOS, wasmJs |
| `render-html-email` | Email-safe HTML (inline styles, table layout) | kotlinx-html | JVM, Android, iOS, wasmJs, macOS, Linux, Windows |
| `render-pdf` | PDF output via compose2pdf | Compose Desktop | JVM only |
| `render-html` | Print-quality HTML (SVG-based, embedded fonts) | compose2pdf | JVM only |

## Platform Support

Kinvoicing is built with Kotlin Multiplatform and publishes artifacts for all major platforms.

**Full KMP** (`core`, `render-html-email`): JVM, Android, iOS (arm64, simulatorArm64, x64), wasmJs, macOS (arm64, x64), Linux (x64, arm64), Windows (mingwX64).

**Compose Multiplatform** (`render-compose`): JVM Desktop, Android, iOS (arm64, simulatorArm64, x64), wasmJs.

**JVM only** (`render-pdf`, `render-html`): Depend on Compose Desktop and compose2pdf.

{: .note }
If you only need email HTML output, `render-html-email` has a very lightweight dependency footprint (just kotlinx-html) and supports every KMP target including server-side native (Linux).

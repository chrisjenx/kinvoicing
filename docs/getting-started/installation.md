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

| Module | Description | Key Dependency |
|--------|-------------|----------------|
| `core` | Invoice IR, DSL builder, currency formatting | kotlinx-datetime |
| `render-pdf` | PDF output via compose2pdf | Compose Desktop |
| `render-html-email` | Email-safe HTML (inline styles, table layout) | kotlinx-html (lightweight) |
| `render-compose` | Compose Multiplatform UI | Compose |
| `render-html` | Print-quality HTML (SVG-based, embedded fonts) | compose2pdf |

{: .note }
If you only need email HTML output, `render-html-email` has a very lightweight dependency footprint (just kotlinx-html).

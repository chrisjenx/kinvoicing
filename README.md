# Kinvoicing

Kotlin Multiplatform invoicing library with a sealed IR, type-safe DSL builder, and four renderers (Compose, PDF, HTML, Email HTML).

[![Maven Central](https://img.shields.io/maven-central/v/com.chrisjenx.kinvoicing/core.svg)](https://central.sonatype.com/namespace/com.chrisjenx.kinvoicing)
[![Build](https://github.com/chrisjenx/kinvoicing/actions/workflows/build.yml/badge.svg)](https://github.com/chrisjenx/kinvoicing/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-purple.svg)](https://kotlinlang.org)
[![Docs](https://img.shields.io/badge/Docs-kinvoicing-blue.svg)](https://chrisjenx.github.io/kinvoicing)

> **[Full documentation](https://chrisjenx.github.io/kinvoicing)** — installation, section guides, theming, branding, recipes, and examples.

## Installation

```kotlin
dependencies {
    // Core IR + DSL (all KMP targets)
    implementation("com.chrisjenx.kinvoicing:core:1.0.0")

    // Renderers — pick the ones you need:

    // Compose UI (JVM, Android, iOS, wasmJs)
    implementation("com.chrisjenx.kinvoicing:render-compose:1.0.0")

    // Email-safe HTML — inline styles, table layout (all KMP targets)
    implementation("com.chrisjenx.kinvoicing:render-html-email:1.0.0")

    // JVM only:
    implementation("com.chrisjenx.kinvoicing:render-pdf:1.0.0")   // PDF via compose2pdf
    implementation("com.chrisjenx.kinvoicing:render-html:1.0.0")  // Print-quality HTML via compose2pdf
}
```

## Quick Start

### Build an invoice

```kotlin
val doc = invoice {
    header {
        branding {
            primary {
                name("Acme Corp")
                address("123 Main St", "Springfield, IL 62701")
                email("billing@acme.com")
            }
        }
        invoiceNumber("INV-2026-0001")
        issueDate(LocalDate(2026, 3, 1))
        dueDate(LocalDate(2026, 3, 31))
    }
    billFrom {
        name("Acme Corp")
        address("123 Main St", "Springfield, IL 62701")
        email("billing@acme.com")
        phone("+1 (555) 100-0001")
    }
    billTo {
        name("Jane Smith")
        address("456 Oak Ave", "Boulder, CO 80301")
        email("jane@example.com")
        phone("+1 (555) 200-0002")
    }
    lineItems {
        columns("Description", "Qty", "Rate", "Amount")
        item("Web Development", qty = 40, unitPrice = 150.0)
        item("Design Services", qty = 10, unitPrice = 100.0)
        item("Hosting (Monthly)", qty = 1, unitPrice = 49.99)
    }
    summary {
        currency("USD")
    }
    footer {
        notes("Thank you for your business!")
        terms("Net 30")
    }
}
```

### Render it

```kotlin
// PDF — write to file
val pdf: ByteArray = doc.toPdf()
File("invoice.pdf").writeBytes(pdf)

// PDF — US Letter page size
val letterPdf = doc.toPdf(PdfRenderConfig.Letter)

// Email-safe HTML (inline styles, table layout — works in Gmail, Outlook, etc.)
val emailHtml: String = doc.toHtml()

// Compose UI — drop into any Compose screen
@Composable
fun InvoiceScreen() {
    InvoiceView(doc)
}

// Print-quality HTML (SVG-based, embedded fonts, print styles)
val printHtml: String = renderToHtml { InvoiceContent(doc) }
```

## Theming

Kinvoicing ships with 8 built-in themes:

| Theme | Character |
|-------|-----------|
| `Classic` | Default — clean blue on white |
| `Corporate` | Navy with accent stripe, professional |
| `Modern` | Indigo tones, airy, minimal decoration |
| `Bold` | Strong blue, grid lines, stacked header |
| `Warm` | Amber earth tones, Georgia font, friendly |
| `Minimal` | Near-monochrome, ultra-clean |
| `Elegant` | Dark stone + gold, Georgia font, refined |
| `Fresh` | Green/teal, clean, optimistic |

Apply a theme:

```kotlin
val doc = invoice {
    style {
        theme(InvoiceThemes.Modern)
    }
    // ... sections
}
```

Override individual properties after applying a theme:

```kotlin
style {
    theme(InvoiceThemes.Elegant)
    accentBorder = true
    showGridLines = true
    primaryColor = 0xFF1D4ED8
    fontFamily = "Helvetica"
}
```

### Style properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `primaryColor` | `Long` (ARGB) | Blue | Headings, totals, accent elements |
| `secondaryColor` | `Long` (ARGB) | Slate | Labels, secondary text |
| `textColor` | `Long` (ARGB) | Dark | Body text |
| `backgroundColor` | `Long` (ARGB) | White | Page background |
| `fontFamily` | `String` | `"Inter"` | Font family name |
| `logoPlacement` | `LogoPlacement` | `LEFT` | `LEFT`, `CENTER`, or `RIGHT` |
| `headerLayout` | `HeaderLayout` | `HORIZONTAL` | `HORIZONTAL` or `STACKED` |
| `showGridLines` | `Boolean` | `false` | Draw borders around line-item rows |
| `accentBorder` | `Boolean` | `false` | Colored top/left border accent |
| `negativeColor` | `Long` (ARGB) | Red | Discounts, credits, negative amounts |
| `borderColor` | `Long` (ARGB) | Light gray | Table borders, grid lines |
| `dividerColor` | `Long` (ARGB) | Lighter gray | Subtle row dividers |
| `mutedBackgroundColor` | `Long` (ARGB) | Near-white | Alternating rows, meta blocks |
| `surfaceColor` | `Long` (ARGB) | White | Card-like section backgrounds |

### Material 3 integration

In a Compose app, derive invoice styles from your Material theme:

```kotlin
@Composable
fun ThemedInvoice(doc: InvoiceDocument) {
    val style = InvoiceStyle.fromMaterialTheme {
        copy(accentBorder = true) // optional overrides
    }
    InvoiceView(doc.copy(style = style))
}
```

## Branding & Logos

### Logo

Provide a logo as a `ByteArray` with its MIME type:

```kotlin
header {
    branding {
        primary {
            name("Acme Corp")
            logo(logoBytes, "image/png")  // PNG, JPEG, etc.
        }
    }
}
```

### Dual branding

Show a secondary brand (e.g., a payment processor or platform):

```kotlin
header {
    branding {
        layout = BrandLayout.DUAL_HEADER  // Both side-by-side
        primary {
            name("My Company")
            logo(myLogoBytes, "image/png")
        }
        poweredBy {
            name("Powered by Platform")
            logo(platformLogoBytes, "image/png")
        }
    }
}
```

**Layout options:**
- `POWERED_BY_FOOTER` (default) — primary in header, powered-by in footer
- `POWERED_BY_HEADER` — primary in header, powered-by in header subtitle
- `DUAL_HEADER` — both brands side-by-side in header

## Renderer Configuration

### PDF

```kotlin
// Default: A4, vector rendering, auto-pagination
doc.toPdf()

// US Letter
doc.toPdf(PdfRenderConfig.Letter)

// Custom config
doc.toPdf(PdfRenderConfig(
    pageConfig = PdfPageConfig.A4,
    renderMode = RenderMode.VECTOR,
    pagination = PdfPagination.AUTO,
    density = Density(2f),
))
```

Long invoices automatically paginate across multiple pages.

### Email HTML

```kotlin
// Default: full document with embedded images
doc.toHtml()

// Fragment for embedding in an existing email template
doc.toHtml(HtmlRenderConfig(
    embedImages = true,
    includeDoctype = false,
    wrapInBody = false,
))
```

Output uses inline styles and table layout for maximum email client compatibility (Gmail, Outlook, Apple Mail, etc.).

## Advanced Features

### Adjustments

Apply tax, discounts, fees, and credits at line-item or summary level:

```kotlin
lineItems {
    item("Consulting", qty = 10, unitPrice = 200.0)
    item("Rush Fee", amount = 50.0)
}
summary {
    currency("USD")
    tax("Sales Tax", percent = 8.0)             // 8% tax
    discount("Early Payment", percent = 10.0)  // 10% discount
    fee("Processing Fee", fixed = 25.0)       // Flat fee
    credit("Previous Credit", 100.0)          // Credit applied
}
```

### Sub-items

Nest detail rows under a parent line item:

```kotlin
lineItems {
    item("Project Alpha") {
        sub("Design Phase", qty = 20, unitPrice = 150.0)
        sub("Development Phase", qty = 40, unitPrice = 175.0)
        sub("Testing", qty = 10, unitPrice = 125.0)
    }
}
```

### Payment info

```kotlin
paymentInfo {
    bankName("First National Bank")
    accountNumber("1234567890")
    routingNumber("021000021")
    paymentLink("https://pay.example.com/inv-001")
    notes("Wire transfer preferred for amounts over $1,000")
}
```

### Metadata blocks

Add key-value metadata (PO numbers, project names, etc.):

```kotlin
metaBlock {
    entry("PO Number", "PO-2026-0042")
    entry("Project", "Website Redesign")
    entry("Department", "Engineering")
}
```

### Custom sections

Build freeform sections from primitives:

```kotlin
custom("terms-detail") {
    text("Extended Payment Terms", styleRef = "bold")
    divider()
    text("This invoice is subject to the terms outlined in Contract #C-2026-001.")
    spacer()
}
```

## Modules

| Module | Artifact | Description | Platforms |
|--------|----------|-------------|-----------|
| `:core` | `core` | Invoice IR (sealed classes), DSL builder, currency formatting | JVM, Android, iOS, wasmJs, macOS, Linux, Windows |
| `:render-compose` | `render-compose` | Compose Multiplatform UI renderer | JVM, Android, iOS, wasmJs |
| `:render-html-email` | `render-html-email` | Email-safe HTML renderer via kotlinx.html | JVM, Android, iOS, wasmJs, macOS, Linux, Windows |
| `:render-pdf` | `render-pdf` | PDF renderer via compose2pdf | JVM only |
| `:render-html` | `render-html` | Compose → HTML renderer via compose2pdf | JVM only |

## Architecture

The core module defines a sealed `InvoiceDocument` IR with 9 section variants (Header, BillFrom, BillTo, LineItems, Summary, PaymentInfo, Footer, Custom, MetaBlock). Renderers use exhaustive `when` blocks over the sealed hierarchy, so adding a new section type is a compile-time-checked change.

The Compose and PDF renderers share a single `InvoiceContent` composable — PDF matches Compose by construction via [compose2pdf](https://github.com/nicbell/compose2pdf). The HTML renderer also uses compose2pdf to produce SVG-based print-quality output. The email HTML renderer is standalone, using kotlinx.html with inline styles and table layout for maximum email client compatibility.

```
core (sealed IR + DSL)
 ├── render-compose  (Compose UI)
 │    ├── render-pdf  (PDF via compose2pdf)
 │    └── render-html (HTML via compose2pdf)
 └── render-html-email (standalone, kotlinx.html)
```

Cross-renderer fidelity is verified by automated tests that compare Compose, PDF, HTML, and email HTML output using RMSE and SSIM metrics. See the [docs](https://chrisjenx.github.io/kinvoicing) for details.

## License

```
Copyright 2026 Christopher Jenkins

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

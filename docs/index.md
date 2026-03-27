---
title: Home
layout: default
nav_order: 1
---

# Kinvoicing

Kotlin Multiplatform invoicing library with a sealed IR, type-safe DSL builder, and four renderers.

{: .fs-6 .fw-300 }

Build professional invoices in Kotlin and render them to PDF, email-safe HTML, print-quality HTML, or Compose UI.

---

## Quick Example

```kotlin
val doc = invoice {
    header {
        branding { primary { name("Acme Corp") } }
        invoiceNumber("INV-2026-0001")
        issueDate(LocalDate(2026, 3, 1))
        dueDate(LocalDate(2026, 3, 31))
    }
    billFrom { name("Acme Corp"); email("billing@acme.com") }
    billTo { name("Jane Smith"); email("jane@example.com") }
    lineItems {
        columns("Description", "Qty", "Rate", "Amount")
        item("Web Development", qty = 40, unitPrice = 150.0)
        item("Design Services", qty = 10, unitPrice = 100.0)
    }
    summary { currency("USD") }
    footer { notes("Thank you!") }
}

// Render
val pdf: ByteArray = doc.toPdf()
val emailHtml: String = doc.toHtml()
```

{% include example-preview.html name="basic" height="600px" %}

## Features

- **Type-safe DSL** -- build invoices with compile-time checked Kotlin code
- **Sealed IR** -- 9 section types with exhaustive rendering
- **4 renderers** -- PDF, email HTML, print HTML, Compose UI
- **8 built-in themes** -- Corporate, Modern, Bold, Warm, Minimal, Elegant, Fresh, Classic
- **Branding** -- logos, dual branding, custom layouts
- **Adjustments** -- tax, discount, fee, credit at line-item or summary level
- **Multiplatform** -- core, Compose, and email renderers support JVM, Android, iOS, and wasmJs; PDF/HTML renderers are JVM-only

[Get Started](getting-started/){: .btn .btn-primary .fs-5 .mb-4 .mb-md-0 .mr-2 }
[View on GitHub](https://github.com/chrisjenx/kinvoicing){: .btn .fs-5 .mb-4 .mb-md-0 }

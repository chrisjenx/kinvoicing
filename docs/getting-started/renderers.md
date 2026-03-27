---
title: Renderers
parent: Getting Started
nav_order: 3
---

# Renderers

Kinvoicing provides four renderers. Each takes an `InvoiceDocument` and produces output in a different format.

First, build a document with the DSL (see [Your First Invoice](first-invoice) for the full walkthrough):

```kotlin
val doc = invoice {
    header {
        branding { primary { name("Acme Corp") } }
        invoiceNumber("INV-2026-0001")
        issueDate(LocalDate(2026, 3, 1))
        dueDate(LocalDate(2026, 3, 31))
    }
    lineItems {
        columns("Description", "Qty", "Rate", "Amount")
        item("Consulting", qty = 10, unitPrice = 150.0)
    }
    summary { currency("USD") }
}
```

Then pass `doc` to any renderer:

## PDF

```kotlin
import com.chrisjenx.kinvoicing.pdf.toPdf
import com.chrisjenx.kinvoicing.pdf.PdfRenderConfig

// Default (A4, vector, auto-pagination)
val pdf: ByteArray = doc.toPdf()
File("invoice.pdf").writeBytes(pdf)

// US Letter page size
val letterPdf = doc.toPdf(PdfRenderConfig.Letter)
```

Long invoices automatically paginate across multiple pages.

## Email HTML

```kotlin
import com.chrisjenx.kinvoicing.html.email.toHtml
import com.chrisjenx.kinvoicing.html.email.HtmlRenderConfig

// Full document with embedded images
val html: String = doc.toHtml()

// Fragment for embedding in an existing template
val fragment = doc.toHtml(HtmlRenderConfig(
    includeDoctype = false,
    wrapInBody = false,
))
```

Output uses inline styles and table layout for maximum email client compatibility (Gmail, Outlook, Apple Mail).

## Compose UI

```kotlin
import com.chrisjenx.kinvoicing.compose.InvoiceView

@Composable
fun InvoiceScreen() {
    InvoiceView(doc)
}
```

`InvoiceView` includes built-in vertical scrolling. For more control, use `InvoiceContent` directly.

## Print HTML

```kotlin
import com.chrisjenx.kinvoicing.html.renderToHtml
import com.chrisjenx.kinvoicing.compose.InvoiceContent

val html: String = renderToHtml {
    InvoiceContent(doc)
}
```

Produces SVG-based HTML with embedded fonts and print styles. Ideal for browser rendering and printing.

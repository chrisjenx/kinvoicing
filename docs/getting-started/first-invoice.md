---
title: First Invoice
parent: Getting Started
nav_order: 2
---

# Your First Invoice

Build an invoice using the type-safe DSL:

```kotlin
import com.chrisjenx.kinvoicing.invoice
import kotlinx.datetime.LocalDate

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

{% include example-preview.html name="basic" height="600px" %}

The `invoice { }` function returns an `InvoiceDocument` -- an immutable data structure that can be passed to any renderer.

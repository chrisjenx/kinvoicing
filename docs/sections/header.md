---
title: Header
parent: Sections
nav_order: 1
---

# Header

The header displays branding, invoice number, and dates. It's the first thing recipients see.

## Horizontal Layout (default)

```kotlin
header {
    branding {
        primary {
            name("Acme Corp")
            address("123 Main St", "Springfield, IL 62701")
            email("billing@acme.com")
            website("https://acme.com")
        }
    }
    invoiceNumber("INV-2026-0001")
    issueDate(LocalDate(2026, 3, 1))
    dueDate(LocalDate(2026, 3, 31))
}
```

{% include example-preview.html name="header-horizontal" height="500px" %}

## Stacked Layout

Set `headerLayout = HeaderLayout.STACKED` and optionally center the logo:

```kotlin
style {
    headerLayout = HeaderLayout.STACKED
    logoPlacement = LogoPlacement.CENTER
}
header {
    branding {
        primary {
            name("Acme Corp")
            address("123 Main St", "Springfield, IL 62701")
        }
    }
    invoiceNumber("INV-2026-0001")
    issueDate(LocalDate(2026, 3, 1))
    dueDate(LocalDate(2026, 3, 31))
}
```

{% include example-preview.html name="header-stacked" height="500px" %}

## Dual Branding

Show a secondary brand (e.g., a platform or payment processor):

```kotlin
header {
    branding {
        layout = BrandLayout.DUAL_HEADER
        primary {
            name("Client Company")
            email("billing@client.com")
        }
        poweredBy {
            name("Powered by Kinvoicing")
            tagline("Professional Invoicing")
        }
    }
    invoiceNumber("INV-2026-0042")
    issueDate(LocalDate(2026, 3, 1))
    dueDate(LocalDate(2026, 3, 31))
}
```

{% include example-preview.html name="header-dual-branding" height="500px" %}

## Builder Reference

### HeaderBuilder

| Method | Description |
|--------|-------------|
| `branding { }` | Configure branding (see [Branding](../styling/branding)) |
| `invoiceNumber(value)` | Invoice identifier string |
| `issueDate(LocalDate)` | Date the invoice was issued |
| `dueDate(LocalDate)` | Payment due date |

### BrandLayout Options

| Layout | Description |
|--------|-------------|
| `POWERED_BY_FOOTER` | Primary in header, powered-by in footer (default) |
| `POWERED_BY_HEADER` | Primary in header, powered-by as subtitle |
| `DUAL_HEADER` | Both brands side-by-side in header |

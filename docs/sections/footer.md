---
title: Footer
parent: Sections
nav_order: 6
---

# Footer

The footer section displays notes, terms, and free-form custom content at the bottom of the invoice.

```kotlin
footer {
    notes("Thank you for your business! We appreciate your prompt payment.")
    terms("Net 30. A 1.5% monthly interest charge applies to overdue balances.")
}
```

{% include example-preview.html name="section-footer" height="200px" %}

## Inline links and rich content

Each of `notes`, `terms`, and `customContent` accepts a content lambda — embed
links, buttons, dividers, and other inline elements:

```kotlin
footer {
    notes {
        text("Thanks for your business! Read our ")
        link("terms", "https://acme.com/terms")
        text(" or ")
        link("contact support", "mailto:billing@acme.com")
        text(".")
    }
    terms {
        text("Net 30. ")
        link("Late payment policy", "https://acme.com/late-payment")
    }
}
```

Plain-string forms still work for simple cases:

```kotlin
footer {
    notes("Thank you for your business!")
    terms("Net 30")
}
```

## Powered-by Branding

When the header branding uses `BrandLayout.POWERED_BY_FOOTER` (the default), the powered-by brand is displayed at the bottom of the footer section with its logo and tagline. See [Branding & Logos](../styling/branding) for configuration details.

```kotlin
header {
    branding {
        layout = BrandLayout.POWERED_BY_FOOTER
        primary { name("Client Company") }
        poweredBy {
            name("Platform")
            logo(platformLogoBytes, "image/png")
            tagline("Professional Invoicing")
        }
    }
}
footer {
    notes("Thank you for your business!")
    terms("Net 30")
}
```

## Builder Reference

### FooterBuilder

| Method | Description |
|--------|-------------|
| `notes(value)` | Thank-you message or general notes (plain string) |
| `notes { ... }` | Rich notes — `text()`, `link()`, `button()`, etc. |
| `terms(value)` | Payment terms and conditions (plain string) |
| `terms { ... }` | Rich terms |
| `customContent(value)` | Free-form content rendered below notes/terms (plain string) |
| `customContent { ... }` | Rich custom content |

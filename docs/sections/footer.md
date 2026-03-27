---
title: Footer
parent: Sections
nav_order: 6
---

# Footer

The footer section displays notes and terms at the bottom of the invoice.

```kotlin
footer {
    notes("Thank you for your business! We appreciate your prompt payment.")
    terms("Net 30. A 1.5% monthly interest charge applies to overdue balances.")
}
```

{% include example-preview.html name="section-footer" height="200px" %}

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
| `notes(value)` | Thank-you message or general notes |
| `terms(value)` | Payment terms and conditions |

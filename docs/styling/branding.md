---
title: Branding
parent: Styling
nav_order: 3
---

# Branding & Logos

Configure company branding, logos, and dual-brand layouts.

## Logo

Provide a logo as a `ByteArray` with its MIME type:

```kotlin
header {
    branding {
        primary {
            name("Acme Corp")
            logo(logoBytes, "image/png")  // PNG, JPEG, etc.
            address("123 Main St", "Springfield, IL 62701")
            email("billing@acme.com")
        }
    }
}
```

## Dual Branding

Show a secondary brand alongside the primary:

```kotlin
header {
    branding {
        layout = BrandLayout.DUAL_HEADER
        primary {
            name("My Company")
            logo(myLogoBytes, "image/png")
        }
        poweredBy {
            name("Powered by Platform")
            tagline("Professional Invoicing")
        }
    }
}
```

{% include example-preview.html name="header-dual-branding" height="500px" %}

## Brand Layout Options

| Layout | Description |
|--------|-------------|
| `POWERED_BY_FOOTER` | Primary in header, powered-by in footer (default) |
| `POWERED_BY_HEADER` | Primary in header, powered-by as header subtitle |
| `DUAL_HEADER` | Both brands side-by-side in header |

## BrandIdentityBuilder Reference

| Method | Description |
|--------|-------------|
| `name(value)` | Brand name (required) |
| `logo(data, contentType)` | Logo from bytes + MIME type |
| `logo(source)` | Logo from `ImageSource` |
| `address(vararg lines)` | Address lines |
| `email(value)` | Contact email |
| `phone(value)` | Contact phone |
| `website(value)` | Website URL |
| `tagline(value)` | Subtitle or tagline |

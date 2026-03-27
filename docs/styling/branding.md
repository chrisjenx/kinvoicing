---
title: Branding
parent: Styling
nav_order: 3
---

# Branding & Logos

Configure company branding, logos, and dual-brand layouts.

## Logo from Bytes

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

{% include example-preview.html name="branding-logo" height="500px" %}

## Logo from CMP Resources

If you use Compose Multiplatform resources, the `render-compose` module provides a `logo(DrawableResource)` extension that wraps the resource in a `DrawableImageSource`. This renders efficiently in Compose and lazy-loads bytes for non-Compose renderers (PDF, HTML).

```kotlin
import com.chrisjenx.kinvoicing.compose.logo  // extension from render-compose

header {
    branding {
        primary {
            name("Acme Corp")
            logo(Res.drawable.company_logo)  // CMP DrawableResource
            address("123 Main St", "Springfield, IL 62701")
        }
    }
}
```

{: .note }
The `logo(DrawableResource)` extension requires the `render-compose` dependency.

## Stacked Layout with Logo

Combine a centered logo with a stacked header layout:

```kotlin
style {
    headerLayout = HeaderLayout.STACKED
    logoPlacement = LogoPlacement.CENTER
}
header {
    branding {
        primary {
            name("Acme Corp")
            logo(logoBytes, "image/png")
            address("123 Main St", "Springfield, IL 62701")
        }
    }
    invoiceNumber("INV-2026-0001")
    issueDate(LocalDate(2026, 3, 1))
    dueDate(LocalDate(2026, 3, 31))
}
```

{% include example-preview.html name="branding-stacked-logo" height="500px" %}

## Dual Branding

Show a secondary brand alongside the primary. Both can include logos:

```kotlin
header {
    branding {
        layout = BrandLayout.DUAL_HEADER
        primary {
            name("Client Company")
            logo(clientLogoBytes, "image/png")
            email("billing@client.com")
        }
        poweredBy {
            name("Powered by Platform")
            logo(platformLogoBytes, "image/png")
            tagline("Professional Invoicing")
        }
    }
}
```

{% include example-preview.html name="branding-dual-logo" height="500px" %}

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
| `logo(resource)` | Logo from CMP `DrawableResource` (render-compose) |
| `address(vararg lines)` | Address lines |
| `email(value)` | Contact email |
| `phone(value)` | Contact phone |
| `website(value)` | Website URL |
| `tagline(value)` | Subtitle or tagline |

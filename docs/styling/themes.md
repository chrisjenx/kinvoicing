---
title: Themes
parent: Styling
nav_order: 1
---

# Themes

Kinvoicing ships with 8 built-in themes. Apply a theme with the `style { }` block:

```kotlin
val doc = invoice {
    style { theme(InvoiceThemes.Modern) }
    // ... sections
}
```

Override individual properties after applying a theme:

```kotlin
style {
    theme(InvoiceThemes.Elegant)
    accentBorder = true
    showGridLines = true
}
```

## Theme Gallery

### Classic (default)

Clean blue on white. The default when no theme is specified.

{% include example-preview.html name="basic" height="600px" %}

### Corporate

Conservative navy with an accent stripe. Professional and trustworthy.

{% include example-preview.html name="theme-corporate" height="600px" %}

### Modern

Indigo tones, airy spacing, minimal decoration.

{% include example-preview.html name="theme-modern" height="600px" %}

### Bold

Strong blue, grid lines, stacked header. Structured and authoritative.

{% include example-preview.html name="theme-bold" height="600px" %}

### Warm

Amber earth tones, Georgia font. Friendly and approachable.

{% include example-preview.html name="theme-warm" height="600px" %}

### Minimal

Near-monochrome, ultra-clean. Lets the content speak.

{% include example-preview.html name="theme-minimal" height="600px" %}

### Elegant

Dark stone and gold accent, Georgia font. Luxurious and refined.

{% include example-preview.html name="theme-elegant" height="600px" %}

### Fresh

Green and teal. Clean, eco-feeling, optimistic.

{% include example-preview.html name="theme-fresh" height="600px" %}

---
title: Custom Styles
parent: Styling
nav_order: 2
---

# Custom Styles

Fine-tune every visual aspect of the invoice with the `style { }` block.

```kotlin
style {
    primaryColor = 0xFF1D4ED8       // Blue
    secondaryColor = 0xFF64748B     // Slate
    textColor = 0xFF1E293B          // Dark
    backgroundColor = 0xFFFFFFFF    // White
    fontFamily = "Helvetica"
    headerLayout = HeaderLayout.STACKED
    logoPlacement = LogoPlacement.CENTER
    showGridLines = true
    accentBorder = true
}
```

{% include example-preview.html name="style-grid-accent" height="500px" %}

## Style Properties

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

## Material 3 Integration

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

This maps your Material color scheme (primary, onSurface, error, etc.) to invoice style properties automatically.

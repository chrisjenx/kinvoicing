---
title: Material 3
parent: Styling
nav_order: 2
---

# Material 3 Integration

If your app uses Material 3 theming, invoices can automatically match your color scheme. The `render-compose` module provides `InvoiceStyle.fromMaterialTheme()` which derives all invoice style colors from the current `MaterialTheme.colorScheme`.

{: .note }
Requires the `render-compose` dependency.

## Basic Usage

```kotlin
@Composable
fun InvoiceScreen(doc: InvoiceDocument) {
    val style = InvoiceStyle.fromMaterialTheme()
    InvoiceView(doc.copy(style = style))
}
```

## With Overrides

Apply Material3 colors as the base, then customize individual properties:

```kotlin
@Composable
fun StyledInvoice(doc: InvoiceDocument) {
    val style = InvoiceStyle.fromMaterialTheme {
        copy(
            accentBorder = true,
            showGridLines = true,
            headerLayout = HeaderLayout.STACKED,
        )
    }
    InvoiceView(doc.copy(style = style))
}
```

## Dark Mode

The same code works in both light and dark themes — `fromMaterialTheme()` reads whatever `MaterialTheme.colorScheme` is currently provided:

```kotlin
@Composable
fun App() {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        val style = InvoiceStyle.fromMaterialTheme()
        InvoiceView(doc.copy(style = style))
    }
}
```

## Color Mapping

`fromMaterialTheme()` maps Material 3 color roles to invoice style properties:

| Material 3 Role | InvoiceStyle Property | Used For |
|-----|-----|-----|
| `primary` | `primaryColor` | Headings, totals, accent elements |
| `onSurfaceVariant` | `secondaryColor` | Labels, secondary text |
| `onSurface` | `textColor` | Body text |
| `surface` | `backgroundColor` | Page background |
| `error` | `negativeColor` | Discounts, credits, negative amounts |
| `outlineVariant` | `borderColor` | Table borders, grid lines |
| `outlineVariant` (50% alpha) | `dividerColor` | Subtle row dividers |
| `surfaceContainerLow` | `mutedBackgroundColor` | Alternating rows, meta blocks |
| `surfaceVariant` | `surfaceColor` | Card-like section backgrounds |

## Dynamic Colors (Material You)

On Android 12+, Material You derives colors from the user's wallpaper via `dynamicColorScheme`. Since `fromMaterialTheme()` reads whatever `MaterialTheme.colorScheme` is currently provided, it works automatically with dynamic colors — no extra configuration needed.

```kotlin
@Composable
fun DynamicInvoice(doc: InvoiceDocument) {
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicLightColorScheme(LocalContext.current)
    } else {
        lightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme) {
        val style = InvoiceStyle.fromMaterialTheme()
        InvoiceView(doc.copy(style = style))
    }
}
```

{: .note }
Dynamic colors are only available on Android 12 (API 31) and above. On older versions or non-Android platforms, fall back to a static `colorScheme` or use one of the [built-in themes](themes).

## Example Color Schemes

The following examples show invoices styled with colors that match common Material 3 palettes.

### Purple (M3 Default)

{% include example-preview.html name="m3-purple" height="500px" %}

### Blue

{% include example-preview.html name="m3-blue" height="500px" %}

### Green / Teal

{% include example-preview.html name="m3-green" height="500px" %}

---
title: Status
parent: Sections
nav_order: 9
---

# Status

Display the invoice's state visually. Kinvoicing provides 7 predefined statuses with default colors, plus a `Custom` variant for your own.

| Status | Color | Use case |
|--------|-------|----------|
| `Draft` | Gray | Invoice being prepared |
| `Sent` | Blue | Sent to recipient |
| `Paid` | Green | Payment received |
| `Overdue` | Red | Past due |
| `Void` | Light gray | Cancelled |
| `Uncollectable` | Stone | Written off |
| `Refunded` | Violet | Payment returned |
| `Custom(label, color)` | Any | Your own states |

## Display Modes

Each status can be rendered in one of five display modes:

| Mode | Description |
|------|-------------|
| `Badge` (default) | Small colored pill next to invoice number |
| `Banner` | Full-width colored bar at top |
| `Watermark` | Large diagonal text overlaying the body |
| `Stamp` | Rotated stamp/seal below the header |
| `None` | Status stored as data, not rendered |

## Badge

The default — a small colored pill in the header:

```kotlin
invoice {
    status(InvoiceStatus.Paid)
    // ... sections
}
```

{% include example-preview.html name="status-badge" height="400px" %}

## Banner

Full-width bar at the top of the invoice:

```kotlin
invoice {
    status {
        overdue()
        banner()
    }
    // ... sections
}
```

{% include example-preview.html name="status-banner" height="400px" %}

## Watermark

Diagonal text overlaid across the invoice body:

```kotlin
invoice {
    status {
        voided()
        watermark()          // default opacity = 0.15
        // watermark(0.25f)  // or custom opacity
    }
    // ... sections
}
```

{% include example-preview.html name="status-watermark" height="400px" %}

## Stamp

Rotated stamp/seal positioned below the header:

```kotlin
invoice {
    status {
        draft()
        stamp()          // default opacity = 0.35
        // stamp(0.5f)   // or custom opacity
    }
    // ... sections
}
```

{% include example-preview.html name="status-stamp" height="400px" %}

## Custom Status

Define your own status with any label and ARGB color:

```kotlin
invoice {
    status {
        custom("PENDING APPROVAL", 0xFFF59E0B)
        banner()
    }
    // ... sections
}
```

{% include example-preview.html name="status-custom" height="400px" %}

## Builder Reference

### StatusBuilder

| Method | Description |
|--------|-------------|
| `draft()` | Set status to Draft (gray) |
| `sent()` | Set status to Sent (blue) |
| `paid()` | Set status to Paid (green) |
| `overdue()` | Set status to Overdue (red) |
| `voided()` | Set status to Void (gray) |
| `uncollectable()` | Set status to Uncollectable (stone) |
| `refunded()` | Set status to Refunded (violet) |
| `custom(label, color)` | Set a custom status with ARGB color |
| `badge()` | Display as header pill (default) |
| `banner()` | Display as full-width bar |
| `watermark(opacity)` | Display as diagonal text overlay |
| `stamp(opacity)` | Display as rotated stamp |
| `hidden()` | Store status without rendering |

## Cross-Renderer Support

| Display | Compose | PDF | HTML | Email HTML |
|---------|---------|-----|------|------------|
| Badge | Pill in header | Pill in header | Pill in header | Inline span in header |
| Banner | Full-width bar | Full-width bar | Full-width bar | Full-width div |
| Watermark | Canvas overlay | SVG overlay | SVG overlay | Background SVG image |
| Stamp | Canvas overlay | SVG overlay | SVG overlay | Background SVG image |

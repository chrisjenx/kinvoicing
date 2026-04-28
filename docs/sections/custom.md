---
title: Custom Sections
parent: Sections
nav_order: 8
---

# Custom Sections

Custom sections let you build freeform content from element primitives. Each custom section has a `key` for identification.

```kotlin
custom("terms-and-conditions") {
    text("Terms & Conditions", styleRef = "bold")
    divider()
    text("Payment is due within 30 days of the invoice date.")
    spacer()
    row(1f, 1f) {
        text("Questions? Contact billing@acme.com")
        text("Ref: T&C v2.1 (2026)")
    }
    spacer(8)
    link("View full terms", "https://acme.com/terms")
}
```

{% include example-preview.html name="section-custom-elements" height="300px" %}

## Element Types

### Text

```kotlin
text("Hello, world!")
text("Bold heading", styleRef = "bold")
```

### Divider

```kotlin
divider()  // Horizontal line
```

### Spacer

```kotlin
spacer()       // Default 16px spacing
spacer(8)      // Custom height
```

### Row (columns)

```kotlin
row(1f, 1f) {       // Two equal columns
    text("Left")
    text("Right")
}
row(2f, 1f) {       // 2:1 ratio columns
    text("Wider column")
    text("Narrow")
}
```

### Link (inline text)

```kotlin
link("View terms", "https://example.com/terms")
```

Renders as a primary-colored Material 3 `labelLarge` inline link. Clickable in
PDF and HTML email.

### Button (styled CTA)

```kotlin
button("Pay Now", "https://pay.example.com/inv-001")
```

Renders as an M3 `Button`-equivalent — primary container, white label, 20dp pill
corners. In email HTML this becomes a bulletproof `<table>`-wrapped button. In
PDF the bounding rectangle becomes a clickable annotation.

`link()` and `button()` are also available inside `paymentInfo { notes { ... } }`
and `footer { notes { ... } }` / `terms { ... }` / `customContent { ... }`.

### Image

```kotlin
image(pngBytes, "image/png", width = 200, height = 100)
```

## Builder Reference

### CustomBuilder

`CustomBuilder` extends `ContentBuilder`, so the same `link()`/`button()`/`text()`/etc.
surface is available wherever rich content is accepted (Custom sections,
`paymentInfo { notes { ... } }`, `footer { notes/terms/customContent { ... } }`).

| Method | Description |
|--------|-------------|
| `text(value, styleRef?)` | Text element with optional style reference |
| `divider()` | Horizontal divider line |
| `spacer(height = 16)` | Vertical whitespace |
| `row(vararg weights) { }` | Horizontal layout with column weights |
| `link(text, href)` | Inline TEXT-style hyperlink (primary color, M3 labelLarge) |
| `button(text, href)` | BUTTON-style CTA (filled primary container, white label) |
| `image(data, contentType, width?, height?)` | Inline image from bytes |
| `image(source, width?, height?)` | Inline image from `ImageSource` |

---
title: Payment Info
parent: Sections
nav_order: 5
---

# Payment Info

The payment info section displays bank details, a "Pay Now" call-to-action, and additional notes.

```kotlin
paymentInfo {
    bankName("First National Bank")
    accountNumber("9876543210")
    routingNumber("021000021")
    paymentLink("https://pay.example.com/inv-001")
    notes("Wire transfers preferred for amounts over $1,000.")
}
```

{% include example-preview.html name="section-payment-info" height="250px" %}

## Pay Now: text link or styled button

The pay link can render two ways across all renderers (Compose, PDF, Email HTML).
Choose per invoice.

### Text link (default — markdown-style `[Text](url)`)

```kotlin
paymentInfo {
    paymentLink("https://pay.example.com/inv-001")              // label defaults to "Pay Now"
    paymentLink("Pay this invoice", "https://pay.example.com")  // custom label, TEXT style
}
```

Renders as a primary-colored Material 3 `labelLarge` inline link.

### Styled CTA button

```kotlin
paymentInfo {
    paymentButton("Pay $1,000 Now", "https://pay.example.com/inv-001")
    // or, equivalently:
    paymentLink("Pay $1,000 Now", "https://pay.example.com/inv-001", LinkStyle.BUTTON)
}
```

Renders as an M3 `Button`-equivalent — primary container, white label, 20dp pill
corners, 24dp/10dp padding. In email HTML this becomes a bulletproof
`<table>`-wrapped button (Outlook-friendly). In PDF this remains clickable via
compose2pdf's `PdfLink`.

## Inline links inside notes

`notes` accepts a content lambda — embed text and links anywhere:

```kotlin
paymentInfo {
    bankName("First National")
    paymentLink("https://pay.example.com/inv-001")
    notes {
        text("Wire transfer alternative — see ")
        link("transfer policy", "https://example.com/wire-policy")
    }
}
```

The plain-string form still works:

```kotlin
paymentInfo {
    notes("Wire transfers preferred for amounts over $1,000.")
}
```

## Builder Reference

### PaymentInfoBuilder

| Method | Description |
|--------|-------------|
| `bankName(value)` | Bank or financial institution name |
| `accountNumber(value)` | Account number |
| `routingNumber(value)` | Routing/sort code |
| `paymentLink(href)` | Pay link with default label "Pay Now", TEXT style |
| `paymentLink(text, href, style = LinkStyle.TEXT)` | Pay link with custom label and explicit style |
| `paymentButton(text, href)` | Convenience — equivalent to `paymentLink(text, href, LinkStyle.BUTTON)` |
| `qrCodeData(value)` | QR code payload (renderer-dependent) |
| `notes(value)` | Additional payment instructions (plain string) |
| `notes { ... }` | Rich notes — supports `text()`, `link()`, `button()`, `spacer()`, `divider()`, `image()`, `row()` |

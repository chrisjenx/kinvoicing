---
title: Payment Info
parent: Sections
nav_order: 5
---

# Payment Info

The payment info section displays bank details, payment links, and additional notes.

```kotlin
paymentInfo {
    bankName("First National Bank")
    accountNumber("9876543210")
    routingNumber("021000021")
    paymentLink("https://pay.example.com/inv-001")
    notes("Wire transfers preferred for amounts over $1,000.")
}
```

{% include example-preview.html name="payment-info" height="550px" %}

## Builder Reference

### PaymentInfoBuilder

| Method | Description |
|--------|-------------|
| `bankName(value)` | Bank or financial institution name |
| `accountNumber(value)` | Account number |
| `routingNumber(value)` | Routing/sort code |
| `paymentLink(value)` | Online payment URL |
| `qrCodeData(value)` | QR code payload (renderer-dependent) |
| `notes(value)` | Additional payment instructions |

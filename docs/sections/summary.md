---
title: Summary
parent: Sections
nav_order: 4
---

# Summary

The summary section shows the subtotal, any adjustments (tax, discounts, fees, credits), and the total.

## With All Adjustment Types

```kotlin
summary {
    currency("USD")
    discount("Early Payment (10%)", percent = 10.0)
    tax("Sales Tax (8.25%)", percent = 8.25)
    fee("Processing Fee", fixed = 25.0)
    credit("Referral Credit", 100.0)
}
```

{% include example-preview.html name="section-summary" height="350px" %}

## Builder Reference

### SummaryBuilder

| Method | Description |
|--------|-------------|
| `currency(code)` | 3-letter ISO currency code (e.g., "USD") |
| `discount(label, percent?, fixed?)` | Percentage or fixed discount |
| `tax(label, percent?, fixed?)` | Percentage or fixed tax |
| `fee(label, percent?, fixed?)` | Percentage or fixed fee |
| `credit(label, amount)` | Fixed credit amount |

{: .note }
Adjustments are applied in the order they are declared in the DSL.

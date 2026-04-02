---
title: Footer
parent: Sections
nav_order: 6
---

# Footer

The footer section displays notes and terms at the bottom of the invoice.

```kotlin
footer {
    notes("Thank you for your business! We appreciate your prompt payment.")
    terms("Net 30. A 1.5% monthly interest charge applies to overdue balances.")
}
```

{% include example-preview.html name="footer" height="450px" %}

## Builder Reference

### FooterBuilder

| Method | Description |
|--------|-------------|
| `notes(value)` | Thank-you message or general notes |
| `terms(value)` | Payment terms and conditions |

---
title: BillFrom / BillTo
parent: Sections
nav_order: 2
---

# BillFrom / BillTo

The party sections identify the sender and recipient. When both are present, they render side-by-side.

```kotlin
billFrom {
    name("Acme Corp")
    address("123 Main St", "Springfield, IL 62701")
    email("billing@acme.com")
    phone("+1 (555) 100-0001")
}
billTo {
    name("Jane Smith")
    address("456 Oak Ave", "Boulder, CO 80301")
    email("jane@example.com")
    phone("+1 (555) 200-0002")
}
```

{% include example-preview.html name="section-bill-parties" height="250px" %}

## Builder Reference

### PartyBuilder

| Method | Description |
|--------|-------------|
| `name(value)` | Party name (required) |
| `address(vararg lines)` | Address lines |
| `email(value)` | Email address |
| `phone(value)` | Phone number |

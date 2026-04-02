---
title: Line Items
parent: Sections
nav_order: 3
---

# Line Items

The line items section lists the charges on the invoice.

## Basic Line Items

```kotlin
lineItems {
    columns("Description", "Qty", "Rate", "Amount")
    item("Web Development", qty = 40, unitPrice = 150.0)
    item("Design Services", qty = 10, unitPrice = 100.0)
    item("Hosting (Monthly)", qty = 1, unitPrice = 49.99)
    item("Domain Registration", qty = 1, unitPrice = 14.99)
}
```

{% include example-preview.html name="line-items-basic" height="450px" %}

## Sub-Items

Nest detail rows under a parent line item:

```kotlin
lineItems {
    columns("Description", "Qty", "Rate", "Amount")
    item("Project Alpha") {
        sub("Design Phase", qty = 20, unitPrice = 150.0)
        sub("Development Phase", qty = 40, unitPrice = 175.0)
        sub("Testing & QA", qty = 10, unitPrice = 125.0)
    }
    item("Project Beta") {
        sub("Requirements Gathering", qty = 8, unitPrice = 200.0)
        sub("Implementation", qty = 24, unitPrice = 175.0)
    }
}
```

{% include example-preview.html name="line-items-sub" height="450px" %}

## Builder Reference

### LineItemsBuilder

| Method | Description |
|--------|-------------|
| `columns(vararg names)` | Set column headers |
| `item(description, qty?, unitPrice?, amount?, metadata?) { }` | Add a line item |

### LineItemBuilder (inside `item { }`)

| Method | Description |
|--------|-------------|
| `sub(description, qty?, unitPrice?, amount?)` | Add a nested sub-item |
| `discount(label, percent?, fixed?)` | Item-level discount |

{: .note }
Each item must specify either `qty` + `unitPrice`, an explicit `amount`, or sub-items. The builder validates this at build time.

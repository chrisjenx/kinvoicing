---
title: Sections
layout: default
nav_order: 3
has_children: true
---

# Invoice Sections

An `InvoiceDocument` is composed of sections. There are 9 sealed section types, and renderers handle each with exhaustive `when` blocks.

| Section | Purpose | Status |
|---------|---------|--------|
| [Header](header) | Branding, invoice number, dates | Recommended |
| [BillFrom / BillTo](parties) | Sender and recipient contact info | Optional |
| [LineItems](line-items) | Itemized charges with quantities and rates | Recommended |
| [Summary](summary) | Subtotal, adjustments, and total | Recommended |
| [PaymentInfo](payment-info) | Bank details, payment links | Optional |
| [Footer](footer) | Notes and terms | Optional |
| [MetaBlock](meta-block) | Key-value metadata (PO numbers, etc.) | Optional |
| [Custom](custom) | Freeform content from element primitives | Optional |

{: .note }
No section is strictly required at the IR level, but a practical invoice typically includes at least a Header, LineItems, and Summary. All other sections are optional and can be added in any order.

For logo configuration and dual-brand layouts, see [Branding & Logos](../styling/branding).

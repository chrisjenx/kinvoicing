---
title: Sections
layout: default
nav_order: 3
has_children: true
---

# Invoice Sections

An `InvoiceDocument` is composed of sections. There are 9 sealed section types, and renderers handle each with exhaustive `when` blocks.

| Section | Purpose |
|---------|---------|
| [Header](header) | Branding, invoice number, dates |
| [BillFrom / BillTo](parties) | Sender and recipient contact info |
| [LineItems](line-items) | Itemized charges with quantities and rates |
| [Summary](summary) | Subtotal, adjustments, and total |
| [PaymentInfo](payment-info) | Bank details, payment links |
| [Footer](footer) | Notes and terms |
| [MetaBlock](meta-block) | Key-value metadata (PO numbers, etc.) |
| [Custom](custom) | Freeform content from element primitives |

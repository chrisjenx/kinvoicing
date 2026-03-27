# Changelog

All notable changes to Kinvoicing will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Sealed IR** with 9 invoice section types: Header, BillFrom, BillTo, LineItems, Summary, PaymentInfo, Footer, Custom, MetaBlock
- **Type-safe DSL** builder: `invoice { header { ... } lineItems { ... } summary { ... } }`
- **Compose renderer** (`render-compose`): `InvoiceView` and `InvoiceContent` composables for interactive preview
- **PDF renderer** (`render-pdf`): `InvoiceDocument.toPdf()` via compose2pdf with automatic multi-page pagination
- **Email HTML renderer** (`render-html-email`): `InvoiceDocument.toHtml()` with inline styles and table layout for email client compatibility
- **Print HTML renderer** (`render-html`): `renderToHtml()` with SVG-based output, embedded fonts, and print styles
- **8 built-in themes**: Classic, Corporate, Modern, Bold, Warm, Minimal, Elegant, Fresh
- **14 style properties**: colors, fonts, logo placement, header layout, grid lines, accent borders
- **Material 3 bridge**: `InvoiceStyle.fromMaterialTheme()` for Compose apps
- **Branding & logos**: `ImageSource` abstraction, dual-branding layouts (POWERED_BY_FOOTER, POWERED_BY_HEADER, DUAL_HEADER)
- **Line item adjustments**: tax, discount, fee, credit (percentage or fixed amount)
- **Sub-items**: nested detail rows under parent line items
- **Payment info section**: bank details, payment links, QR code data, notes
- **MetaBlock**: key-value metadata (PO numbers, project names, etc.)
- **Custom sections**: freeform content from primitives (text, divider, spacer, row, image)
- **Currency formatting**: locale-aware symbol lookup, 3-letter ISO code validation
- **Validation**: required field checks with descriptive error messages
- **Email safety**: validated output (no scripts, iframes, or unsafe elements)
- **Dokka API documentation** generation

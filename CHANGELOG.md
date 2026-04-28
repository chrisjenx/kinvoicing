# Changelog

All notable changes to Kinvoicing will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **`LinkStyle` enum** on `InvoiceElement.Link` — `TEXT` (default, inline primary-colored Material 3 `labelLarge`) or `BUTTON` (styled CTA — filled primary container, white label, 20dp pill, M3 `Button`-equivalent).
- **`button(text, href)`** in the shared `ContentBuilder` DSL, available wherever rich content is accepted: `custom("…") { … }`, `paymentInfo { notes { … } }`, `footer { notes/terms/customContent { … } }`.
- **`link()` callable in any rich-content block.** Previously only `Custom` sections supported `link()`; now `Footer.notes/terms/customContent` and `PaymentInfo.notes` accept the same content lambda.
- **`paymentInfo { paymentLink(text, href, style?) }`** overload — custom display text and explicit `LinkStyle`.
- **`paymentInfo { paymentButton(text, href) }`** convenience — equivalent to `paymentLink(text, href, LinkStyle.BUTTON)`.
- HTML email renders BUTTON-style links as bulletproof `<table>`-wrapped buttons (Outlook-friendly).
- `payButton` test fixture exercising BUTTON style and inline links in notes/footer.
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

### Changed

- **BREAKING (IR):** `InvoiceSection.PaymentInfo.paymentLink: String?` → `InvoiceElement.Link?`. The DSL `paymentLink(href: String)` is unchanged; the underlying field type is the breaking change for code that destructures the IR directly.
- **BREAKING (IR):** `InvoiceSection.PaymentInfo.notes: String?` → `List<InvoiceElement>?`. The DSL `notes("…")` form is unchanged; the field type is the breaking change.
- **BREAKING (IR):** `InvoiceSection.Footer.notes/terms/customContent: String?` → `List<InvoiceElement>?`. DSL string-form setters are unchanged.
- **BREAKING (DSL minor):** `CustomBuilder.row(vararg weights: Float, init: ContentBuilder.() -> Unit)` — receiver narrowed from `CustomBuilder` to `ContentBuilder`. Inside a `row { }` block you now have only content primitives; you cannot reference the section `key`. No bundled fixture used the old broader receiver.
- Pay link rendering no longer hard-prefixes `"Pay Online: "` — the rendered text is the link's `text` field (default `"Pay Now"` when omitted).

### Removed

- `PdfRendererTest.paymentLinkAnnotationPresent` — misleading text-substring check; real PDF annotation coverage lives in `fidelity-test`'s `LinkAndImageTest`.

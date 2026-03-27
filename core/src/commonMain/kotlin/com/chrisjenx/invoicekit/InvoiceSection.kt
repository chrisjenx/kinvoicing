package com.chrisjenx.invoicekit

import kotlinx.datetime.LocalDate

/**
 * Sealed hierarchy of invoice sections. Renderers exhaustively match on these —
 * the compiler enforces completeness when new variants are added.
 */
public sealed class InvoiceSection {

    /** Invoice header with branding, invoice number, and dates. */
    public data class Header(
        val branding: Branding? = null,
        val invoiceNumber: String? = null,
        val issueDate: LocalDate? = null,
        val dueDate: LocalDate? = null,
    ) : InvoiceSection()

    /** Sender/issuer contact details. */
    public data class BillFrom(
        val name: String,
        val address: List<String> = emptyList(),
        val email: String? = null,
        val phone: String? = null,
    ) : InvoiceSection()

    /** Recipient/customer contact details. */
    public data class BillTo(
        val name: String,
        val address: List<String> = emptyList(),
        val email: String? = null,
        val phone: String? = null,
    ) : InvoiceSection()

    /** Table of billable line items with configurable column headers. */
    public data class LineItems(
        val columnHeaders: List<String>,
        val rows: List<LineItem>,
    ) : InvoiceSection()

    /** Financial summary with subtotal, adjustments (taxes, discounts, fees), and total. */
    public data class Summary(
        val subtotal: Double,
        val adjustments: List<Adjustment>,
        val total: Double,
        val currency: String = "USD",
    ) : InvoiceSection()

    /** Bank/payment details for the recipient to remit payment. */
    public data class PaymentInfo(
        val bankName: String? = null,
        val accountNumber: String? = null,
        val routingNumber: String? = null,
        val paymentLink: String? = null,
        /** Raw data to encode as a QR code (e.g., a payment URL). */
        val qrCodeData: String? = null,
        val notes: String? = null,
    ) : InvoiceSection()

    /** Closing notes, terms and conditions, or free-form content. */
    public data class Footer(
        val notes: String? = null,
        val terms: String? = null,
        val customContent: String? = null,
    ) : InvoiceSection()

    /**
     * Renderer-agnostic custom section built from [InvoiceElement] primitives.
     * @property key Unique identifier used by renderers to match custom templates.
     */
    public data class Custom(
        val key: String,
        val content: List<InvoiceElement>,
    ) : InvoiceSection()

    /** Block of key-value metadata (e.g., PO number, project name). */
    public data class MetaBlock(
        val entries: List<MetaEntry>,
    ) : InvoiceSection()
}

package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.*
import kotlinx.datetime.LocalDate

/** DSL builder for the [InvoiceSection.Header] section. */
@InvoiceDsl
public class HeaderBuilder {
    private var branding: Branding? = null
    private var invoiceNumber: String? = null
    private var issueDate: LocalDate? = null
    private var dueDate: LocalDate? = null

    /** Configure branding (logo, company name, etc.) for the header. */
    public fun branding(init: BrandingBuilder.() -> Unit) {
        branding = BrandingBuilder().apply(init).build()
    }

    /** Set the invoice number (e.g., "INV-2026-0001"). */
    public fun invoiceNumber(value: String) {
        invoiceNumber = value
    }

    /** Set the invoice issue date. */
    public fun issueDate(value: LocalDate) {
        issueDate = value
    }

    /** Set the payment due date. */
    public fun dueDate(value: LocalDate) {
        dueDate = value
    }

    internal fun build(): InvoiceSection.Header = InvoiceSection.Header(
        branding = branding,
        invoiceNumber = invoiceNumber,
        issueDate = issueDate,
        dueDate = dueDate,
    )
}

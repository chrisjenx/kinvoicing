package com.chrisjenx.invoicekit.builders

import com.chrisjenx.invoicekit.InvoiceDsl
import com.chrisjenx.invoicekit.InvoiceSection

/** DSL builder for the [InvoiceSection.Footer] section. */
@InvoiceDsl
public class FooterBuilder {
    private var notes: String? = null
    private var terms: String? = null
    private var customContent: String? = null

    /** Set closing notes (e.g., "Thank you for your business!"). */
    public fun notes(value: String) { notes = value }
    /** Set payment terms (e.g., "Net 30"). */
    public fun terms(value: String) { terms = value }
    /** Set arbitrary custom content rendered below notes and terms. */
    public fun customContent(value: String) { customContent = value }

    internal fun build(): InvoiceSection.Footer = InvoiceSection.Footer(
        notes = notes,
        terms = terms,
        customContent = customContent,
    )
}

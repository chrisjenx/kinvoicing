package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.InvoiceDsl
import com.chrisjenx.kinvoicing.InvoiceElement
import com.chrisjenx.kinvoicing.InvoiceSection

/** DSL builder for the [InvoiceSection.Footer] section. */
@InvoiceDsl
public class FooterBuilder {
    private var notes: List<InvoiceElement>? = null
    private var terms: List<InvoiceElement>? = null
    private var customContent: List<InvoiceElement>? = null

    /** Set closing notes (e.g., "Thank you for your business!"). */
    public fun notes(value: String) { notes = listOf(InvoiceElement.Text(value)) }
    /** Set payment terms (e.g., "Net 30"). */
    public fun terms(value: String) { terms = listOf(InvoiceElement.Text(value)) }
    /** Set arbitrary custom content rendered below notes and terms. */
    public fun customContent(value: String) { customContent = listOf(InvoiceElement.Text(value)) }

    internal fun build(): InvoiceSection.Footer = InvoiceSection.Footer(
        notes = notes,
        terms = terms,
        customContent = customContent,
    )
}

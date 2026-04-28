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

    /** Set closing notes from a single plain-text string. */
    public fun notes(value: String) { notes = listOf(InvoiceElement.Text(value)) }

    /** Set rich closing notes — supports text(), link(), button(), etc. */
    public fun notes(init: ContentBuilder.() -> Unit) {
        notes = ContentBuilder().apply(init).build()
    }

    /** Set payment terms from a single plain-text string. */
    public fun terms(value: String) { terms = listOf(InvoiceElement.Text(value)) }

    /** Set rich payment terms — supports text(), link(), button(), etc. */
    public fun terms(init: ContentBuilder.() -> Unit) {
        terms = ContentBuilder().apply(init).build()
    }

    /** Set arbitrary custom content from a single plain-text string. */
    public fun customContent(value: String) { customContent = listOf(InvoiceElement.Text(value)) }

    /** Set rich custom content — supports text(), link(), button(), etc. */
    public fun customContent(init: ContentBuilder.() -> Unit) {
        customContent = ContentBuilder().apply(init).build()
    }

    internal fun build(): InvoiceSection.Footer = InvoiceSection.Footer(
        notes = notes,
        terms = terms,
        customContent = customContent,
    )
}

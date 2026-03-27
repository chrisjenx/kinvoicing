package com.chrisjenx.invoicekit

/**
 * The root invoice document. Contains an ordered list of sections and a style configuration.
 */
public data class InvoiceDocument(
    val sections: List<InvoiceSection>,
    val style: InvoiceStyle = InvoiceStyle(),
)

/**
 * The currency code from the [Summary][InvoiceSection.Summary] section, or "USD" if none.
 */
public val InvoiceDocument.currency: String
    get() = (sections.firstOrNull { it is InvoiceSection.Summary } as? InvoiceSection.Summary)
        ?.currency ?: "USD"

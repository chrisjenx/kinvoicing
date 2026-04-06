package com.chrisjenx.kinvoicing

/**
 * The root invoice document. Contains an ordered list of sections and a style configuration.
 */
public data class InvoiceDocument(
    val sections: List<InvoiceSection>,
    val style: InvoiceStyle = InvoiceStyle(),
    val currency: String = "USD",
    val status: InvoiceStatus? = null,
    val statusDisplay: StatusDisplay = StatusDisplay.Badge,
)

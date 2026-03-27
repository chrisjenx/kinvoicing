package com.chrisjenx.kinvoicing

/** A single key-value pair in a [InvoiceSection.MetaBlock] (e.g., "PO Number" to "PO-123"). */
public data class MetaEntry(
    val label: String,
    val value: String,
)

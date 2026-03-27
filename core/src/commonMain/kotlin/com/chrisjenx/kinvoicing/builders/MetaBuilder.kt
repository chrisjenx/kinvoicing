package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.InvoiceDsl
import com.chrisjenx.kinvoicing.MetaEntry

/** DSL builder for [InvoiceSection.MetaBlock] key-value entries. */
@InvoiceDsl
public class MetaBuilder {
    private val entries: MutableList<MetaEntry> = mutableListOf()

    /** Add a metadata entry with the given [label] and [value]. */
    public fun entry(label: String, value: String) {
        entries.add(MetaEntry(label, value))
    }

    internal fun build(): List<MetaEntry> = entries.toList()
}

package com.chrisjenx.kinvoicing

import com.chrisjenx.kinvoicing.builders.InvoiceBuilder

/** Marker annotation for the invoice builder DSL. Prevents scope leaking across nested lambdas. */
@DslMarker
public annotation class InvoiceDsl

/**
 * Top-level entry point for building an [InvoiceDocument] using the type-safe DSL.
 *
 * ```kotlin
 * val doc = invoice {
 *     header { invoiceNumber("INV-001") }
 *     billTo { name("Jane Smith") }
 *     lineItems {
 *         columns("Description", "Qty", "Rate", "Amount")
 *         item("Consulting", qty = 10, unitPrice = 150.0)
 *     }
 *     summary { currency("USD") }
 * }
 * ```
 */
public fun invoice(init: InvoiceBuilder.() -> Unit): InvoiceDocument {
    return InvoiceBuilder().apply(init).build()
}

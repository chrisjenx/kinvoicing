package com.chrisjenx.invoicekit.builders

import com.chrisjenx.invoicekit.*

/**
 * DSL builder for [InvoiceSection.Custom] sections composed of [InvoiceElement] primitives.
 *
 * ```kotlin
 * custom("terms-banner") {
 *     text("Special terms apply", styleRef = "bold")
 *     divider()
 *     row(1f, 1f) {
 *         text("Left column")
 *         text("Right column")
 *     }
 * }
 * ```
 */
@InvoiceDsl
public class CustomBuilder(private val key: String) {
    private val elements: MutableList<InvoiceElement> = mutableListOf()

    /** Add a text element, optionally referencing a renderer-defined style via [styleRef]. */
    public fun text(value: String, styleRef: String? = null) {
        elements.add(InvoiceElement.Text(value, styleRef))
    }

    /** Add vertical whitespace of the given [height]. */
    public fun spacer(height: Int = 16) {
        elements.add(InvoiceElement.Spacer(height))
    }

    /** Add a horizontal divider line. */
    public fun divider() {
        elements.add(InvoiceElement.Divider)
    }

    /** Add a horizontal row of child elements. [weights] controls relative column widths. */
    public fun row(vararg weights: Float, init: CustomBuilder.() -> Unit) {
        val children = CustomBuilder(key).apply(init).elements.toList()
        elements.add(InvoiceElement.Row(children, weights.toList()))
    }

    internal fun build(): InvoiceSection.Custom = InvoiceSection.Custom(
        key = key,
        content = elements.toList(),
    )
}

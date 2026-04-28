package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.ImageSource
import com.chrisjenx.kinvoicing.InvoiceDsl
import com.chrisjenx.kinvoicing.InvoiceElement
import com.chrisjenx.kinvoicing.LinkStyle
import com.chrisjenx.kinvoicing.util.requireSafeUrl

/**
 * Shared inline-content DSL used by every block that holds a list of [InvoiceElement]s
 * (Custom sections, Footer notes/terms/customContent, PaymentInfo notes).
 *
 * ```kotlin
 * notes {
 *     text("Read our ")
 *     link("terms", "https://acme.com/terms")
 *     text(" or ")
 *     button("Pay Now", "https://pay.acme.com")
 * }
 * ```
 */
@InvoiceDsl
public open class ContentBuilder {
    private val elements: MutableList<InvoiceElement> = mutableListOf()

    /** Add a text element, optionally referencing a renderer-defined style via [styleRef]. */
    public fun text(value: String, styleRef: String? = null) {
        elements.add(InvoiceElement.Text(value, styleRef))
    }

    /** Add an inline TEXT-style hyperlink. Renders as primary-colored M3 labelLarge text. */
    public fun link(text: String, href: String) {
        elements.add(InvoiceElement.Link(text, requireSafeUrl(href, "href"), LinkStyle.TEXT))
    }

    /** Add a BUTTON-style CTA. Renders as M3 FilledButton-equivalent with primary container. */
    public fun button(text: String, href: String) {
        elements.add(InvoiceElement.Link(text, requireSafeUrl(href, "href"), LinkStyle.BUTTON))
    }

    /** Add vertical whitespace of the given [height]. */
    public fun spacer(height: Int = 16) {
        elements.add(InvoiceElement.Spacer(height))
    }

    /** Add a horizontal divider line. */
    public fun divider() {
        elements.add(InvoiceElement.Divider)
    }

    /** Add an inline image from raw bytes. */
    public fun image(
        data: ByteArray,
        contentType: String = "image/png",
        width: Int? = null,
        height: Int? = null,
    ) {
        elements.add(InvoiceElement.Image(ImageSource.Bytes(data, contentType), width, height))
    }

    /** Add an inline image from an [ImageSource]. */
    public fun image(
        source: ImageSource,
        width: Int? = null,
        height: Int? = null,
    ) {
        elements.add(InvoiceElement.Image(source, width, height))
    }

    /** Add a horizontal row of child elements. [weights] controls relative column widths. */
    public fun row(vararg weights: Float, init: ContentBuilder.() -> Unit) {
        val children = ContentBuilder().apply(init).build()
        elements.add(InvoiceElement.Row(children, weights.toList()))
    }

    internal fun build(): List<InvoiceElement> = elements.toList()
}

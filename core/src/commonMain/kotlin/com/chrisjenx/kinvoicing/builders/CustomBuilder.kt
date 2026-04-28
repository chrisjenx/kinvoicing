package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.ImageSource
import com.chrisjenx.kinvoicing.InvoiceDsl
import com.chrisjenx.kinvoicing.InvoiceSection

/**
 * DSL builder for [InvoiceSection.Custom] sections composed of inline content elements.
 *
 * ```kotlin
 * custom("terms-banner") {
 *     text("Special terms apply", styleRef = "bold")
 *     divider()
 *     link("Read more", "https://example.com/terms")
 * }
 * ```
 */
@InvoiceDsl
public class CustomBuilder(private val key: String) {
    private val content: ContentBuilder = ContentBuilder()

    /** Add a text element, optionally referencing a renderer-defined style via [styleRef]. */
    public fun text(value: String, styleRef: String? = null): Unit = content.text(value, styleRef)

    /** Add vertical whitespace of the given [height]. */
    public fun spacer(height: Int = 16): Unit = content.spacer(height)

    /** Add a horizontal divider line. */
    public fun divider(): Unit = content.divider()

    /** Add an inline TEXT-style hyperlink with display [text] pointing to [href]. */
    public fun link(text: String, href: String): Unit = content.link(text, href)

    /** Add a BUTTON-style CTA with display [text] pointing to [href]. */
    public fun button(text: String, href: String): Unit = content.button(text, href)

    /** Add an inline image from raw bytes. */
    public fun image(
        data: ByteArray,
        contentType: String = "image/png",
        width: Int? = null,
        height: Int? = null,
    ): Unit = content.image(data, contentType, width, height)

    /** Add an inline image from an [ImageSource]. */
    public fun image(source: ImageSource, width: Int? = null, height: Int? = null): Unit =
        content.image(source, width, height)

    /** Add a horizontal row of child elements. [weights] controls relative column widths. */
    public fun row(vararg weights: Float, init: ContentBuilder.() -> Unit): Unit =
        content.row(*weights, init = init)

    internal fun build(): InvoiceSection.Custom = InvoiceSection.Custom(
        key = key,
        content = content.build(),
    )
}

package com.chrisjenx.kinvoicing

/**
 * Visual style hint for [InvoiceElement.Link].
 * Renderers map TEXT to inline hyperlink typography and BUTTON to a filled CTA.
 */
public enum class LinkStyle {
    /** Inline text hyperlink — primary color, M3 labelLarge weight. */
    TEXT,
    /** Filled CTA button — primary container, white label, M3 FilledButton-equivalent. */
    BUTTON,
}

/**
 * Low-level building blocks for [InvoiceSection.Custom] sections.
 * Renderers map each variant to platform-specific UI primitives.
 */
public sealed class InvoiceElement {
    /** Styled text content. [styleRef] is an optional renderer-defined style key. */
    public data class Text(val value: String, val styleRef: String? = null) : InvoiceElement()
    /**
     * Vertical whitespace of the given [height] in logical points.
     * Compose renders as dp (density-independent pixels), HTML renders as px.
     * Default value of 16 produces visually similar results across renderers.
     */
    public data class Spacer(val height: Int = 16) : InvoiceElement()
    /** Horizontal divider line. */
    public data object Divider : InvoiceElement()
    /**
     * Horizontal row of child elements. [weights] controls the relative width of each child;
     * when empty, children share equal space.
     */
    public data class Row(
        val children: List<InvoiceElement>,
        val weights: List<Float> = emptyList(),
    ) : InvoiceElement()

    /**
     * Hyperlink with display text.
     * @property text The visible label (e.g., "Visit Our Site").
     * @property href The link URL (e.g., "https://example.com").
     * @property style Visual style — defaults to [LinkStyle.TEXT] (inline text link).
     */
    public data class Link(
        val text: String,
        val href: String,
        val style: LinkStyle = LinkStyle.TEXT,
    ) : InvoiceElement()

    /**
     * Inline image from an [ImageSource].
     *
     * Construct with [ImageSource.Bytes] for raw byte arrays, or with a CMP
     * `DrawableResource` via render-compose's `DrawableImageSource`.
     */
    public data class Image(
        val source: ImageSource,
        val width: Int? = null,
        val height: Int? = null,
    ) : InvoiceElement() {

        /** Backwards-compatible constructor accepting raw bytes. */
        public constructor(
            data: ByteArray,
            contentType: String,
            width: Int? = null,
            height: Int? = null,
        ) : this(ImageSource.Bytes(data, contentType), width, height)
    }
}

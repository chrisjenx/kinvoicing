package com.chrisjenx.invoicekit

/**
 * Low-level building blocks for [InvoiceSection.Custom] sections.
 * Renderers map each variant to platform-specific UI primitives.
 */
public sealed class InvoiceElement {
    /** Styled text content. [styleRef] is an optional renderer-defined style key. */
    public data class Text(val value: String, val styleRef: String? = null) : InvoiceElement()
    /** Vertical whitespace of the given [height] in dp/px (renderer-dependent). */
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
     * Inline image from raw bytes.
     * @property contentType MIME type (e.g., "image/png").
     */
    public data class Image(
        val data: ByteArray,
        val contentType: String,
        val width: Int? = null,
        val height: Int? = null,
    ) : InvoiceElement() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Image) return false
            return data.contentEquals(other.data) &&
                contentType == other.contentType &&
                width == other.width &&
                height == other.height
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + contentType.hashCode()
            result = 31 * result + (width ?: 0)
            result = 31 * result + (height ?: 0)
            return result
        }
    }
}

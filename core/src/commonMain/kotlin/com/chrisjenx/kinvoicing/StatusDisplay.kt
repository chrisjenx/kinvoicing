package com.chrisjenx.kinvoicing

/**
 * Controls how an [InvoiceStatus] is visually rendered on the invoice.
 *
 * Renderers that cannot support a particular mode (e.g., HTML email with [Watermark])
 * will fall back to [Banner].
 */
public sealed class StatusDisplay {
    /** Status is stored as data but not rendered visually. */
    public data object None : StatusDisplay()

    /** Small colored pill rendered in the header next to the invoice number. */
    public data object Badge : StatusDisplay()

    /** Full-width colored bar at the top of the invoice. */
    public data object Banner : StatusDisplay()

    /** Large diagonal text overlaid across the invoice body. */
    public data class Watermark(val opacity: Float = 0.15f) : StatusDisplay()

    /** Rotated stamp/seal overlay, typically in the upper-right area. */
    public data class Stamp(val opacity: Float = 0.35f) : StatusDisplay()
}

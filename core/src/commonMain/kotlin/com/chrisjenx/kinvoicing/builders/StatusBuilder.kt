package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.ArgbColor
import com.chrisjenx.kinvoicing.InvoiceDsl
import com.chrisjenx.kinvoicing.InvoiceStatus
import com.chrisjenx.kinvoicing.StatusDisplay

/**
 * DSL builder for configuring invoice status and its visual display mode.
 *
 * ```kotlin
 * invoice {
 *     status {
 *         paid()
 *         watermark()
 *     }
 * }
 * ```
 */
@InvoiceDsl
public class StatusBuilder {
    /** The invoice status. Defaults to [InvoiceStatus.Draft]. */
    public var state: InvoiceStatus = InvoiceStatus.Draft

    /** How the status is visually displayed. Defaults to [StatusDisplay.Badge]. */
    public var display: StatusDisplay = StatusDisplay.Badge

    // -- Status convenience setters --

    public fun draft() { state = InvoiceStatus.Draft }
    public fun sent() { state = InvoiceStatus.Sent }
    public fun paid() { state = InvoiceStatus.Paid }
    public fun overdue() { state = InvoiceStatus.Overdue }
    public fun voided() { state = InvoiceStatus.Void }
    public fun uncollectable() { state = InvoiceStatus.Uncollectable }
    public fun refunded() { state = InvoiceStatus.Refunded }

    /** Set a custom status with the given [label] and ARGB [color] (e.g., `0xFFF59E0B`). */
    public fun custom(label: String, color: Long) {
        state = InvoiceStatus.Custom(label, ArgbColor(color))
    }

    // -- Display mode setters --

    /** Render as a small colored pill in the header. */
    public fun badge() { display = StatusDisplay.Badge }

    /** Render as a full-width colored bar at the top. */
    public fun banner() { display = StatusDisplay.Banner }

    /** Render as large diagonal text overlaid on the invoice. */
    public fun watermark(opacity: Float = 0.15f) { display = StatusDisplay.Watermark(opacity) }

    /** Render as a rotated stamp/seal overlay. */
    public fun stamp(opacity: Float = 0.35f) { display = StatusDisplay.Stamp(opacity) }

    /** Store the status as data only — no visual rendering. */
    public fun hidden() { display = StatusDisplay.None }

    internal fun buildStatus(): InvoiceStatus = state
    internal fun buildDisplay(): StatusDisplay = display
}

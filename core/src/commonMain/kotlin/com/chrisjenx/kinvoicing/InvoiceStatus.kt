package com.chrisjenx.kinvoicing

/**
 * The status/state of an invoice. Each predefined status has a default [label] and [color].
 * Use [Custom] for application-specific states not covered by the built-in variants.
 */
public sealed class InvoiceStatus(
    public open val label: String,
    public open val color: ArgbColor,
) {
    /** Gray — invoice is being prepared, not yet sent. */
    public data object Draft : InvoiceStatus("DRAFT", ArgbColor(0xFF6B7280))

    /** Blue — invoice has been sent to the recipient. */
    public data object Sent : InvoiceStatus("SENT", ArgbColor(0xFF2563EB))

    /** Green — payment has been received in full. */
    public data object Paid : InvoiceStatus("PAID", ArgbColor(0xFF16A34A))

    /** Red — payment is past due. */
    public data object Overdue : InvoiceStatus("OVERDUE", ArgbColor(0xFFDC2626))

    /** Light gray — invoice has been cancelled/voided. */
    public data object Void : InvoiceStatus("VOID", ArgbColor(0xFF9CA3AF))

    /** Stone — debt has been written off. */
    public data object Uncollectable : InvoiceStatus("UNCOLLECTABLE", ArgbColor(0xFF78716C))

    /** Violet — payment has been returned to the payer. */
    public data object Refunded : InvoiceStatus("REFUNDED", ArgbColor(0xFF7C3AED))

    /** User-defined status with a custom label and color. */
    public data class Custom(
        override val label: String,
        override val color: ArgbColor,
    ) : InvoiceStatus(label, color)
}

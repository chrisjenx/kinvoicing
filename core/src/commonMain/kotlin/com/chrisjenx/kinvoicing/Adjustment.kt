package com.chrisjenx.kinvoicing

/**
 * A monetary adjustment applied to a line item or summary (discount, tax, fee, credit).
 *
 * @property label Human-readable label (e.g., "Early payment discount", "CO Sales Tax").
 * @property type The category of adjustment, which determines how it affects the total.
 * @property value How the adjustment is calculated — percentage, fixed amount, or absolute override.
 * @property appliesTo What the adjustment applies to (subtotal by default).
 */
public data class Adjustment(
    val label: String,
    val type: AdjustmentType,
    val value: AdjustmentValue,
    val appliesTo: AppliesTo = AppliesTo.SUBTOTAL,
)

/** Category of an [Adjustment], determining its sign and application order. */
public enum class AdjustmentType {
    /** Reduces the total (e.g., early payment discount, volume discount). */
    DISCOUNT,
    /** Increases the total (e.g., sales tax, VAT). */
    TAX,
    /** Reduces the total by a fixed amount (e.g., account credit, refund). */
    CREDIT,
    /** Increases the total by a fixed amount (e.g., wire transfer fee, processing fee). */
    FEE,
    /** User-defined adjustment with custom sign handling. */
    CUSTOM,
}

/**
 * How an [Adjustment] is calculated. Sealed hierarchy ensures exhaustive handling.
 */
public sealed class AdjustmentValue {
    /** Percentage of the base amount. [rate] is in percent (e.g., 10.0 = 10%). */
    public data class Percent(val rate: Double) : AdjustmentValue()

    /** Fixed monetary amount. Negative values reduce the total (discounts/credits). */
    public data class Fixed(val amount: Double) : AdjustmentValue()

    /** Absolute override — replaces the running total entirely. */
    public data class Absolute(val amount: Double) : AdjustmentValue()

    /**
     * Calculate the raw delta this adjustment represents against a [base] amount.
     * Returns the absolute value of the adjustment (always positive for Percent).
     * Sign handling is done by [Adjustment.displayAmount] based on [AdjustmentType].
     */
    public fun applyTo(base: Double): Double = when (this) {
        is Percent -> base * rate / 100.0
        is Fixed -> amount
        is Absolute -> amount
    }
}

/** What scope an [Adjustment] applies to. */
public enum class AppliesTo {
    /** Applied to the computed subtotal (default). */
    SUBTOTAL,
    /** Applied to each line item individually. */
    ALL_ITEMS,
}

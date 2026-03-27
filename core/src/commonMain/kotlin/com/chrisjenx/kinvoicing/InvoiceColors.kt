package com.chrisjenx.kinvoicing

/**
 * Semantic color constants used across renderers for consistent styling.
 *
 * @deprecated Use [InvoiceStyle] properties instead: [InvoiceStyle.negativeColor],
 * [InvoiceStyle.borderColor], [InvoiceStyle.dividerColor], [InvoiceStyle.mutedBackgroundColor].
 * These constants remain as the default values in [InvoiceStyle].
 */
@Deprecated("Use InvoiceStyle semantic color properties instead", level = DeprecationLevel.WARNING)
public object InvoiceColors {
    /** Red for negative amounts, discounts, credits. */
    @Deprecated("Use InvoiceStyle.negativeColor", ReplaceWith("InvoiceStyle().negativeColor"))
    public const val NEGATIVE: Long = 0xFFDC2626

    /** Light border for table dividers. */
    @Deprecated("Use InvoiceStyle.borderColor", ReplaceWith("InvoiceStyle().borderColor"))
    public const val BORDER: Long = 0xFFE2E8F0

    /** Very light border for subtle row dividers. */
    @Deprecated("Use InvoiceStyle.dividerColor", ReplaceWith("InvoiceStyle().dividerColor"))
    public const val DIVIDER: Long = 0xFFF1F5F9

    /** Muted background for alternating rows, meta blocks, payment info. */
    @Deprecated("Use InvoiceStyle.mutedBackgroundColor", ReplaceWith("InvoiceStyle().mutedBackgroundColor"))
    public const val BG_MUTED: Long = 0xFFF8FAFC
}

package com.chrisjenx.invoicekit

/**
 * Semantic color constants used across renderers for consistent styling.
 * These are the default palette colors — [InvoiceStyle] colors override branding.
 */
public object InvoiceColors {
    /** Red for negative amounts, discounts, credits. */
    public const val NEGATIVE: Long = 0xFFDC2626

    /** Light border for table dividers. */
    public const val BORDER: Long = 0xFFE2E8F0

    /** Very light border for subtle row dividers. */
    public const val DIVIDER: Long = 0xFFF1F5F9

    /** Muted background for alternating rows, meta blocks, payment info. */
    public const val BG_MUTED: Long = 0xFFF8FAFC
}

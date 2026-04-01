package com.chrisjenx.kinvoicing

/**
 * Curated built-in themes for invoices. Each theme is a pre-configured [InvoiceStyle].
 *
 * Use with the DSL:
 * ```kotlin
 * invoice {
 *     style {
 *         theme(InvoiceThemes.Elegant)
 *         accentBorder = true // override individual properties
 *     }
 * }
 * ```
 */
public object InvoiceThemes {

    /** The default theme — matches [InvoiceStyle] defaults. */
    public val Classic: InvoiceStyle = InvoiceStyle()

    /** Conservative navy with an accent stripe. Professional and trustworthy. */
    public val Corporate: InvoiceStyle = InvoiceStyle(
        primaryColor = 0xFF1E3A5F,
        secondaryColor = 0xFF6B7280,
        textColor = 0xFF111827,
        backgroundColor = 0xFFFFFFFF,
        accentBorder = true,
        negativeColor = 0xFFB91C1C,
        borderColor = 0xFFD1D5DB,
        dividerColor = 0xFFE5E7EB,
        mutedBackgroundColor = 0xFFF3F4F6,
        surfaceColor = 0xFFF9FAFB,
    )

    /** Indigo tones, airy spacing, minimal decoration. */
    public val Modern: InvoiceStyle = InvoiceStyle(
        primaryColor = 0xFF4F46E5,
        secondaryColor = 0xFF6B7280,
        textColor = 0xFF1F2937,
        backgroundColor = 0xFFFFFFFF,
        negativeColor = 0xFFDC2626,
        borderColor = 0xFFE5E7EB,
        dividerColor = 0xFFF3F4F6,
        mutedBackgroundColor = 0xFFF9FAFB,
        surfaceColor = 0xFFF9FAFB,
    )

    /** Strong blue with grid lines and accent border. Structured and authoritative. */
    public val Bold: InvoiceStyle = InvoiceStyle(
        primaryColor = 0xFF1D4ED8,
        secondaryColor = 0xFF475569,
        textColor = 0xFF0F172A,
        backgroundColor = 0xFFFFFFFF,
        headerLayout = HeaderLayout.STACKED,
        showGridLines = true,
        accentBorder = true,
        negativeColor = 0xFFEF4444,
        borderColor = 0xFFCBD5E1,
        dividerColor = 0xFFE2E8F0,
        mutedBackgroundColor = 0xFFF1F5F9,
        surfaceColor = 0xFFF8FAFC,
    )

    /** Amber earth tones on warm ivory. Friendly and approachable. */
    public val Warm: InvoiceStyle = InvoiceStyle(
        primaryColor = 0xFFD97706,
        secondaryColor = 0xFF92400E,
        textColor = 0xFF451A03,
        backgroundColor = 0xFFFFFBEB,
        fontFamily = "Georgia",
        negativeColor = 0xFFDC2626,
        borderColor = 0xFFFDE68A,
        dividerColor = 0xFFFEF3C7,
        mutedBackgroundColor = 0xFFFEF3C7,
        surfaceColor = 0xFFFFFBEB,
    )

    /** Near-monochrome, ultra-clean. Lets the content speak. */
    public val Minimal: InvoiceStyle = InvoiceStyle(
        primaryColor = 0xFF374151,
        secondaryColor = 0xFF9CA3AF,
        textColor = 0xFF111827,
        backgroundColor = 0xFFFFFFFF,
        negativeColor = 0xFFDC2626,
        borderColor = 0xFFF3F4F6,
        dividerColor = 0xFFF9FAFB,
        mutedBackgroundColor = 0xFFF9FAFB,
        surfaceColor = 0xFFF9FAFB,
    )

    /** Dark stone and gold accent. Luxurious and refined. */
    public val Elegant: InvoiceStyle = InvoiceStyle(
        primaryColor = 0xFFB8860B,
        secondaryColor = 0xFF78716C,
        textColor = 0xFF1C1917,
        backgroundColor = 0xFFFAFAF9,
        fontFamily = "Georgia",
        accentBorder = true,
        negativeColor = 0xFFDC2626,
        borderColor = 0xFFD6D3D1,
        dividerColor = 0xFFE7E5E4,
        mutedBackgroundColor = 0xFFF5F5F4,
        surfaceColor = 0xFFFAFAF9,
    )

    /** Green and teal. Clean, eco-feeling, optimistic. */
    public val Fresh: InvoiceStyle = InvoiceStyle(
        primaryColor = 0xFF059669,
        secondaryColor = 0xFF047857,
        textColor = 0xFF064E3B,
        backgroundColor = 0xFFFFFFFF,
        negativeColor = 0xFFDC2626,
        borderColor = 0xFFA7F3D0,
        dividerColor = 0xFFD1FAE5,
        mutedBackgroundColor = 0xFFECFDF5,
        surfaceColor = 0xFFF0FDF4,
    )

    /** All built-in themes as name–style pairs. */
    public val all: List<Pair<String, InvoiceStyle>> = listOf(
        "Classic" to Classic,
        "Corporate" to Corporate,
        "Modern" to Modern,
        "Bold" to Bold,
        "Warm" to Warm,
        "Minimal" to Minimal,
        "Elegant" to Elegant,
        "Fresh" to Fresh,
    )
}

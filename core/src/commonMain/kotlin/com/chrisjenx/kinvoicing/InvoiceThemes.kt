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
        primaryColor = ArgbColor(0xFF1E3A5F),
        secondaryColor = ArgbColor(0xFF6B7280),
        textColor = ArgbColor(0xFF111827),
        backgroundColor = ArgbColor(0xFFFFFFFF),
        accentBorder = true,
        negativeColor = ArgbColor(0xFFB91C1C),
        borderColor = ArgbColor(0xFFD1D5DB),
        dividerColor = ArgbColor(0xFFE5E7EB),
        mutedBackgroundColor = ArgbColor(0xFFF3F4F6),
        surfaceColor = ArgbColor(0xFFF9FAFB),
    )

    /** Indigo tones, airy spacing, minimal decoration. */
    public val Modern: InvoiceStyle = InvoiceStyle(
        primaryColor = ArgbColor(0xFF4F46E5),
        secondaryColor = ArgbColor(0xFF6B7280),
        textColor = ArgbColor(0xFF1F2937),
        backgroundColor = ArgbColor(0xFFFFFFFF),
        negativeColor = ArgbColor(0xFFDC2626),
        borderColor = ArgbColor(0xFFE5E7EB),
        dividerColor = ArgbColor(0xFFF3F4F6),
        mutedBackgroundColor = ArgbColor(0xFFF9FAFB),
        surfaceColor = ArgbColor(0xFFF9FAFB),
    )

    /** Strong blue with grid lines and accent border. Structured and authoritative. */
    public val Bold: InvoiceStyle = InvoiceStyle(
        primaryColor = ArgbColor(0xFF1D4ED8),
        secondaryColor = ArgbColor(0xFF475569),
        textColor = ArgbColor(0xFF0F172A),
        backgroundColor = ArgbColor(0xFFFFFFFF),
        headerLayout = HeaderLayout.STACKED,
        showGridLines = true,
        accentBorder = true,
        negativeColor = ArgbColor(0xFFEF4444),
        borderColor = ArgbColor(0xFFCBD5E1),
        dividerColor = ArgbColor(0xFFE2E8F0),
        mutedBackgroundColor = ArgbColor(0xFFF1F5F9),
        surfaceColor = ArgbColor(0xFFF8FAFC),
    )

    /** Amber earth tones on warm ivory. Friendly and approachable. */
    public val Warm: InvoiceStyle = InvoiceStyle(
        primaryColor = ArgbColor(0xFFD97706),
        secondaryColor = ArgbColor(0xFF92400E),
        textColor = ArgbColor(0xFF451A03),
        backgroundColor = ArgbColor(0xFFFFFBEB),
        fontFamily = "Georgia",
        negativeColor = ArgbColor(0xFFDC2626),
        borderColor = ArgbColor(0xFFFDE68A),
        dividerColor = ArgbColor(0xFFFEF3C7),
        mutedBackgroundColor = ArgbColor(0xFFFEF3C7),
        surfaceColor = ArgbColor(0xFFFFFBEB),
    )

    /** Near-monochrome, ultra-clean. Lets the content speak. */
    public val Minimal: InvoiceStyle = InvoiceStyle(
        primaryColor = ArgbColor(0xFF374151),
        secondaryColor = ArgbColor(0xFF9CA3AF),
        textColor = ArgbColor(0xFF111827),
        backgroundColor = ArgbColor(0xFFFFFFFF),
        negativeColor = ArgbColor(0xFFDC2626),
        borderColor = ArgbColor(0xFFF3F4F6),
        dividerColor = ArgbColor(0xFFF9FAFB),
        mutedBackgroundColor = ArgbColor(0xFFF9FAFB),
        surfaceColor = ArgbColor(0xFFF9FAFB),
    )

    /** Dark stone and gold accent. Luxurious and refined. */
    public val Elegant: InvoiceStyle = InvoiceStyle(
        primaryColor = ArgbColor(0xFFB8860B),
        secondaryColor = ArgbColor(0xFF78716C),
        textColor = ArgbColor(0xFF1C1917),
        backgroundColor = ArgbColor(0xFFFAFAF9),
        fontFamily = "Georgia",
        accentBorder = true,
        negativeColor = ArgbColor(0xFFDC2626),
        borderColor = ArgbColor(0xFFD6D3D1),
        dividerColor = ArgbColor(0xFFE7E5E4),
        mutedBackgroundColor = ArgbColor(0xFFF5F5F4),
        surfaceColor = ArgbColor(0xFFFAFAF9),
    )

    /** Green and teal. Clean, eco-feeling, optimistic. */
    public val Fresh: InvoiceStyle = InvoiceStyle(
        primaryColor = ArgbColor(0xFF059669),
        secondaryColor = ArgbColor(0xFF047857),
        textColor = ArgbColor(0xFF064E3B),
        backgroundColor = ArgbColor(0xFFFFFFFF),
        negativeColor = ArgbColor(0xFFDC2626),
        borderColor = ArgbColor(0xFFA7F3D0),
        dividerColor = ArgbColor(0xFFD1FAE5),
        mutedBackgroundColor = ArgbColor(0xFFECFDF5),
        surfaceColor = ArgbColor(0xFFF0FDF4),
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

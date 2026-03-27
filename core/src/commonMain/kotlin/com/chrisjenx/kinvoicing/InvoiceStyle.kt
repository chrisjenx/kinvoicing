package com.chrisjenx.kinvoicing

/**
 * Visual styling for the entire invoice. Colors use [ArgbColor] for type safety.
 *
 * @property showGridLines When true, renderers draw borders around line-item rows.
 * @property accentBorder When true, renderers add a colored top/left border accent.
 * @property negativeColor Color for negative amounts, discounts, and credits.
 * @property borderColor Color for table borders and grid lines.
 * @property dividerColor Color for subtle row dividers.
 * @property mutedBackgroundColor Background for alternating rows, meta blocks, payment info.
 * @property surfaceColor Background for card-like sections (distinct from [backgroundColor]).
 */
public data class InvoiceStyle(
    val primaryColor: ArgbColor = ArgbColor(0xFF2563EB),
    val secondaryColor: ArgbColor = ArgbColor(0xFF64748B),
    val textColor: ArgbColor = ArgbColor(0xFF1E293B),
    val backgroundColor: ArgbColor = ArgbColor(0xFFFFFFFF),
    val fontFamily: String = "Inter",
    val logoPlacement: LogoPlacement = LogoPlacement.LEFT,
    val headerLayout: HeaderLayout = HeaderLayout.HORIZONTAL,
    val showGridLines: Boolean = false,
    val accentBorder: Boolean = false,
    val negativeColor: ArgbColor = ArgbColor(0xFFDC2626),
    val borderColor: ArgbColor = ArgbColor(0xFFE2E8F0),
    val dividerColor: ArgbColor = ArgbColor(0xFFF1F5F9),
    val mutedBackgroundColor: ArgbColor = ArgbColor(0xFFF8FAFC),
    val surfaceColor: ArgbColor = ArgbColor(0xFFFFFFFF),
) {
    public companion object
}

/** Horizontal alignment of the brand logo in the header. */
public enum class LogoPlacement {
    LEFT,
    CENTER,
    RIGHT,
}

/** Layout direction for the invoice header section. */
public enum class HeaderLayout {
    /** Logo/brand and invoice details side-by-side. */
    HORIZONTAL,
    /** Logo/brand stacked above invoice details. */
    STACKED,
}

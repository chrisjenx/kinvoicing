package com.chrisjenx.kinvoicing

/**
 * Visual styling for the entire invoice. Colors are ARGB [Long] values (e.g., `0xFF2563EB`).
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
    val primaryColor: Long = 0xFF2563EB,
    val secondaryColor: Long = 0xFF64748B,
    val textColor: Long = 0xFF1E293B,
    val backgroundColor: Long = 0xFFFFFFFF,
    val fontFamily: String = "Inter",
    val logoPlacement: LogoPlacement = LogoPlacement.LEFT,
    val headerLayout: HeaderLayout = HeaderLayout.HORIZONTAL,
    val showGridLines: Boolean = false,
    val accentBorder: Boolean = false,
    val negativeColor: Long = 0xFFDC2626,
    val borderColor: Long = 0xFFE2E8F0,
    val dividerColor: Long = 0xFFF1F5F9,
    val mutedBackgroundColor: Long = 0xFFF8FAFC,
    val surfaceColor: Long = 0xFFFFFFFF,
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

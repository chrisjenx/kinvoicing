package com.chrisjenx.invoicekit

/**
 * Visual styling for the entire invoice. Colors are ARGB [Long] values (e.g., `0xFF2563EB`).
 *
 * @property showGridLines When true, renderers draw borders around line-item rows.
 * @property accentBorder When true, renderers add a colored top/left border accent.
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
)

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

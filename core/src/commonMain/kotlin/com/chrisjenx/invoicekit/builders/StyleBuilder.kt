package com.chrisjenx.invoicekit.builders

import com.chrisjenx.invoicekit.*

/** DSL builder for [InvoiceStyle]. All properties have sensible defaults. */
@InvoiceDsl
public class StyleBuilder {
    public var primaryColor: Long = 0xFF2563EB
    public var secondaryColor: Long = 0xFF64748B
    public var textColor: Long = 0xFF1E293B
    public var backgroundColor: Long = 0xFFFFFFFF
    public var fontFamily: String = "Inter"
    public var logoPlacement: LogoPlacement = LogoPlacement.LEFT
    public var headerLayout: HeaderLayout = HeaderLayout.HORIZONTAL
    public var showGridLines: Boolean = false
    public var accentBorder: Boolean = false

    internal fun build(): InvoiceStyle = InvoiceStyle(
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        textColor = textColor,
        backgroundColor = backgroundColor,
        fontFamily = fontFamily,
        logoPlacement = logoPlacement,
        headerLayout = headerLayout,
        showGridLines = showGridLines,
        accentBorder = accentBorder,
    )
}

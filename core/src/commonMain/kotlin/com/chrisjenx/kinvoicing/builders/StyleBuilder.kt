package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.util.sanitizeFontFamily

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
    public var negativeColor: Long = 0xFFDC2626
    public var borderColor: Long = 0xFFE2E8F0
    public var dividerColor: Long = 0xFFF1F5F9
    public var mutedBackgroundColor: Long = 0xFFF8FAFC
    public var surfaceColor: Long = 0xFFFFFFFF

    /** Apply a pre-built [InvoiceStyle] as the base, then override individual properties. */
    public fun theme(base: InvoiceStyle) {
        primaryColor = base.primaryColor.value
        secondaryColor = base.secondaryColor.value
        textColor = base.textColor.value
        backgroundColor = base.backgroundColor.value
        fontFamily = base.fontFamily
        logoPlacement = base.logoPlacement
        headerLayout = base.headerLayout
        showGridLines = base.showGridLines
        accentBorder = base.accentBorder
        negativeColor = base.negativeColor.value
        borderColor = base.borderColor.value
        dividerColor = base.dividerColor.value
        mutedBackgroundColor = base.mutedBackgroundColor.value
        surfaceColor = base.surfaceColor.value
    }

    internal fun build(): InvoiceStyle = InvoiceStyle(
        primaryColor = ArgbColor(primaryColor),
        secondaryColor = ArgbColor(secondaryColor),
        textColor = ArgbColor(textColor),
        backgroundColor = ArgbColor(backgroundColor),
        fontFamily = sanitizeFontFamily(fontFamily),
        logoPlacement = logoPlacement,
        headerLayout = headerLayout,
        showGridLines = showGridLines,
        accentBorder = accentBorder,
        negativeColor = ArgbColor(negativeColor),
        borderColor = ArgbColor(borderColor),
        dividerColor = ArgbColor(dividerColor),
        mutedBackgroundColor = ArgbColor(mutedBackgroundColor),
        surfaceColor = ArgbColor(surfaceColor),
    )
}

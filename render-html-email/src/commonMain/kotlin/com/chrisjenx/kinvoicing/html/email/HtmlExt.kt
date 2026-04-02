package com.chrisjenx.kinvoicing.html.email

import com.chrisjenx.kinvoicing.ArgbColor
import com.chrisjenx.kinvoicing.ImageSource
import com.chrisjenx.kinvoicing.InvoiceDocument
import com.chrisjenx.kinvoicing.InvoiceStyle
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Convenience extension to render an InvoiceDocument to HTML.
 */
public fun InvoiceDocument.toHtml(config: HtmlRenderConfig = HtmlRenderConfig.Default): String {
    return HtmlRenderer(config).render(this)
}

/** Encode an [ImageSource] as a base64 data URI for HTML `<img>` tags. */
@OptIn(ExperimentalEncodingApi::class)
internal fun ImageSource.toDataUri(): String =
    "data:$contentType;base64,${Base64.Default.encode(bytes)}"

/**
 * Pre-computed CSS hex color strings for an [InvoiceStyle].
 * Created once per render to avoid repeated [ArgbColor.toHexColor] conversions in section loops.
 */
internal class HtmlColors(val style: InvoiceStyle) {
    val primary: String = style.primaryColor.toHexColor()
    val secondary: String = style.secondaryColor.toHexColor()
    val text: String = style.textColor.toHexColor()
    val background: String = style.backgroundColor.toHexColor()
    val negative: String = style.negativeColor.toHexColor()
    val border: String = style.borderColor.toHexColor()
    val divider: String = style.dividerColor.toHexColor()
    val mutedBg: String = style.mutedBackgroundColor.toHexColor()
    val surface: String = style.surfaceColor.toHexColor()
}

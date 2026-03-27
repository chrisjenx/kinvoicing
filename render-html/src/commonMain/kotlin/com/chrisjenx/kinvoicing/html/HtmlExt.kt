package com.chrisjenx.kinvoicing.html

import com.chrisjenx.kinvoicing.InvoiceDocument

/**
 * Convenience extension to render an InvoiceDocument to HTML.
 */
public fun InvoiceDocument.toHtml(config: HtmlRenderConfig = HtmlRenderConfig()): String {
    return HtmlRenderer(config).render(this)
}

/**
 * Convert a Long ARGB color to a CSS hex string (e.g., "#2563EB").
 */
internal fun Long.toHexColor(): String {
    val r = (this shr 16 and 0xFF).toInt()
    val g = (this shr 8 and 0xFF).toInt()
    val b = (this and 0xFF).toInt()
    return "#${r.hex()}${g.hex()}${b.hex()}"
}

private fun Int.hex(): String = this.toString(16).padStart(2, '0').uppercase()

package com.chrisjenx.kinvoicing.html.email.sections

import com.chrisjenx.kinvoicing.InvoiceStatus
import com.chrisjenx.kinvoicing.InvoiceStyle
import com.chrisjenx.kinvoicing.StatusDisplay
import kotlinx.html.*

/**
 * Full-width colored banner at the top of the invoice.
 */
internal fun FlowContent.renderStatusBanner(status: InvoiceStatus, style: InvoiceStyle) {
    div {
        attributes["style"] = buildString {
            append("background-color: ${status.color.toHexColor()};")
            append(" color: #FFFFFF;")
            append(" text-align: center;")
            append(" font-size: 16px;")
            append(" font-weight: bold;")
            append(" letter-spacing: 1px;")
            append(" padding: 8px 16px;")
            append(" border-radius: 4px;")
        }
        +status.label
    }
}

/**
 * Small colored pill/badge rendered inline, typically next to the invoice number.
 */
internal fun FlowContent.renderStatusBadge(status: InvoiceStatus) {
    span {
        attributes["style"] = buildString {
            append("display: inline-block;")
            append(" background-color: ${status.color.toHexColor()};")
            append(" color: #FFFFFF;")
            append(" font-size: 11px;")
            append(" font-weight: bold;")
            append(" letter-spacing: 0.5px;")
            append(" padding: 2px 8px;")
            append(" border-radius: 4px;")
            append(" vertical-align: middle;")
            append(" margin-left: 8px;")
        }
        +status.label
    }
}

/**
 * Generates an inline SVG data URI containing rotated watermark text.
 * SVG is widely supported in email clients as an `<img>` source.
 */
private fun watermarkSvgDataUri(label: String, hexColor: String, opacity: Float): String {
    val svg = buildString {
        append("<svg xmlns='http://www.w3.org/2000/svg' width='600' height='400'>")
        append("<text x='300' y='200' text-anchor='middle' dominant-baseline='central'")
        append(" font-size='72' font-weight='800' letter-spacing='8'")
        append(" fill='$hexColor' fill-opacity='$opacity'")
        append(" transform='rotate(-30, 300, 200)'>")
        append(label)
        append("</text></svg>")
    }
    val escaped = svg
        .replace("'", "%27")
        .replace("#", "%23")
        .replace("<", "%3C")
        .replace(">", "%3E")
    return "data:image/svg+xml,$escaped"
}

/**
 * Generates an inline SVG data URI containing a rotated stamp/seal.
 */
private fun stampSvgDataUri(label: String, hexColor: String, opacity: Float): String {
    // Estimate text width: ~16px per character at font-size 28
    val textW = label.length * 16
    val rectW = textW + 40
    val rectH = 50
    val svgW = rectW + 60
    val svgH = rectH + 40
    val cx = svgW / 2
    val cy = svgH / 2

    val svg = buildString {
        append("<svg xmlns='http://www.w3.org/2000/svg' width='$svgW' height='$svgH'>")
        append("<g transform='rotate(-15, $cx, $cy)' opacity='$opacity'>")
        append("<rect x='${(svgW - rectW) / 2}' y='${(svgH - rectH) / 2}'")
        append(" width='$rectW' height='$rectH' rx='8' ry='8'")
        append(" fill='none' stroke='$hexColor' stroke-width='3'/>")
        append("<text x='$cx' y='$cy' text-anchor='middle' dominant-baseline='central'")
        append(" font-size='28' font-weight='800' letter-spacing='2'")
        append(" fill='$hexColor'>")
        append(label)
        append("</text></g></svg>")
    }
    val escaped = svg
        .replace("'", "%27")
        .replace("#", "%23")
        .replace("<", "%3C")
        .replace(">", "%3E")
    return "data:image/svg+xml,$escaped"
}

/**
 * Builds inline CSS for a `background` property containing the watermark SVG.
 * Applied to the content `<td>` so the watermark renders behind all sections.
 *
 * Uses `background` shorthand with `center/contain no-repeat` so the SVG
 * scales to fill the cell without tiling. Email-safe: `<td background>` and
 * inline `background` styles are widely supported (Apple Mail, Gmail, Yahoo,
 * Outlook with VML fallback).
 */
internal fun watermarkBackgroundStyle(status: InvoiceStatus, display: StatusDisplay.Watermark): String {
    val hexColor = status.color.toHexColor()
    val uri = watermarkSvgDataUri(status.label, hexColor, display.opacity)
    return "background: url('$uri') center center / contain no-repeat;"
}

/**
 * Builds inline CSS for a `background` property containing the stamp SVG.
 * Positioned in the top-right of the content cell.
 */
internal fun stampBackgroundStyle(status: InvoiceStatus, display: StatusDisplay.Stamp): String {
    val hexColor = status.color.toHexColor()
    val uri = stampSvgDataUri(status.label, hexColor, display.opacity)
    return "background: url('$uri') top right / auto no-repeat;"
}

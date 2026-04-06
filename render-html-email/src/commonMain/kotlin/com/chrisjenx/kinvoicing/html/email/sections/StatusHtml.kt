package com.chrisjenx.kinvoicing.html.email.sections

import com.chrisjenx.kinvoicing.InvoiceStatus
import com.chrisjenx.kinvoicing.InvoiceStyle
import kotlinx.html.*

/**
 * Full-width colored banner at the top of the invoice.
 * Also used as the fallback for Watermark/Stamp in HTML email (CSS transforms are unreliable).
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

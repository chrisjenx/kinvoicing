package com.chrisjenx.kinvoicing.html.email.sections

import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.html.email.toDataUri
import com.chrisjenx.kinvoicing.util.requireSafeUrl
import kotlinx.html.*

internal fun FlowContent.renderCustom(custom: InvoiceSection.Custom, style: InvoiceStyle) {
    div {
        custom.content.forEach { element ->
            renderElement(element, style)
        }
    }
}

internal fun FlowContent.renderElement(element: InvoiceElement, style: InvoiceStyle) {
    when (element) {
        is InvoiceElement.Text -> {
            div { attributes["style"] = "font-size: 14px; color: ${style.textColor.toHexColor()};" ; +element.value }
        }
        is InvoiceElement.Spacer -> {
            div { attributes["style"] = "height: ${element.height}px;" }
        }
        is InvoiceElement.Divider -> {
            hr { attributes["style"] = "border: none; border-top: 1px solid ${style.borderColor.toHexColor()}; margin: 8px 0;" }
        }
        is InvoiceElement.Row -> {
            val totalWeight = if (element.weights.isNotEmpty()) element.weights.sum() else element.children.size.toFloat()
            table {
                attributes["width"] = "100%"
                attributes["cellpadding"] = "0"
                attributes["cellspacing"] = "0"
                tr {
                    element.children.forEachIndexed { i, child ->
                        td {
                            val weight = if (element.weights.isNotEmpty() && i < element.weights.size) element.weights[i] else 1f
                            attributes["width"] = "${(weight / totalWeight * 100).toInt()}%"
                            renderElement(child, style)
                        }
                    }
                }
            }
        }
        is InvoiceElement.Link -> when (element.style) {
            LinkStyle.TEXT -> div {
                a {
                    href = requireSafeUrl(element.href, "href")
                    attributes["style"] =
                        "font-size:14px; color:${style.primaryColor.toHexColor()}; font-weight:500; text-decoration:none;"
                    +element.text
                }
            }
            LinkStyle.BUTTON -> table {
                attributes["cellpadding"] = "0"
                attributes["cellspacing"] = "0"
                attributes["border"] = "0"
                attributes["style"] = "margin: 8px 0;"
                tr {
                    td {
                        attributes["style"] =
                            "background:${style.primaryColor.toHexColor()}; border-radius:20px; padding:10px 24px;"
                        a {
                            href = requireSafeUrl(element.href, "href")
                            attributes["style"] =
                                "color:#ffffff; font-weight:500; font-size:14px; text-decoration:none; display:inline-block;"
                            +element.text
                        }
                    }
                }
            }
        }
        is InvoiceElement.Image -> {
            val w = element.width
            val h = element.height
            val sizeStyle = buildString {
                append("display: block;")
                if (w != null) append(" max-width: ${w}px;")
                if (h != null) append(" max-height: ${h}px;")
            }
            img {
                src = element.source.toDataUri()
                attributes["style"] = sizeStyle
            }
        }
    }
}

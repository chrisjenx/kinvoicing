package com.chrisjenx.invoicekit.html.sections

import com.chrisjenx.invoicekit.*
import com.chrisjenx.invoicekit.html.toHexColor
import kotlinx.html.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal fun FlowContent.renderCustom(custom: InvoiceSection.Custom, style: InvoiceStyle) {
    div {
        attributes["style"] = "margin-bottom: 16px;"
        custom.content.forEach { element ->
            renderElement(element, style)
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
internal fun FlowContent.renderElement(element: InvoiceElement, style: InvoiceStyle) {
    when (element) {
        is InvoiceElement.Text -> {
            div { attributes["style"] = "font-size: 14px; color: ${style.textColor.toHexColor()};" ; +element.value }
        }
        is InvoiceElement.Spacer -> {
            div { attributes["style"] = "height: ${element.height}px;" }
        }
        is InvoiceElement.Divider -> {
            hr { attributes["style"] = "border: none; border-top: 1px solid ${InvoiceColors.BORDER.toHexColor()}; margin: 8px 0;" }
        }
        is InvoiceElement.Row -> {
            table {
                attributes["width"] = "100%"
                attributes["cellpadding"] = "0"
                attributes["cellspacing"] = "0"
                tr {
                    element.children.forEachIndexed { i, child ->
                        td {
                            if (element.weights.isNotEmpty() && i < element.weights.size) {
                                attributes["width"] = "${(element.weights[i] * 100).toInt()}%"
                            }
                            renderElement(child, style)
                        }
                    }
                }
            }
        }
        is InvoiceElement.Image -> {
            val src = "data:${element.contentType};base64,${Base64.Default.encode(element.data)}"
            img {
                this.src = src
                element.width?.let { width = it.toString() }
                element.height?.let { height = it.toString() }
                attributes["style"] = "display: block;"
            }
        }
    }
}

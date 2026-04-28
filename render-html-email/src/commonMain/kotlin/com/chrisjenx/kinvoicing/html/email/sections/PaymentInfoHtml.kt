package com.chrisjenx.kinvoicing.html.email.sections

import com.chrisjenx.kinvoicing.*
import kotlinx.html.*

internal fun FlowContent.renderPaymentInfo(payment: InvoiceSection.PaymentInfo, style: InvoiceStyle) {
    div {
        attributes["style"] = "padding: 16px; background-color: ${style.mutedBackgroundColor.toHexColor()}; border-radius: 4px;"
        div {
            attributes["style"] = "font-size: 13px; font-weight: bold; text-transform: uppercase; color: ${style.secondaryColor.toHexColor()}; margin-bottom: 8px;"
            +"Payment Information"
        }
        payment.bankName?.let {
            div { attributes["style"] = "font-size: 13px; color: ${style.textColor.toHexColor()};" ; +"Bank: $it" }
        }
        payment.accountNumber?.let {
            div { attributes["style"] = "font-size: 13px; color: ${style.textColor.toHexColor()};" ; +"Account: $it" }
        }
        payment.routingNumber?.let {
            div { attributes["style"] = "font-size: 13px; color: ${style.textColor.toHexColor()};" ; +"Routing: $it" }
        }
        payment.paymentLink?.let { link ->
            div {
                attributes["style"] = "margin-top: 8px;"
                renderElement(link, style)
            }
        }
        payment.notes?.let { elements ->
            div {
                attributes["style"] = "margin-top: 8px;"
                elements.forEach { renderElement(it, style) }
            }
        }
    }
}

package com.chrisjenx.kinvoicing.html.sections

import com.chrisjenx.kinvoicing.InvoiceStyle
import com.chrisjenx.kinvoicing.html.toHexColor
import kotlinx.html.*

internal fun FlowContent.renderParty(
    name: String,
    address: List<String>,
    email: String?,
    phone: String?,
    label: String,
    style: InvoiceStyle,
) {
    div {
        attributes["style"] = "margin-bottom: 16px;"
        div {
            attributes["style"] = "font-size: 11px; font-weight: bold; text-transform: uppercase; color: ${style.secondaryColor.toHexColor()}; margin-bottom: 4px;"
            +label
        }
        div {
            attributes["style"] = "font-size: 14px; font-weight: bold; color: ${style.textColor.toHexColor()};"
            +name
        }
        address.forEach { line ->
            div { attributes["style"] = "font-size: 13px; color: ${style.textColor.toHexColor()};" ; +line }
        }
        email?.let {
            div { attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()};" ; +it }
        }
        phone?.let {
            div { attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()};" ; +it }
        }
    }
}

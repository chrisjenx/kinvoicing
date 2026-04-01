package com.chrisjenx.kinvoicing.html.email.sections

import com.chrisjenx.kinvoicing.ContactInfo
import com.chrisjenx.kinvoicing.InvoiceStyle
import kotlinx.html.*

internal fun FlowContent.renderParty(contact: ContactInfo, label: String, style: InvoiceStyle) {
    div {
        attributes["style"] = "margin-bottom: 16px;"
        div {
            attributes["style"] = "font-size: 11px; font-weight: bold; text-transform: uppercase; color: ${style.secondaryColor.toHexColor()}; margin-bottom: 4px;"
            +label
        }
        div {
            attributes["style"] = "font-size: 14px; font-weight: bold; color: ${style.textColor.toHexColor()};"
            +contact.name
        }
        contact.address.forEach { line ->
            div { attributes["style"] = "font-size: 13px; color: ${style.textColor.toHexColor()};" ; +line }
        }
        contact.email?.let {
            div { attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()};" ; +it }
        }
        contact.phone?.let {
            div { attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()};" ; +it }
        }
    }
}

package com.chrisjenx.kinvoicing.html.sections

import com.chrisjenx.kinvoicing.*
import kotlinx.html.*

internal fun FlowContent.renderFooter(footer: InvoiceSection.Footer, style: InvoiceStyle) {
    div {
        attributes["style"] = "margin-top: 24px; padding: 16px; background-color: ${style.mutedBackgroundColor.toHexColor()}; border-radius: 4px;"
        footer.notes?.let {
            div {
                attributes["style"] = "font-size: 11px; font-weight: bold; text-transform: uppercase; color: ${style.secondaryColor.toHexColor()}; margin-bottom: 4px;"
                +"Notes"
            }
            div { attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()}; margin-bottom: 8px;" ; +it }
        }
        footer.terms?.let {
            div {
                attributes["style"] = "font-size: 11px; font-weight: bold; text-transform: uppercase; color: ${style.secondaryColor.toHexColor()}; margin-bottom: 4px;"
                +"Terms"
            }
            div { attributes["style"] = "font-size: 12px; color: ${style.secondaryColor.toHexColor()};" ; +it }
        }
        footer.customContent?.let {
            div { attributes["style"] = "font-size: 12px; color: ${style.secondaryColor.toHexColor()}; margin-top: 8px;" ; +it }
        }
    }
}

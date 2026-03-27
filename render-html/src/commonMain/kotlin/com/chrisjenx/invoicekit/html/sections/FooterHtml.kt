package com.chrisjenx.invoicekit.html.sections

import com.chrisjenx.invoicekit.*
import com.chrisjenx.invoicekit.html.toHexColor
import kotlinx.html.*

internal fun FlowContent.renderFooter(footer: InvoiceSection.Footer, style: InvoiceStyle) {
    div {
        attributes["style"] = "margin-top: 24px; padding-top: 16px; border-top: 1px solid ${InvoiceColors.BORDER.toHexColor()};"
        footer.notes?.let {
            div { attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()}; margin-bottom: 8px;" ; +it }
        }
        footer.terms?.let {
            div { attributes["style"] = "font-size: 12px; color: ${style.secondaryColor.toHexColor()};" ; +it }
        }
        footer.customContent?.let {
            div { attributes["style"] = "font-size: 12px; color: ${style.secondaryColor.toHexColor()}; margin-top: 8px;" ; +it }
        }
    }
}

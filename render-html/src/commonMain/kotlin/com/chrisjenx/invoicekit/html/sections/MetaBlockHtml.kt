package com.chrisjenx.invoicekit.html.sections

import com.chrisjenx.invoicekit.*
import com.chrisjenx.invoicekit.html.toHexColor
import kotlinx.html.*

internal fun FlowContent.renderMetaBlock(metaBlock: InvoiceSection.MetaBlock, style: InvoiceStyle) {
    div {
        attributes["style"] = "margin-bottom: 16px; padding: 12px; background-color: ${InvoiceColors.BG_MUTED.toHexColor()}; border-radius: 4px;"
        metaBlock.entries.forEach { entry ->
            div {
                attributes["style"] = "font-size: 13px; margin-bottom: 4px;"
                span { attributes["style"] = "font-weight: bold; color: ${style.secondaryColor.toHexColor()};" ; +"${entry.label}: " }
                span { attributes["style"] = "color: ${style.textColor.toHexColor()};" ; +entry.value }
            }
        }
    }
}

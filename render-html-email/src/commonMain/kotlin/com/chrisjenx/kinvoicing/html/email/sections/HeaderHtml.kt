package com.chrisjenx.kinvoicing.html.email.sections

import com.chrisjenx.kinvoicing.*
import kotlinx.html.*

internal fun FlowContent.renderHeader(header: InvoiceSection.Header, style: InvoiceStyle) {
    val borderStyle = if (style.accentBorder) "border-bottom: 3px solid ${style.primaryColor.toHexColor()};" else ""

    when (style.headerLayout) {
        HeaderLayout.HORIZONTAL -> {
            table {
                attributes["width"] = "100%"
                attributes["cellpadding"] = "0"
                attributes["cellspacing"] = "0"
                attributes["style"] = "margin-bottom: 24px; $borderStyle"
                tr {
                    td {
                        attributes["style"] = "vertical-align: top; padding-bottom: 16px;"
                        header.branding?.let { renderBranding(it, style) }
                    }
                    td {
                        attributes["style"] = "vertical-align: top; text-align: right; padding-bottom: 16px;"
                        renderInvoiceDetails(header, style)
                    }
                }
            }
        }
        HeaderLayout.STACKED -> {
            val textAlign = when (style.logoPlacement) {
                LogoPlacement.LEFT -> "left"
                LogoPlacement.CENTER -> "center"
                LogoPlacement.RIGHT -> "right"
            }
            div {
                attributes["style"] = "margin-bottom: 24px; text-align: $textAlign; $borderStyle"
                header.branding?.let { renderBranding(it, style) }
                div {
                    attributes["style"] = "margin-top: 16px; text-align: right;"
                    renderInvoiceDetails(header, style)
                }
            }
        }
    }
}

private fun FlowContent.renderInvoiceDetails(header: InvoiceSection.Header, style: InvoiceStyle) {
    header.invoiceNumber?.let {
        div {
            attributes["style"] = "font-size: 20px; font-weight: bold; color: ${style.primaryColor.toHexColor()};"
            +it
        }
    }
    header.issueDate?.let {
        div {
            attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()}; margin-top: 4px;"
            +"Issue Date: $it"
        }
    }
    header.dueDate?.let {
        div {
            attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()}; margin-top: 4px;"
            +"Due Date: $it"
        }
    }
}

private fun FlowContent.renderBranding(branding: Branding, style: InvoiceStyle) {
    div {
        attributes["style"] = "font-size: 18px; font-weight: bold; color: ${style.textColor.toHexColor()};"
        +branding.primary.name
    }
    branding.primary.address.forEach { line ->
        div {
            attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()};"
            +line
        }
    }
    branding.primary.email?.let {
        div { attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()};" ; +it }
    }
    branding.primary.phone?.let {
        div { attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()};" ; +it }
    }
    branding.primary.website?.let {
        div { attributes["style"] = "font-size: 13px; color: ${style.primaryColor.toHexColor()};" ; +it }
    }
    branding.poweredBy?.let { pb ->
        when (branding.layout) {
            BrandLayout.POWERED_BY_FOOTER -> div {
                attributes["style"] = "margin-top: 8px; font-size: 11px; color: ${style.secondaryColor.toHexColor()};"
                +(pb.tagline ?: "Powered by ${pb.name}")
            }
            BrandLayout.POWERED_BY_HEADER -> div {
                attributes["style"] = "margin-top: 8px; font-size: 12px; color: ${style.secondaryColor.toHexColor()};"
                +pb.name
            }
            BrandLayout.DUAL_HEADER -> div {
                attributes["style"] = "margin-top: 8px; font-size: 14px; font-weight: bold; color: ${style.textColor.toHexColor()};"
                +pb.name
            }
        }
    }
}

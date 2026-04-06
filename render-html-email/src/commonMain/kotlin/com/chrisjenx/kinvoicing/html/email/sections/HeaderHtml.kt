package com.chrisjenx.kinvoicing.html.email.sections

import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.html.email.toDataUri
import com.chrisjenx.kinvoicing.util.requireSafeUrl
import kotlinx.html.*

internal fun FlowContent.renderHeader(
    header: InvoiceSection.Header,
    style: InvoiceStyle,
    status: InvoiceStatus? = null,
    statusDisplay: StatusDisplay = StatusDisplay.Badge,
) {
    // Compose: accent border has Spacer(md=12dp) then HorizontalDivider(3dp)
    val borderStyle = if (style.accentBorder) "padding-bottom: 12px; border-bottom: 3px solid ${style.primaryColor.toHexColor()};" else ""

    when (style.headerLayout) {
        HeaderLayout.HORIZONTAL -> {
            table {
                attributes["width"] = "100%"
                attributes["cellpadding"] = "0"
                attributes["cellspacing"] = "0"
                attributes["style"] = borderStyle
                tr {
                    td {
                        attributes["style"] = "vertical-align: top;"
                        header.branding?.let { renderBranding(it, style) }
                    }
                    td {
                        attributes["style"] = "vertical-align: top; text-align: right;"
                        renderInvoiceDetails(header, style, status, statusDisplay)
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
                attributes["style"] = "text-align: $textAlign; $borderStyle"
                header.branding?.let { renderBranding(it, style) }
                div {
                    attributes["style"] = "margin-top: 16px; text-align: right;"
                    renderInvoiceDetails(header, style, status, statusDisplay)
                }
            }
        }
    }
}

private fun FlowContent.renderInvoiceDetails(
    header: InvoiceSection.Header,
    style: InvoiceStyle,
    status: InvoiceStatus?,
    statusDisplay: StatusDisplay,
) {
    header.invoiceNumber?.let {
        div {
            attributes["style"] = "font-size: 20px; font-weight: bold; color: ${style.primaryColor.toHexColor()};"
            +it
            if (status != null && statusDisplay is StatusDisplay.Badge) {
                renderStatusBadge(status)
            }
        }
    }
    // Badge without invoice number: render standalone
    if (header.invoiceNumber == null && status != null && statusDisplay is StatusDisplay.Badge) {
        div { renderStatusBadge(status) }
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
    branding.primary.logo?.let { logoSource ->
        img {
            src = logoSource.toDataUri()
            alt = "${branding.primary.name} logo"
            attributes["style"] = "display: block; max-height: 60px; margin-bottom: 8px;"
        }
    }
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
    branding.primary.email?.let { email ->
        div {
            a {
                href = "mailto:$email"
                attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()}; text-decoration: none;"
                +email
            }
        }
    }
    branding.primary.phone?.let { phone ->
        div {
            a {
                href = "tel:$phone"
                attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()}; text-decoration: none;"
                +phone
            }
        }
    }
    branding.primary.website?.let { website ->
        div {
            a {
                href = requireSafeUrl(website, "website")
                attributes["style"] = "font-size: 13px; color: ${style.primaryColor.toHexColor()}; text-decoration: none;"
                +website
            }
        }
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

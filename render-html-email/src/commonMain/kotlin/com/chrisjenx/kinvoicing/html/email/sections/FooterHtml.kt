package com.chrisjenx.kinvoicing.html.email.sections

import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.html.email.toDataUri
import kotlinx.html.*

internal fun FlowContent.renderFooter(footer: InvoiceSection.Footer, style: InvoiceStyle, branding: Branding? = null) {
    div {
        attributes["style"] = "padding: 16px; background-color: ${style.mutedBackgroundColor.toHexColor()}; border-radius: 4px;"
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
        val pb = branding?.takeIf { it.layout == BrandLayout.POWERED_BY_FOOTER }?.poweredBy
        if (pb != null) {
            div {
                attributes["style"] = "margin-top: 16px; padding-top: 8px; border-top: 1px solid ${style.dividerColor.toHexColor()}; text-align: center;"
                pb.logo?.let { logoSource ->
                    img {
                        src = logoSource.toDataUri()
                        alt = "${pb.name} logo"
                        attributes["style"] = "display: inline-block; max-height: 24px; vertical-align: middle; margin-right: 8px;"
                    }
                }
                span {
                    attributes["style"] = "font-size: 11px; color: ${style.secondaryColor.toHexColor()}; vertical-align: middle;"
                    +(pb.tagline ?: "Powered by ${pb.name}")
                }
            }
        }
    }
}

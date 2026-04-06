package com.chrisjenx.kinvoicing.html.email

import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.html.email.sections.*
import kotlinx.html.*
import kotlinx.html.stream.appendHTML

/** Whether this display mode renders as a top-of-document banner. */
private fun StatusDisplay.isBanner(): Boolean = this is StatusDisplay.Banner

/**
 * Renders an [InvoiceDocument] to email-safe HTML.
 *
 * All styling is inline (no `<style>` blocks). Layout is table-based for
 * maximum email client compatibility. Max width is 600px.
 *
 * Section rendering is delegated to individual functions in the `sections` package,
 * each handling one [InvoiceSection] variant with exhaustive `when` matching.
 */
public class HtmlRenderer(
    private val config: HtmlRenderConfig = HtmlRenderConfig.Default,
) : InvoiceRenderer<String> {

    override fun render(document: InvoiceDocument): String {
        val style = document.style
        val currency = document.currency
        val bgColor = style.backgroundColor.toHexColor()

        return buildString {
            if (config.includeDoctype) appendLine("<!DOCTYPE html>")
            appendHTML().html {
                head {
                    meta { charset = "utf-8" }
                    meta {
                        name = "viewport"
                        content = "width=device-width, initial-scale=1.0"
                    }
                }
                body {
                    val safeFontFamily = style.fontFamily.replace(Regex("[;{}()<>\"'\\\\]"), "")
                    attributes["style"] = "margin: 0; padding: 0; background-color: $bgColor; font-family: $safeFontFamily, Arial, sans-serif;"
                    table {
                        attributes["role"] = "presentation"
                        attributes["width"] = "100%"
                        attributes["cellpadding"] = "0"
                        attributes["cellspacing"] = "0"
                        attributes["style"] = "max-width: 600px; margin: 0 auto; background-color: $bgColor;"
                        tr {
                            td {
                                val status = document.status
                                val statusDisplay = document.statusDisplay
                                // Watermark/Stamp render as background-image on this <td>
                                val bgStyle = when {
                                    status != null && statusDisplay is StatusDisplay.Watermark ->
                                        " ${watermarkBackgroundStyle(status, statusDisplay)}"
                                    status != null && statusDisplay is StatusDisplay.Stamp ->
                                        " ${stampBackgroundStyle(status, statusDisplay)}"
                                    else -> ""
                                }
                                attributes["style"] = "padding: 24px;$bgStyle"
                                if (status != null && statusDisplay.isBanner()) {
                                    renderStatusBanner(status, style)
                                    div { attributes["style"] = "height: 16px;" }
                                }
                                val sections = document.sections
                                val branding = sections.filterIsInstance<InvoiceSection.Header>().firstOrNull()?.branding
                                var i = 0
                                while (i < sections.size) {
                                    val section = sections[i]
                                    if (section is InvoiceSection.BillFrom && i + 1 < sections.size) {
                                        val next = sections[i + 1]
                                        if (next is InvoiceSection.BillTo) {
                                            renderPartiesSideBySide(section, next, style)
                                            // 16px spacer matching Compose's Spacer(lg)
                                            div { attributes["style"] = "height: 16px;" }
                                            i += 2
                                            continue
                                        }
                                    }
                                    renderSection(section, style, currency, branding, document.status, document.statusDisplay)
                                    // 16px spacer matching Compose's Spacer(lg)
                                    div { attributes["style"] = "height: 16px;" }
                                    i++
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun FlowContent.renderPartiesSideBySide(
        from: InvoiceSection.BillFrom,
        to: InvoiceSection.BillTo,
        style: InvoiceStyle,
    ) {
        table {
            attributes["width"] = "100%"
            attributes["cellpadding"] = "0"
            attributes["cellspacing"] = "0"
            tr {
                td {
                    attributes["style"] = "vertical-align: top; width: 50%;"
                    renderParty(from.contact, "From", style)
                }
                td {
                    attributes["style"] = "width: 16px;"
                }
                td {
                    attributes["style"] = "vertical-align: top; width: 50%;"
                    renderParty(to.contact, "Bill To", style)
                }
            }
        }
    }

    private fun FlowContent.renderSection(
        section: InvoiceSection,
        style: InvoiceStyle,
        currency: String,
        branding: Branding? = null,
        status: InvoiceStatus? = null,
        statusDisplay: StatusDisplay = StatusDisplay.Badge,
    ) {
        when (section) {
            is InvoiceSection.Header -> renderHeader(section, style, status, statusDisplay)
            is InvoiceSection.BillFrom -> renderParty(section.contact, "From", style)
            is InvoiceSection.BillTo -> renderParty(section.contact, "Bill To", style)
            is InvoiceSection.LineItems -> renderLineItems(section, style, currency)
            is InvoiceSection.Summary -> renderSummary(section, style, currency)
            is InvoiceSection.PaymentInfo -> renderPaymentInfo(section, style)
            is InvoiceSection.Footer -> renderFooter(section, style, branding)
            is InvoiceSection.Custom -> renderCustom(section, style)
            is InvoiceSection.MetaBlock -> renderMetaBlock(section, style)
        }
    }
}

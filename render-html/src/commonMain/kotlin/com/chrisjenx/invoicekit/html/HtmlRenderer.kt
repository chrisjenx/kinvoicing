package com.chrisjenx.invoicekit.html

import com.chrisjenx.invoicekit.*
import com.chrisjenx.invoicekit.html.sections.*
import kotlinx.html.*
import kotlinx.html.stream.appendHTML

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
    private val config: HtmlRenderConfig = HtmlRenderConfig(),
) : InvoiceRenderer<String> {

    override fun render(document: InvoiceDocument): String {
        val style = document.style
        val currency = document.currency

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
                    attributes["style"] = "margin: 0; padding: 0; background-color: ${style.backgroundColor.toHexColor()}; font-family: ${style.fontFamily}, Arial, sans-serif;"
                    table {
                        attributes["role"] = "presentation"
                        attributes["width"] = "100%"
                        attributes["cellpadding"] = "0"
                        attributes["cellspacing"] = "0"
                        attributes["style"] = "max-width: 600px; margin: 0 auto; background-color: #FFFFFF;"
                        tr {
                            td {
                                attributes["style"] = "padding: 24px;"
                                for (section in document.sections) {
                                    renderSection(section, style, currency)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun FlowContent.renderSection(
        section: InvoiceSection,
        style: InvoiceStyle,
        currency: String,
    ) {
        when (section) {
            is InvoiceSection.Header -> renderHeader(section, style)
            is InvoiceSection.BillFrom -> renderParty(section.name, section.address, section.email, section.phone, "From", style)
            is InvoiceSection.BillTo -> renderParty(section.name, section.address, section.email, section.phone, "Bill To", style)
            is InvoiceSection.LineItems -> renderLineItems(section, style, currency)
            is InvoiceSection.Summary -> renderSummary(section, style, currency)
            is InvoiceSection.PaymentInfo -> renderPaymentInfo(section, style)
            is InvoiceSection.Footer -> renderFooter(section, style)
            is InvoiceSection.Custom -> renderCustom(section, style)
            is InvoiceSection.MetaBlock -> renderMetaBlock(section, style)
        }
    }
}

package com.chrisjenx.invoicekit.html.sections

import com.chrisjenx.invoicekit.*
import com.chrisjenx.invoicekit.html.toHexColor
import com.chrisjenx.invoicekit.util.CurrencyFormatter
import com.chrisjenx.invoicekit.util.formatAsQuantity
import com.chrisjenx.invoicekit.util.labelWithPercent
import kotlinx.html.*

internal fun FlowContent.renderLineItems(
    lineItems: InvoiceSection.LineItems,
    style: InvoiceStyle,
    currency: String,
) {
    // Cache hex conversions to avoid repeated allocation in row loops
    val primaryHex = style.primaryColor.toHexColor()
    val secondaryHex = style.secondaryColor.toHexColor()
    val textHex = style.textColor.toHexColor()
    val negativeHex = InvoiceColors.NEGATIVE.toHexColor()
    val bgMutedHex = InvoiceColors.BG_MUTED.toHexColor()

    val borderStyle = if (style.accentBorder) "border-left: 4px solid $primaryHex;" else ""
    val cellBorder = if (style.showGridLines) "border: 1px solid ${InvoiceColors.BORDER.toHexColor()};" else "border-bottom: 1px solid ${InvoiceColors.DIVIDER.toHexColor()};"

    table {
        attributes["width"] = "100%"
        attributes["cellpadding"] = "8"
        attributes["cellspacing"] = "0"
        attributes["style"] = "margin-bottom: 24px; border-collapse: collapse; $borderStyle"

        thead {
            tr {
                lineItems.columnHeaders.forEach { header ->
                    th {
                        attributes["style"] = "text-align: left; font-size: 12px; font-weight: bold; text-transform: uppercase; color: $secondaryHex; background-color: ${primaryHex}10; padding: 10px 8px; $cellBorder"
                        +header
                    }
                }
            }
        }

        tbody {
            lineItems.rows.forEachIndexed { rowIdx, item ->
                val bgColor = if (rowIdx % 2 == 1) "background-color: $bgMutedHex;" else ""
                tr {
                    td {
                        attributes["style"] = "font-size: 14px; color: $textHex; padding: 10px 8px; $cellBorder $bgColor"
                        +item.description
                    }
                    if (lineItems.columnHeaders.size >= 3) {
                        td {
                            attributes["style"] = "font-size: 14px; color: $textHex; padding: 10px 8px; $cellBorder $bgColor"
                            +(item.quantity?.formatAsQuantity() ?: "")
                        }
                    }
                    if (lineItems.columnHeaders.size >= 4) {
                        td {
                            attributes["style"] = "font-size: 14px; color: $textHex; padding: 10px 8px; $cellBorder $bgColor"
                            +(item.unitPrice?.let { CurrencyFormatter.format(it, currency) } ?: "")
                        }
                    }
                    td {
                        val amountColor = if (item.amount < 0) negativeHex else textHex
                        attributes["style"] = "font-size: 14px; color: $amountColor; text-align: right; padding: 10px 8px; $cellBorder $bgColor"
                        +CurrencyFormatter.format(item.amount, currency)
                    }
                }

                item.subItems.forEach { sub ->
                    tr {
                        td {
                            attributes["style"] = "font-size: 12px; color: $secondaryHex; padding: 4px 8px 4px 24px; $cellBorder"
                            +sub.description
                        }
                        if (lineItems.columnHeaders.size >= 3) {
                            td {
                                attributes["style"] = "font-size: 12px; color: $secondaryHex; padding: 4px 8px; $cellBorder"
                                +(sub.quantity?.formatAsQuantity() ?: "")
                            }
                        }
                        if (lineItems.columnHeaders.size >= 4) {
                            td {
                                attributes["style"] = "font-size: 12px; color: $secondaryHex; padding: 4px 8px; $cellBorder"
                                +(sub.unitPrice?.let { CurrencyFormatter.format(it, currency) } ?: "")
                            }
                        }
                        td {
                            attributes["style"] = "font-size: 12px; color: $secondaryHex; text-align: right; padding: 4px 8px; $cellBorder"
                            +CurrencyFormatter.format(sub.amount, currency)
                        }
                    }
                }

                item.discounts.forEach { disc ->
                    tr {
                        td {
                            attributes["colspan"] = (lineItems.columnHeaders.size - 1).toString()
                            attributes["style"] = "font-size: 12px; color: $negativeHex; padding: 2px 8px 2px 24px; $cellBorder"
                            +disc.labelWithPercent
                        }
                        td {
                            attributes["style"] = "font-size: 12px; color: $negativeHex; text-align: right; padding: 2px 8px; $cellBorder"
                        }
                    }
                }
            }
        }
    }
}

package com.chrisjenx.kinvoicing.html.sections

import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.html.toHexColor
import com.chrisjenx.kinvoicing.util.CurrencyFormatter
import com.chrisjenx.kinvoicing.util.formatAsQuantity
import com.chrisjenx.kinvoicing.util.labelWithPercent
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
    val negativeHex = style.negativeColor.toHexColor()
    val bgMutedHex = style.mutedBackgroundColor.toHexColor()

    val borderStyle = if (style.accentBorder) "border-left: 4px solid $primaryHex;" else ""
    val cellBorder = if (style.showGridLines) "border: 1px solid ${style.borderColor.toHexColor()};" else "border-bottom: 1px solid ${style.dividerColor.toHexColor()};"

    table {
        attributes["width"] = "100%"
        attributes["cellpadding"] = "8"
        attributes["cellspacing"] = "0"
        attributes["style"] = "margin-bottom: 24px; border-collapse: collapse; $borderStyle"

        thead {
            tr {
                lineItems.columns.forEach { col ->
                    th {
                        val align = if (col.column == LineItemColumn.AMOUNT) "text-align: right;" else "text-align: left;"
                        attributes["style"] = "$align font-size: 12px; font-weight: bold; text-transform: uppercase; color: $secondaryHex; background-color: ${primaryHex}10; padding: 10px 8px; $cellBorder"
                        +col.label
                    }
                }
            }
        }

        tbody {
            lineItems.rows.forEachIndexed { rowIdx, item ->
                val bgColor = if (rowIdx % 2 == 1) "background-color: $bgMutedHex;" else ""
                tr {
                    lineItems.columns.forEach { col ->
                        td {
                            val text = when (col.column) {
                                LineItemColumn.DESCRIPTION -> item.description
                                LineItemColumn.QUANTITY -> item.quantity?.formatAsQuantity() ?: ""
                                LineItemColumn.UNIT_PRICE -> item.unitPrice?.let { CurrencyFormatter.format(it, currency) } ?: ""
                                LineItemColumn.AMOUNT -> CurrencyFormatter.format(item.amount, currency)
                            }
                            val amountColor = if (col.column == LineItemColumn.AMOUNT && item.amount < 0) negativeHex else textHex
                            val align = if (col.column == LineItemColumn.AMOUNT) "text-align: right; " else ""
                            attributes["style"] = "font-size: 14px; color: $amountColor; ${align}padding: 10px 8px; $cellBorder $bgColor"
                            +text
                        }
                    }
                }

                item.subItems.forEach { sub ->
                    tr {
                        lineItems.columns.forEach { col ->
                            td {
                                val text = when (col.column) {
                                    LineItemColumn.DESCRIPTION -> sub.description
                                    LineItemColumn.QUANTITY -> sub.quantity?.formatAsQuantity() ?: ""
                                    LineItemColumn.UNIT_PRICE -> sub.unitPrice?.let { CurrencyFormatter.format(it, currency) } ?: ""
                                    LineItemColumn.AMOUNT -> CurrencyFormatter.format(sub.amount, currency)
                                }
                                val padding = if (col.column == LineItemColumn.DESCRIPTION) "padding: 4px 8px 4px 24px;" else "padding: 4px 8px;"
                                val align = if (col.column == LineItemColumn.AMOUNT) "text-align: right; " else ""
                                attributes["style"] = "font-size: 12px; color: $secondaryHex; $align$padding $cellBorder"
                                +text
                            }
                        }
                    }
                }

                item.discounts.forEach { disc ->
                    tr {
                        td {
                            attributes["colspan"] = (lineItems.columns.size - 1).toString()
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

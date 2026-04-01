package com.chrisjenx.kinvoicing.html.email.sections

import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.util.CurrencyFormatter
import com.chrisjenx.kinvoicing.util.displayAmount
import com.chrisjenx.kinvoicing.util.labelWithPercent
import kotlinx.html.*

internal fun FlowContent.renderSummary(
    summary: InvoiceSection.Summary,
    style: InvoiceStyle,
    currency: String,
) {
    table {
        attributes["width"] = "100%"
        attributes["cellpadding"] = "0"
        attributes["cellspacing"] = "0"
        attributes["style"] = "margin-bottom: 24px;"
        tr {
            td { attributes["width"] = "60%" }
            td {
                attributes["width"] = "40%"
                table {
                    attributes["width"] = "100%"
                    attributes["cellpadding"] = "6"
                    attributes["cellspacing"] = "0"
                    attributes["style"] = "border-collapse: collapse;"

                    tr {
                        td { attributes["style"] = "font-size: 13px; color: ${style.secondaryColor.toHexColor()};" ; +"Subtotal" }
                        td { attributes["style"] = "font-size: 13px; color: ${style.textColor.toHexColor()}; text-align: right;" ; +CurrencyFormatter.format(summary.subtotal, currency) }
                    }

                    summary.adjustments.forEach { adj ->
                        tr {
                            td {
                                val color = when (adj.type) {
                                    AdjustmentType.DISCOUNT, AdjustmentType.CREDIT -> style.negativeColor.toHexColor()
                                    else -> style.secondaryColor.toHexColor()
                                }
                                attributes["style"] = "font-size: 13px; color: $color;"
                                +adj.labelWithPercent
                            }
                            td {
                                val adjAmount = adj.displayAmount(summary.subtotal)
                                val color = if (adjAmount < 0) style.negativeColor.toHexColor() else style.textColor.toHexColor()
                                attributes["style"] = "font-size: 13px; color: $color; text-align: right;"
                                +CurrencyFormatter.format(adjAmount, currency)
                            }
                        }
                    }

                    tr {
                        td { attributes["style"] = "font-size: 20px; font-weight: bold; color: ${style.textColor.toHexColor()}; border-top: 3px solid ${style.primaryColor.toHexColor()}; padding-top: 8px;" ; +"Total" }
                        td { attributes["style"] = "font-size: 20px; font-weight: bold; color: ${style.primaryColor.toHexColor()}; text-align: right; border-top: 3px solid ${style.primaryColor.toHexColor()}; padding-top: 8px;" ; +CurrencyFormatter.format(summary.total, currency) }
                    }
                }
            }
        }
    }
}

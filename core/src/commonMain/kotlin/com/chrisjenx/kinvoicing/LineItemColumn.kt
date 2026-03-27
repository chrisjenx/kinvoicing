package com.chrisjenx.kinvoicing

import com.chrisjenx.kinvoicing.util.CurrencyFormatter
import com.chrisjenx.kinvoicing.util.formatAsQuantity

/** Standard columns available for line item tables. */
public enum class LineItemColumn {
    DESCRIPTION,
    QUANTITY,
    UNIT_PRICE,
    AMOUNT,
}

/** A column in the line items table, mapping a standard field to a display label. */
public data class ColumnHeader(
    val column: LineItemColumn,
    val label: String,
)

/**
 * Resolves the display text for a [LineItemColumn] given the line item field values.
 * Shared by Compose and HTML renderers to avoid duplicating the column→field mapping.
 */
public fun LineItemColumn.textFor(
    description: String,
    quantity: Double?,
    unitPrice: Double?,
    amount: Double,
    currency: String,
): String = when (this) {
    LineItemColumn.DESCRIPTION -> description
    LineItemColumn.QUANTITY -> quantity?.formatAsQuantity() ?: ""
    LineItemColumn.UNIT_PRICE -> unitPrice?.let { CurrencyFormatter.format(it, currency) } ?: ""
    LineItemColumn.AMOUNT -> CurrencyFormatter.format(amount, currency)
}

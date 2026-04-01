package com.chrisjenx.kinvoicing

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

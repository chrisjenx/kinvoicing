package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.util.roundToScale

/** Builder for the line items section, collecting items and typed column descriptors. */
@InvoiceDsl
public class LineItemsBuilder {
    private var columns: List<ColumnHeader> = defaultColumns
    private val items: MutableList<LineItem> = mutableListOf()

    /** Set columns with typed descriptors for type-safe column-to-field mapping. */
    public fun columns(vararg cols: ColumnHeader) {
        columns = cols.toList()
    }

    /** Set column labels for standard layouts. Maps positionally to [LineItemColumn] types. */
    public fun columns(vararg headers: String) {
        columns = mapToColumnHeaders(headers.toList())
    }

    /**
     * Add a line item. Amount is resolved in priority order:
     * 1. Explicit [amount] if provided
     * 2. [qty] × [unitPrice] if both provided
     * 3. Sum of sub-items if any
     * 4. Zero
     *
     * Use the [init] lambda to add sub-items and item-level discounts.
     */
    public fun item(
        description: String,
        qty: Number? = null,
        unitPrice: Double? = null,
        amount: Double? = null,
        metadata: Map<String, String> = emptyMap(),
        init: (LineItemBuilder.() -> Unit)? = null,
    ) {
        val builder = LineItemBuilder(description, qty?.toDouble(), unitPrice, amount, metadata)
        init?.invoke(builder)
        items.add(builder.build())
    }

    internal fun build(): InvoiceSection.LineItems = InvoiceSection.LineItems(
        columns = columns,
        rows = items.toList(),
    )

    internal companion object {
        val defaultColumns = listOf(
            ColumnHeader(LineItemColumn.DESCRIPTION, "Description"),
            ColumnHeader(LineItemColumn.QUANTITY, "Qty"),
            ColumnHeader(LineItemColumn.UNIT_PRICE, "Rate"),
            ColumnHeader(LineItemColumn.AMOUNT, "Amount"),
        )

        fun mapToColumnHeaders(headers: List<String>): List<ColumnHeader> {
            val columnTypes = when (headers.size) {
                2 -> listOf(LineItemColumn.DESCRIPTION, LineItemColumn.AMOUNT)
                3 -> listOf(LineItemColumn.DESCRIPTION, LineItemColumn.QUANTITY, LineItemColumn.AMOUNT)
                4 -> listOf(LineItemColumn.DESCRIPTION, LineItemColumn.QUANTITY, LineItemColumn.UNIT_PRICE, LineItemColumn.AMOUNT)
                else -> headers.indices.map { i ->
                    when {
                        i == 0 -> LineItemColumn.DESCRIPTION
                        i == headers.lastIndex -> LineItemColumn.AMOUNT
                        i == 1 -> LineItemColumn.QUANTITY
                        i == 2 -> LineItemColumn.UNIT_PRICE
                        else -> LineItemColumn.DESCRIPTION
                    }
                }
            }
            return headers.zip(columnTypes) { label, col -> ColumnHeader(col, label) }
        }
    }
}

/** Builder scope for a single line item — add sub-items and discounts here. */
@InvoiceDsl
public class LineItemBuilder internal constructor(
    private val description: String,
    private val quantity: Double?,
    private val unitPrice: Double?,
    private val explicitAmount: Double?,
    private val metadata: Map<String, String>,
) {
    private val subItems: MutableList<LineSubItem> = mutableListOf()
    private val discounts: MutableList<Adjustment> = mutableListOf()

    /** Add a nested sub-item (rendered indented under the parent). */
    public fun sub(
        description: String,
        qty: Number? = null,
        unitPrice: Double? = null,
        amount: Double? = null,
        metadata: Map<String, String> = emptyMap(),
    ) {
        val subAmount = amount ?: run {
            val q = qty?.toDouble()
            val p = unitPrice
            if (q != null && p != null) q * p else 0.0
        }
        subItems.add(
            LineSubItem(
                description = description,
                quantity = qty?.toDouble(),
                unitPrice = unitPrice,
                amount = subAmount,
                metadata = metadata,
            )
        )
    }

    /** Add an item-level discount, applied before the item rolls into the subtotal. */
    public fun discount(label: String, percent: Double? = null, fixed: Double? = null) {
        discounts.add(
            Adjustment(
                label = label,
                type = AdjustmentType.DISCOUNT,
                value = buildAdjustmentValue(percent, fixed, "Discount"),
            )
        )
    }

    internal fun build(): LineItem {
        require(explicitAmount != null || (quantity != null && unitPrice != null) || subItems.isNotEmpty()) {
            "Line item '$description' must specify amount, quantity+unitPrice, or sub-items"
        }

        // Calculate amount: explicit > qty*unitPrice > sum of subItems
        val computedAmount = explicitAmount ?: run {
            val q = quantity
            val p = unitPrice
            if (q != null && p != null) (q * p).roundToScale()
            else subItems.sumOf { it.amount }.roundToScale()
        }

        // Apply item-level discounts to get effective amount
        val effectiveAmount = discounts.fold(computedAmount) { acc, adj ->
            val delta = adj.value.applyTo(acc)
            when (adj.value) {
                is AdjustmentValue.Percent -> acc - delta
                is AdjustmentValue.Fixed -> acc + delta
                is AdjustmentValue.Absolute -> delta
            }.roundToScale()
        }

        return LineItem(
            description = description,
            quantity = quantity,
            unitPrice = unitPrice,
            amount = effectiveAmount,
            metadata = metadata,
            subItems = subItems.toList(),
            discounts = discounts.toList(),
        )
    }
}

/**
 * Shared helper to build an [AdjustmentValue] from either a percent or fixed amount.
 * Exactly one must be non-null.
 */
internal fun buildAdjustmentValue(percent: Double?, fixed: Double?, label: String): AdjustmentValue = when {
    percent != null -> AdjustmentValue.Percent(percent)
    fixed != null -> AdjustmentValue.Fixed(fixed)
    else -> error("$label must specify either percent or fixed amount")
}

/** Validates that [value] is a 3-letter uppercase ISO 4217 currency code. */
internal fun requireValidCurrencyCode(value: String) {
    require(value.length == 3 && value.all { it.isUpperCase() }) {
        "Currency code must be a 3-letter uppercase code (e.g., 'USD'), got: '$value'"
    }
}

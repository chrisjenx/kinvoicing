package com.chrisjenx.invoicekit.builders

import com.chrisjenx.invoicekit.*

/** Builder for the line items section, collecting items and column headers. */
@InvoiceDsl
public class LineItemsBuilder {
    private var columnHeaders: List<String> = listOf("Description", "Qty", "Rate", "Amount")
    private val items: MutableList<LineItem> = mutableListOf()

    /** Set the column headers displayed in the line items table. */
    public fun columns(vararg headers: String) {
        columnHeaders = headers.toList()
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
        columnHeaders = columnHeaders,
        rows = items.toList(),
    )
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
    private val subItems: MutableList<SubItem> = mutableListOf()
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
            SubItem(
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
        // Calculate amount: explicit > qty*unitPrice > sum of subItems
        val computedAmount = explicitAmount ?: run {
            val q = quantity
            val p = unitPrice
            if (q != null && p != null) q * p
            else if (subItems.isNotEmpty()) subItems.sumOf { it.amount }
            else 0.0
        }

        // Apply item-level discounts to get effective amount
        val effectiveAmount = discounts.fold(computedAmount) { acc, adj ->
            val delta = adj.value.applyTo(acc)
            when (adj.value) {
                is AdjustmentValue.Percent -> acc - delta
                is AdjustmentValue.Fixed -> acc + delta
                is AdjustmentValue.Absolute -> delta
            }
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

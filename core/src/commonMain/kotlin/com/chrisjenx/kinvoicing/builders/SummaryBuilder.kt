package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.util.roundToScale

/**
 * Builder for the summary section. Collects adjustments (discounts, taxes, fees, credits)
 * and auto-computes subtotal and total from line items.
 *
 * Adjustment application order: discounts → credits → fees → taxes.
 */
@InvoiceDsl
public class SummaryBuilder {
    private var currency: String? = null
    private val adjustments: MutableList<Adjustment> = mutableListOf()

    /** Set the currency code (e.g., "USD", "EUR", "GBP"). Must be a 3-letter uppercase code. */
    public fun currency(value: String) {
        require(value.length == 3 && value.all { it.isUpperCase() }) {
            "Currency code must be a 3-letter uppercase code (e.g., 'USD'), got: '$value'"
        }
        currency = value
    }

    /** Add a discount adjustment (reduces the total). */
    public fun discount(label: String, percent: Double? = null, fixed: Double? = null) {
        adjustments.add(Adjustment(label, AdjustmentType.DISCOUNT, buildAdjustmentValue(percent, fixed, "Discount")))
    }

    /** Add a tax adjustment (increases the total). */
    public fun tax(label: String, percent: Double? = null, fixed: Double? = null) {
        adjustments.add(Adjustment(label, AdjustmentType.TAX, buildAdjustmentValue(percent, fixed, "Tax")))
    }

    /** Add a fee adjustment (increases the total). */
    public fun fee(label: String, percent: Double? = null, fixed: Double? = null) {
        adjustments.add(Adjustment(label, AdjustmentType.FEE, buildAdjustmentValue(percent, fixed, "Fee")))
    }

    /** Add a credit (always reduces the total by the given positive [amount]). */
    public fun credit(label: String, amount: Double) {
        adjustments.add(Adjustment(label, AdjustmentType.CREDIT, AdjustmentValue.Fixed(-kotlin.math.abs(amount))))
    }

    /**
     * Build the summary, auto-computing subtotal from [lineItems] and total from adjustments.
     * Adjustments are sorted and applied: discounts → credits → fees → taxes.
     */
    internal fun build(lineItems: List<LineItem>, fallbackCurrency: String = "USD"): InvoiceSection.Summary {
        val subtotal = lineItems.sumOf { it.amount }

        val sortedAdjustments = adjustments.sortedBy { adj ->
            when (adj.type) {
                AdjustmentType.DISCOUNT -> 0
                AdjustmentType.CREDIT -> 1
                AdjustmentType.FEE -> 2
                AdjustmentType.TAX -> 3
                AdjustmentType.CUSTOM -> 4
            }
        }

        val total = sortedAdjustments.fold(subtotal) { acc, adj ->
            val delta = adj.value.applyTo(acc)
            when (adj.value) {
                is AdjustmentValue.Percent -> when (adj.type) {
                    AdjustmentType.DISCOUNT, AdjustmentType.CREDIT -> acc - delta
                    AdjustmentType.TAX, AdjustmentType.FEE, AdjustmentType.CUSTOM -> acc + delta
                }
                is AdjustmentValue.Fixed -> acc + delta
                is AdjustmentValue.Absolute -> delta
            }.roundToScale()
        }

        return InvoiceSection.Summary(
            subtotal = subtotal,
            adjustments = sortedAdjustments,
            total = total,
            currency = currency ?: fallbackCurrency,
        )
    }
}

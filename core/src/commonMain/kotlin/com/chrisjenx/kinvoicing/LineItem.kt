package com.chrisjenx.kinvoicing

/**
 * A single billable line item in the invoice.
 *
 * **Note on precision:** Monetary values use [Double] for Kotlin Multiplatform compatibility
 * (no `BigDecimal` in commonMain). This is sufficient for display-oriented invoicing but
 * may exhibit IEEE 754 rounding. For financial-grade arithmetic, compute amounts externally
 * and pass pre-rounded values.
 *
 * @property metadata Arbitrary key-value pairs rendered alongside the item (e.g., SKU, date range).
 * @property subItems Nested detail rows displayed indented under this item.
 * @property discounts Item-level adjustments applied before the item rolls into the subtotal.
 */
public data class LineItem(
    val description: String,
    val quantity: Double? = null,
    val unitPrice: Double? = null,
    val amount: Double,
    val metadata: Map<String, String> = emptyMap(),
    val subItems: List<SubItem> = emptyList(),
    val discounts: List<Adjustment> = emptyList(),
)

/** A detail row nested under a [LineItem], typically rendered indented. */
public data class SubItem(
    val description: String,
    val quantity: Double? = null,
    val unitPrice: Double? = null,
    val amount: Double,
    val metadata: Map<String, String> = emptyMap(),
)

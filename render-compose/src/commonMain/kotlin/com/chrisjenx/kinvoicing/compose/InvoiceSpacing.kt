package com.chrisjenx.kinvoicing.compose

import androidx.compose.ui.unit.dp

/** Consistent spacing rhythm (4dp base) for invoice rendering. */
internal object InvoiceSpacing {
    /** Tight — within same element. */
    val xxs = 2.dp

    /** Compact — label to value. */
    val xs = 4.dp

    /** Between related items. */
    val sm = 8.dp

    /** Between sub-sections. */
    val md = 12.dp

    /** Between sections. */
    val lg = 16.dp

    /** Page padding, major separators. */
    val xl = 24.dp
}

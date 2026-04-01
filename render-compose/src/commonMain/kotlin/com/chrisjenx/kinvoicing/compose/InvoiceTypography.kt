package com.chrisjenx.kinvoicing.compose

import androidx.compose.ui.unit.sp

/** Consistent type scale for invoice rendering. */
internal object InvoiceTypography {
    /** Invoice number, total amount. */
    val titleLarge = 20.sp

    /** Company/brand name. */
    val titleMedium = 18.sp

    /** Section titles, total label. */
    val titleSmall = 16.sp

    /** Line item text, party names, payment link. */
    val bodyLarge = 14.sp

    /** Dates, addresses, summary rows, payment details, section labels. */
    val bodyMedium = 13.sp

    /** Sub-items, discounts, fine print, terms. */
    val bodySmall = 12.sp

    /** Section labels (FROM, BILL TO), powered-by text. */
    val caption = 11.sp
}

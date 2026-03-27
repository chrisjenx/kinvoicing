package com.chrisjenx.invoicekit.compose

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chrisjenx.invoicekit.InvoiceDocument

/**
 * Public composable for interactive invoice preview with scrolling.
 */
@Composable
public fun InvoiceView(
    document: InvoiceDocument,
    modifier: Modifier = Modifier,
) {
    InvoiceContent(
        document = document,
        modifier = modifier.verticalScroll(rememberScrollState()),
    )
}

package com.chrisjenx.invoicekit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chrisjenx.invoicekit.*
import com.chrisjenx.invoicekit.compose.sections.*

/**
 * Shared composable that renders a full invoice document.
 * Used by both the interactive preview ([InvoiceView]) and PdfRenderer.
 *
 * Each section is a direct child of the Column, enabling compose2pdf's
 * auto-pagination to keep sections together at page boundaries.
 */
@Composable
public fun InvoiceContent(
    document: InvoiceDocument,
    modifier: Modifier = Modifier,
) {
    val style = document.style
    val currency = document.currency

    InvoiceStyleProvider(style) {
        Column(
            modifier = modifier
                .background(style.backgroundComposeColor)
                .padding(24.dp)
        ) {
            for (section in document.sections) {
                when (section) {
                    is InvoiceSection.Header -> HeaderSection(section)
                    is InvoiceSection.BillFrom -> PartySection(section.name, section.address, section.email, section.phone, "From")
                    is InvoiceSection.BillTo -> PartySection(section.name, section.address, section.email, section.phone, "Bill To")
                    is InvoiceSection.LineItems -> LineItemsSection(section, currency)
                    is InvoiceSection.Summary -> SummarySection(section, currency)
                    is InvoiceSection.PaymentInfo -> PaymentInfoSection(section)
                    is InvoiceSection.Footer -> FooterSection(section)
                    is InvoiceSection.Custom -> CustomSection(section)
                    is InvoiceSection.MetaBlock -> MetaBlockSection(section)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

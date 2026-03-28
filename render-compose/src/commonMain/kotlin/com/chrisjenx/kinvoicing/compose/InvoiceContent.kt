package com.chrisjenx.kinvoicing.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.compose.sections.*

/**
 * Shared composable that renders a full invoice document in a Column.
 * Used by the interactive preview ([InvoiceView]) and as a single-page layout.
 *
 * For PDF rendering with auto-pagination, use [InvoiceSectionContent] directly
 * so each section is a direct child of compose2pdf's content lambda.
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
                InvoiceSectionContent(section, currency)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Renders a single [InvoiceSection] to its corresponding composable.
 *
 * Call this per-section inside compose2pdf's `renderToPdf` lambda so each
 * section is a direct child — enabling auto-pagination to keep sections
 * together and split between them at page boundaries.
 */
@Composable
public fun InvoiceSectionContent(section: InvoiceSection, currency: String) {
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
}

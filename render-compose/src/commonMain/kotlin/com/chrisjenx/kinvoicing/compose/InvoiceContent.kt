package com.chrisjenx.kinvoicing.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        CompositionLocalProvider(
            LocalInvoiceStatus provides document.status,
            LocalStatusDisplay provides document.statusDisplay,
        ) {
            Box(
                modifier = modifier
                    .background(style.backgroundComposeColor),
            ) {
                Column(
                    modifier = Modifier.padding(InvoiceSpacing.xl),
                ) {
                    // Banner renders before sections
                    if (document.status != null && document.statusDisplay is StatusDisplay.Banner) {
                        StatusBanner(document.status!!, style)
                        Spacer(modifier = Modifier.height(InvoiceSpacing.lg))
                    }
                    InvoiceSections(document.sections, currency)
                }
                // Watermark/Stamp overlay on top of content
                if (document.status != null) {
                    when (val display = document.statusDisplay) {
                        is StatusDisplay.Watermark -> StatusWatermark(document.status!!, display)
                        is StatusDisplay.Stamp -> StatusStamp(document.status!!, display)
                        else -> {} // Badge/Banner/None handled elsewhere
                    }
                }
            }
        }
    }
}

/**
 * Renders all [sections] with intelligent grouping (e.g., adjacent BillFrom + BillTo
 * rendered side-by-side). Each logical group is emitted as a direct child, making this
 * suitable for compose2pdf's auto-pagination.
 */
@Composable
public fun InvoiceSections(sections: List<InvoiceSection>, currency: String) {
    val branding = sections.filterIsInstance<InvoiceSection.Header>().firstOrNull()?.branding
    PdfColumn(modifier = Modifier.fillMaxWidth()) {
        var i = 0
        while (i < sections.size) {
            val section = sections[i]
            if (section is InvoiceSection.BillFrom && i + 1 < sections.size) {
                val next = sections[i + 1]
                if (next is InvoiceSection.BillTo) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            PartySection(section.contact, "From")
                        }
                        Spacer(modifier = Modifier.width(InvoiceSpacing.lg))
                        Box(modifier = Modifier.weight(1f)) {
                            PartySection(next.contact, "Bill To")
                        }
                    }
                    Spacer(modifier = Modifier.height(InvoiceSpacing.lg))
                    i += 2
                    continue
                }
            }
            InvoiceSectionContent(section, currency, branding)
            Spacer(modifier = Modifier.height(InvoiceSpacing.lg))
            i++
        }
    }
}

/**
 * Renders a single [InvoiceSection] to its corresponding composable.
 *
 * For grouped rendering with side-by-side parties, use [InvoiceSections] instead.
 */
@Composable
public fun InvoiceSectionContent(
    section: InvoiceSection,
    currency: String,
    branding: Branding? = null,
) {
    when (section) {
        is InvoiceSection.Header -> HeaderSection(section)
        is InvoiceSection.BillFrom -> PartySection(section.contact, "From")
        is InvoiceSection.BillTo -> PartySection(section.contact, "Bill To")
        is InvoiceSection.LineItems -> LineItemsSection(section, currency)
        is InvoiceSection.Summary -> SummarySection(section, currency)
        is InvoiceSection.PaymentInfo -> PaymentInfoSection(section)
        is InvoiceSection.Footer -> FooterSection(section, branding)
        is InvoiceSection.Custom -> CustomSection(section)
        is InvoiceSection.MetaBlock -> MetaBlockSection(section)
    }
}

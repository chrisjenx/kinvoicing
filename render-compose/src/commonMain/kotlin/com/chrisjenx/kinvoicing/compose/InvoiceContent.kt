package com.chrisjenx.kinvoicing.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
        Column(
            modifier = modifier
                .background(style.backgroundComposeColor)
                .padding(InvoiceSpacing.xl),
        ) {
            InvoiceSections(document)
        }
    }
}

/**
 * Renders a full [InvoiceDocument] with status visuals (banner, watermark, stamp, badge).
 *
 * Banner is rendered as the first child inside PdfColumn for proper layout flow.
 * Watermark and Stamp are rendered as Box overlays on top of all sections.
 * Badge is provided via [LocalInvoiceStatus] and rendered by [HeaderSection].
 *
 * Suitable for compose2pdf's auto-pagination — each section is a direct child
 * of PdfColumn via [LocalPdfColumn].
 */
@Composable
public fun InvoiceSections(document: InvoiceDocument) {
    val status = document.status
    val display = document.statusDisplay

    CompositionLocalProvider(
        LocalInvoiceStatus provides status,
        LocalStatusDisplay provides display,
    ) {
        InvoiceSectionsColumn(document)
    }
}

/**
 * Renders all [sections] with intelligent grouping (e.g., adjacent BillFrom + BillTo
 * rendered side-by-side). Each logical group is emitted as a direct child, making this
 * suitable for compose2pdf's auto-pagination.
 */
@Composable
public fun InvoiceSections(sections: List<InvoiceSection>, currency: String) {
    InvoiceSectionsColumn(sections, currency)
}

@Composable
private fun InvoiceSectionsColumn(document: InvoiceDocument) {
    val status = document.status
    val display = document.statusDisplay
    PdfColumn(modifier = Modifier.fillMaxWidth()) {
        // Banner / Watermark / Stamp as first child in layout flow
        if (status != null) {
            when (display) {
                is StatusDisplay.Banner -> {
                    StatusBanner(status, document.style)
                    Spacer(modifier = Modifier.height(InvoiceSpacing.lg))
                }
                is StatusDisplay.Watermark -> {
                    StatusWatermark(status, display)
                    Spacer(modifier = Modifier.height(InvoiceSpacing.lg))
                }
                is StatusDisplay.Stamp -> {
                    StatusStamp(status, display)
                    Spacer(modifier = Modifier.height(InvoiceSpacing.lg))
                }
                else -> {} // Badge/None handled in header or not at all
            }
        }
        InvoiceSectionsContent(document.sections, document.currency)
    }
}

@Composable
private fun InvoiceSectionsColumn(sections: List<InvoiceSection>, currency: String) {
    PdfColumn(modifier = Modifier.fillMaxWidth()) {
        InvoiceSectionsContent(sections, currency)
    }
}

@Composable
private fun InvoiceSectionsContent(sections: List<InvoiceSection>, currency: String) {
    val branding = sections.filterIsInstance<InvoiceSection.Header>().firstOrNull()?.branding
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

package com.chrisjenx.kinvoicing.fidelity.compose

import androidx.compose.runtime.Composable
import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.kinvoicing.examples.InvoiceExamples
import com.chrisjenx.kinvoicing.compose.InvoiceContent

data class Fixture(
    val name: String,
    val category: String,
    val description: String,
    val vectorThreshold: Double,
    val htmlThreshold: Double,
    val config: PdfPageConfig = PdfPageConfig.A4,
    val content: @Composable () -> Unit,
)

val fidelityFixtures: List<Fixture> = listOf(
    // ── basics ──
    Fixture(
        name = "BasicInvoice",
        category = "basic",
        description = "Standard invoice: header, parties, line items, summary, footer",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.basic)
    },
    Fixture(
        name = "MinimalInvoice",
        category = "basic",
        description = "Minimum viable invoice: header + one item + total",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.minimal)
    },

    // ── line items ──
    Fixture(
        name = "SubItems",
        category = "line-items",
        description = "Nested sub-items with item-level discounts",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.subItems)
    },
    Fixture(
        name = "CreditsNegatives",
        category = "line-items",
        description = "Credit memo with negative amounts",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.creditsAndNegatives)
    },

    // ── adjustments ──
    Fixture(
        name = "AllAdjustments",
        category = "adjustments",
        description = "Discount, tax, fee, and credit at summary level",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.adjustments)
    },

    // ── branding ──
    Fixture(
        name = "DualBranding",
        category = "branding",
        description = "Powered-by footer with two brand identities",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.dualBranding)
    },

    // ── sections ──
    Fixture(
        name = "Metadata",
        category = "sections",
        description = "Rich metadata block (PO, project, contract)",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.metadata)
    },
    Fixture(
        name = "PaymentInfo",
        category = "sections",
        description = "Bank details and payment link",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.paymentInfo)
    },
    Fixture(
        name = "CustomSections",
        category = "sections",
        description = "Custom content sections with text, dividers, rows",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.customSections)
    },

    // ── styles ──
    Fixture(
        name = "StyledRed",
        category = "style",
        description = "Red theme with accent border and stacked layout",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.styledRed)
    },
    Fixture(
        name = "StyledStacked",
        category = "style",
        description = "Green theme with stacked header, right logo placement",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.styledStacked)
    },
    Fixture(
        name = "GridLines",
        category = "style",
        description = "Grid lines and accent border enabled",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.gridLines)
    },

    // ── composite ──
    Fixture(
        name = "FullFeatured",
        category = "composite",
        description = "Every feature: dual branding, sub-items, adjustments, metadata, payment",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.fullFeatured)
    },

    // ── stress ──
    Fixture(
        name = "ManyItems",
        category = "stress",
        description = "30 line items to test dense layout",
        vectorThreshold = 0.30,
        htmlThreshold = 0.35,
    ) {
        InvoiceContent(InvoiceExamples.manyItems)
    },
)

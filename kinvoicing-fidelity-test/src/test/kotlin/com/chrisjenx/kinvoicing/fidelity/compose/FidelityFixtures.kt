package com.chrisjenx.kinvoicing.fidelity.compose

import androidx.compose.runtime.Composable
import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.kinvoicing.examples.InvoiceExamples
import com.chrisjenx.kinvoicing.compose.InvoiceContent

data class Fixture(
    val name: String,
    val category: String,
    val description: String,
    val vectorThreshold: Double = 0.30,
    val htmlThreshold: Double = 0.35,
    val config: PdfPageConfig = PdfPageConfig.A4,
    val content: @Composable () -> Unit,
)

val fidelityFixtures: List<Fixture> = listOf(
    Fixture("BasicInvoice", "basic", "Standard invoice: header, parties, line items, summary, footer") { InvoiceContent(InvoiceExamples.basic) },
    Fixture("MinimalInvoice", "basic", "Minimum viable invoice: header + one item + total") { InvoiceContent(InvoiceExamples.minimal) },
    Fixture("SubItems", "line-items", "Nested sub-items with item-level discounts") { InvoiceContent(InvoiceExamples.subItems) },
    Fixture("CreditsNegatives", "line-items", "Credit memo with negative amounts") { InvoiceContent(InvoiceExamples.creditsAndNegatives) },
    Fixture("AllAdjustments", "adjustments", "Discount, tax, fee, and credit at summary level") { InvoiceContent(InvoiceExamples.adjustments) },
    Fixture("DualBranding", "branding", "Powered-by footer with two brand identities") { InvoiceContent(InvoiceExamples.dualBranding) },
    Fixture("Metadata", "sections", "Rich metadata block (PO, project, contract)") { InvoiceContent(InvoiceExamples.metadata) },
    Fixture("PaymentInfo", "sections", "Bank details and payment link") { InvoiceContent(InvoiceExamples.paymentInfo) },
    Fixture("CustomSections", "sections", "Custom content sections with text, dividers, rows") { InvoiceContent(InvoiceExamples.customSections) },
    Fixture("StyledRed", "style", "Red theme with accent border and stacked layout") { InvoiceContent(InvoiceExamples.styledRed) },
    Fixture("StyledStacked", "style", "Green theme with stacked header, right logo placement") { InvoiceContent(InvoiceExamples.styledStacked) },
    Fixture("GridLines", "style", "Grid lines and accent border enabled") { InvoiceContent(InvoiceExamples.gridLines) },
    Fixture("FullFeatured", "composite", "Every feature: dual branding, sub-items, adjustments, metadata, payment") { InvoiceContent(InvoiceExamples.fullFeatured) },
    Fixture("ManyItems", "stress", "30 line items to test dense layout") { InvoiceContent(InvoiceExamples.manyItems) },
)

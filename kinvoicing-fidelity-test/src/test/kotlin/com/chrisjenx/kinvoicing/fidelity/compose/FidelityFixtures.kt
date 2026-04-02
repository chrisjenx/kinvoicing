package com.chrisjenx.kinvoicing.fidelity.compose

import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.kinvoicing.InvoiceDocument
import com.chrisjenx.kinvoicing.examples.InvoiceExamples

data class Fixture(
    val name: String,
    val category: String,
    val description: String,
    val document: InvoiceDocument,
    val vectorThreshold: Double = 0.30,
    val htmlThreshold: Double = 0.35,
    val emailHtmlThreshold: Double = 0.35,
    val config: PdfPageConfig = PdfPageConfig.A4,
)

val fidelityFixtures: List<Fixture> = listOf(
    Fixture("BasicInvoice", "basic", "Standard invoice: header, parties, line items, summary, footer", InvoiceExamples.basic),
    Fixture("MinimalInvoice", "basic", "Minimum viable invoice: header + one item + total", InvoiceExamples.minimal),
    Fixture("SubItems", "line-items", "Nested sub-items with item-level discounts", InvoiceExamples.subItems),
    Fixture("CreditsNegatives", "line-items", "Credit memo with negative amounts", InvoiceExamples.creditsAndNegatives),
    Fixture("AllAdjustments", "adjustments", "Discount, tax, fee, and credit at summary level", InvoiceExamples.adjustments),
    Fixture("DualBranding", "branding", "Powered-by footer with two brand identities", InvoiceExamples.dualBranding),
    Fixture("Metadata", "sections", "Rich metadata block (PO, project, contract)", InvoiceExamples.metadata),
    Fixture("PaymentInfo", "sections", "Bank details and payment link", InvoiceExamples.paymentInfo),
    Fixture("CustomSections", "sections", "Custom content sections with text, dividers, rows", InvoiceExamples.customSections),
    Fixture("StyledRed", "style", "Red theme with accent border and stacked layout", InvoiceExamples.styledRed),
    Fixture("StyledStacked", "style", "Green theme with stacked header, right logo placement", InvoiceExamples.styledStacked),
    Fixture("GridLines", "style", "Grid lines and accent border enabled", InvoiceExamples.gridLines),
    Fixture("FullFeatured", "composite", "Every feature: dual branding, sub-items, adjustments, metadata, payment", InvoiceExamples.fullFeatured),
    Fixture("ManyItems", "stress", "15 line items to test dense layout", InvoiceExamples.manyItems),
    Fixture("LinksAndImages", "links", "All link types (website, email, phone, payment) and images (logo, custom)", InvoiceExamples.linksAndImages),
)

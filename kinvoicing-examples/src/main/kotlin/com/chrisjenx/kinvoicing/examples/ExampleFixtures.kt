package com.chrisjenx.kinvoicing.examples

import com.chrisjenx.kinvoicing.BrandLayout
import com.chrisjenx.kinvoicing.HeaderLayout
import com.chrisjenx.kinvoicing.InvoiceDocument
import com.chrisjenx.kinvoicing.InvoiceFixtures
import com.chrisjenx.kinvoicing.InvoiceThemes
import com.chrisjenx.kinvoicing.LogoPlacement
import com.chrisjenx.kinvoicing.invoice
import kotlinx.datetime.LocalDate

/**
 * Invoice example fixtures demonstrating every DSL feature.
 * Used for fidelity testing and documentation.
 */
object InvoiceExamples {

    /** All examples for parameterized testing. */
    val all: List<Pair<String, InvoiceDocument>> by lazy {
        listOf(
            "basic" to basic,
            "minimal" to minimal,
            "full-featured" to fullFeatured,
            "sub-items" to subItems,
            "adjustments" to adjustments,
            "credits-negatives" to creditsAndNegatives,
            "dual-branding" to dualBranding,
            "metadata" to metadata,
            "payment-info" to paymentInfo,
            "custom-sections" to customSections,
            "styled-red" to styledRed,
            "styled-stacked" to styledStacked,
            "grid-lines" to gridLines,
            "many-items" to manyItems,
            "pagination" to pagination,
            "theme-corporate" to themeCorporate,
            "theme-modern" to themeModern,
            "theme-bold" to themeBold,
            "theme-warm" to themeWarm,
            "theme-minimal" to themeMinimal,
            "theme-elegant" to themeElegant,
            "theme-fresh" to themeFresh,
            "links-and-images" to linksAndImages,
        )
    }

    // ── Basic ──

    /** Standard invoice: header, parties, line items, summary, footer. */
    val basic: InvoiceDocument = invoice {
        header {
            branding {
                primary {
                    name("Acme Corp")
                    address("123 Main St", "Springfield, IL 62701")
                    email("billing@acme.com")
                }
            }
            invoiceNumber("INV-2026-0001")
            issueDate(LocalDate(2026, 3, 1))
            dueDate(LocalDate(2026, 3, 31))
        }
        billFrom {
            name("Acme Corp")
            address("123 Main St", "Springfield, IL 62701")
            email("billing@acme.com")
            phone("+1 (555) 100-0001")
        }
        billTo {
            name("Jane Smith")
            address("456 Oak Ave", "Boulder, CO 80301")
            email("jane@example.com")
            phone("+1 (555) 200-0002")
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            item("Web Development", qty = 40, unitPrice = 150.0)
            item("Design Services", qty = 10, unitPrice = 100.0)
            item("Hosting (Monthly)", qty = 1, unitPrice = 49.99)
        }
        summary {
            currency("USD")
        }
        footer {
            notes("Thank you for your business!")
            terms("Net 30")
        }
    }

    /** Absolute minimum: header + one item + total. */
    val minimal: InvoiceDocument = invoice {
        header {
            invoiceNumber("INV-MIN")
        }
        billTo {
            name("Customer")
        }
        lineItems {
            columns("Description", "Amount")
            item("Consulting", amount = 500.0)
        }
        summary {
            currency("USD")
        }
    }

    // ── Sub-items & Item Discounts ──

    /** Nested sub-items under parent line items with item-level discounts. */
    val subItems: InvoiceDocument = invoice {
        header {
            branding { primary { name("Dev Agency") } }
            invoiceNumber("INV-SUB-001")
            issueDate(LocalDate(2026, 3, 15))
            dueDate(LocalDate(2026, 4, 14))
        }
        billTo {
            name("TechStartup Inc.")
            address("100 Innovation Way", "Austin, TX 78701")
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            item("Backend Development", qty = 80, unitPrice = 150.0) {
                sub("API Design", qty = 20, unitPrice = 175.0)
                sub("Database Architecture", qty = 15, unitPrice = 175.0)
                sub("Implementation", qty = 35, unitPrice = 140.0)
                sub("Code Review", qty = 10, unitPrice = 125.0)
                discount("Volume discount", percent = 10.0)
            }
            item("Frontend Development", qty = 60, unitPrice = 140.0) {
                sub("React Components", qty = 30, unitPrice = 150.0)
                sub("Styling & Animation", qty = 20, unitPrice = 130.0)
                sub("Testing", qty = 10, unitPrice = 120.0)
            }
            item("DevOps Setup", qty = 1, unitPrice = 2000.0) {
                sub("CI/CD Pipeline", amount = 800.0)
                sub("Kubernetes Config", amount = 700.0)
                sub("Monitoring Setup", amount = 500.0)
            }
        }
        summary {
            currency("USD")
        }
    }

    // ── Adjustments ──

    /** All adjustment types: discount, tax, fee, credit at summary level. */
    val adjustments: InvoiceDocument = invoice {
        header {
            branding { primary { name("Pro Services LLC") } }
            invoiceNumber("INV-ADJ-001")
            issueDate(LocalDate(2026, 3, 1))
            dueDate(LocalDate(2026, 3, 31))
        }
        billTo {
            name("Enterprise Client")
            address("500 Business Park Dr", "Chicago, IL 60601")
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            item("Strategic Consulting", qty = 20, unitPrice = 300.0)
            item("Technical Audit", qty = 1, unitPrice = 5000.0)
            item("Training Workshop", qty = 2, unitPrice = 1500.0)
        }
        summary {
            currency("USD")
            discount("Early payment discount", percent = 5.0)
            discount("Loyalty discount", fixed = 200.0)
            tax("IL Sales Tax", percent = 6.25)
            tax("City Tax", percent = 1.0)
            fee("Expedited processing", fixed = 75.0)
            credit("Account credit", amount = 500.0)
        }
        footer {
            notes("Discounts applied before taxes. Credits reduce the final total.")
            terms("Net 30. 1.5% monthly interest on overdue balances.")
        }
    }

    // ── Credits & Negatives ──

    /** Credit memo with negative amounts and net-zero result. */
    val creditsAndNegatives: InvoiceDocument = invoice {
        header {
            invoiceNumber("INV-2026-CREDIT")
            issueDate(LocalDate(2026, 3, 15))
        }
        billTo {
            name("Bob Johnson")
            email("bob@example.com")
        }
        lineItems {
            columns("Description", "Amount")
            item("Overpayment refund", amount = -1000.0)
            item("Service credit (March)", amount = -500.0)
            item("Reactivation fee", amount = 200.0)
            item("Remaining balance forward", amount = 1300.0)
        }
        summary {
            currency("USD")
        }
        footer {
            notes("This credit memo reflects adjustments to your account.")
        }
    }

    // ── Dual Branding ──

    /** Powered-by branding with two brand identities. */
    val dualBranding: InvoiceDocument = invoice {
        header {
            branding {
                primary {
                    name("Artisan Coffee Roasters")
                    address("42 Bean Street", "Portland, OR 97201")
                    email("orders@artisancoffee.com")
                    website("https://artisancoffee.com")
                    tagline("Small batch, big flavor")
                }
                poweredBy {
                    name("Kinvoicing Payments")
                    website("https://kinvoicing.dev")
                    tagline("Powered by Kinvoicing")
                }
                layout = BrandLayout.POWERED_BY_FOOTER
            }
            invoiceNumber("INV-BRAND-001")
            issueDate(LocalDate(2026, 3, 10))
            dueDate(LocalDate(2026, 4, 10))
        }
        billTo {
            name("Downtown Café")
            address("88 Brew Ave", "Portland, OR 97209")
        }
        lineItems {
            columns("Item", "Qty", "Unit Price", "Total")
            item("Ethiopian Yirgacheffe (1lb)", qty = 10, unitPrice = 18.50)
            item("Colombian Supremo (1lb)", qty = 8, unitPrice = 16.00)
            item("House Blend (5lb)", qty = 3, unitPrice = 55.00)
        }
        summary {
            currency("USD")
            tax("OR Sales Tax", percent = 0.0)
        }
        footer {
            notes("Free shipping on orders over \$200!")
        }
    }

    // ── Metadata ──

    /** Invoice with rich metadata block (PO number, project, contract). */
    val metadata: InvoiceDocument = invoice {
        header {
            branding { primary { name("Consulting Group") } }
            invoiceNumber("INV-META-001")
            issueDate(LocalDate(2026, 3, 20))
            dueDate(LocalDate(2026, 4, 19))
        }
        billTo {
            name("Government Agency")
            address("1 Federal Plaza", "Washington, DC 20001")
        }
        metaBlock {
            entry("Purchase Order", "PO-2026-1138")
            entry("Project", "Digital Transformation Phase 2")
            entry("Contract Ref", "GSA-2025-007")
            entry("Department", "IT Modernization")
            entry("Cost Center", "CC-4200")
            entry("Approver", "Sarah Williams, CTO")
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            item("Requirements Analysis", qty = 40, unitPrice = 200.0)
            item("System Architecture", qty = 30, unitPrice = 225.0)
            item("Implementation", qty = 80, unitPrice = 175.0)
        }
        summary {
            currency("USD")
        }
    }

    // ── Payment Info ──

    /** Invoice with full payment details (bank, payment link). */
    val paymentInfo: InvoiceDocument = invoice {
        header {
            branding {
                primary {
                    name("Global Exports Ltd.")
                    address("100 Trade Center", "Miami, FL 33101")
                }
            }
            invoiceNumber("INV-PAY-001")
            issueDate(LocalDate(2026, 3, 5))
            dueDate(LocalDate(2026, 4, 5))
        }
        billTo {
            name("International Imports Co.")
            address("Via Roma 42", "Milan, Italy 20121")
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            item("Premium Widget (Export)", qty = 500, unitPrice = 12.50)
            item("Shipping & Handling", qty = 1, unitPrice = 450.0)
            item("Export Documentation", qty = 1, unitPrice = 150.0)
        }
        summary {
            currency("USD")
            fee("Wire transfer fee", fixed = 35.0)
        }
        paymentInfo {
            bankName("First International Bank")
            accountNumber("****4242")
            routingNumber("000000000")
            paymentLink("https://pay.globalexports.com/inv-pay-001")
            notes("Wire transfers preferred for international orders.")
        }
        footer {
            terms("Net 30. All prices in USD.")
        }
    }

    // ── Custom Sections ──

    /** Invoice with custom content sections using InvoiceElement DSL. */
    val customSections: InvoiceDocument = invoice {
        header {
            branding { primary { name("Creative Studio") } }
            invoiceNumber("INV-CUSTOM-001")
            issueDate(LocalDate(2026, 3, 25))
            dueDate(LocalDate(2026, 4, 24))
        }
        billTo {
            name("Marketing Agency")
            address("200 Ad Row", "New York, NY 10013")
        }
        lineItems {
            columns("Deliverable", "Amount")
            item("Logo Design Package", amount = 2500.0)
            item("Brand Guidelines", amount = 1500.0)
            item("Social Media Kit", amount = 800.0)
        }
        custom("project-scope") {
            text("Project Scope & Deliverables")
            divider()
            text("Phase 1: Discovery & Research")
            text("Phase 2: Concept Development (3 directions)")
            text("Phase 3: Refinement & Final Delivery")
            spacer()
            text("All deliverables include source files and usage rights.")
        }
        custom("timeline") {
            text("Project Timeline")
            divider()
            row(1f, 1f) {
                text("Start Date")
                text("March 25, 2026")
            }
            row(1f, 1f) {
                text("Milestone 1")
                text("April 8, 2026")
            }
            row(1f, 1f) {
                text("Final Delivery")
                text("April 24, 2026")
            }
        }
        summary {
            currency("USD")
            discount("Package discount", percent = 5.0)
        }
        footer {
            notes("Revisions beyond 3 rounds subject to additional charges.")
        }
    }

    // ── Full Featured ──

    /** Exercises every major feature: dual branding, sub-items, all adjustments, metadata, payment info. Fits on one A4 page. */
    val fullFeatured: InvoiceDocument = invoice {
        style {
            primaryColor = 0xFF1E40AF
            accentBorder = true
            showGridLines = true
        }
        header {
            branding {
                primary {
                    name("Client's Store")
                    address("456 Oak Ave", "Boulder, CO 80301")
                }
                poweredBy {
                    name("Acme Payments")
                    tagline("Powered by Acme Payments")
                }
                layout = BrandLayout.POWERED_BY_FOOTER
            }
            invoiceNumber("INV-2026-0042")
            issueDate(LocalDate(2026, 3, 23))
            dueDate(LocalDate(2026, 4, 22))
        }
        billTo {
            name("Jane Smith")
            address("456 Oak Ave", "Boulder, CO 80301")
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            item("Consulting - March", qty = 40, unitPrice = 150.0) {
                sub("Senior engineer", qty = 24, unitPrice = 175.0)
                sub("Junior engineer", qty = 16, unitPrice = 112.50)
                discount("Volume discount", percent = 10.0)
            }
            item("Travel expenses", qty = 1, unitPrice = 350.0) {
                sub("Flight LAX-DEN", amount = 280.0)
                sub("Ground transport", amount = 70.0)
            }
        }
        metaBlock {
            entry("PO Number", "PO-2026-1138")
            entry("Project", "Website Redesign")
        }
        summary {
            currency("USD")
            discount("Early payment", percent = 5.0)
            tax("CO Sales Tax", percent = 4.55)
            fee("Wire transfer fee", fixed = 25.0)
        }
        footer {
            notes("Thank you for your business!")
            terms("Net 30. Late payments subject to 1.5% monthly interest.")
        }
    }

    // ── Style Variations ──

    /** Red theme with accent border and stacked header layout. */
    val styledRed: InvoiceDocument = invoice {
        style {
            primaryColor = 0xFFDC2626
            secondaryColor = 0xFF991B1B
            textColor = 0xFF1C1917
            backgroundColor = 0xFFFEF2F2
            accentBorder = true
            headerLayout = HeaderLayout.STACKED
            logoPlacement = LogoPlacement.CENTER
        }
        header {
            branding {
                primary {
                    name("Red Design Studio")
                    address("789 Color Blvd", "San Francisco, CA 94102")
                    tagline("Bold design, bold results")
                }
            }
            invoiceNumber("INV-STYLE-RED")
            issueDate(LocalDate(2026, 3, 20))
            dueDate(LocalDate(2026, 4, 20))
        }
        billTo {
            name("Fashion Brand Co.")
            address("321 Fashion Ave", "Los Angeles, CA 90001")
        }
        lineItems {
            columns("Service", "Hours", "Rate", "Total")
            item("Brand Identity Design", qty = 20, unitPrice = 200.0)
            item("Website Mockups", qty = 15, unitPrice = 175.0)
            item("Print Collateral", qty = 8, unitPrice = 150.0)
        }
        summary {
            currency("USD")
            discount("Loyalty discount", percent = 10.0)
            tax("CA Sales Tax", percent = 7.25)
        }
    }

    /** Stacked header layout variation. */
    val styledStacked: InvoiceDocument = invoice {
        style {
            primaryColor = 0xFF059669
            secondaryColor = 0xFF047857
            headerLayout = HeaderLayout.STACKED
            logoPlacement = LogoPlacement.RIGHT
        }
        header {
            branding {
                primary {
                    name("Green Consulting")
                    address("1 Eco Drive", "Denver, CO 80202")
                    email("hello@greenconsulting.com")
                }
            }
            invoiceNumber("INV-STACK-001")
            issueDate(LocalDate(2026, 3, 12))
            dueDate(LocalDate(2026, 4, 12))
        }
        billTo {
            name("Sustainability Corp")
            address("400 Clean St", "Seattle, WA 98101")
        }
        lineItems {
            columns("Description", "Amount")
            item("Carbon Audit", amount = 3500.0)
            item("ESG Report", amount = 2000.0)
        }
        summary {
            currency("USD")
        }
    }

    /** Grid lines enabled with accent border. */
    val gridLines: InvoiceDocument = invoice {
        style {
            showGridLines = true
            accentBorder = true
            primaryColor = 0xFF7C3AED
        }
        header {
            branding { primary { name("Data Analytics Inc.") } }
            invoiceNumber("INV-GRID-001")
            issueDate(LocalDate(2026, 3, 18))
            dueDate(LocalDate(2026, 4, 18))
        }
        billTo {
            name("Research Lab")
            address("50 Data Lane", "Boston, MA 02101")
        }
        lineItems {
            columns("Analysis Type", "Qty", "Rate", "Amount")
            item("Data Cleaning", qty = 10, unitPrice = 80.0)
            item("Statistical Analysis", qty = 8, unitPrice = 120.0)
            item("ML Model Training", qty = 5, unitPrice = 200.0)
            item("Report Generation", qty = 3, unitPrice = 150.0)
            item("Visualization Dashboard", qty = 2, unitPrice = 250.0)
        }
        summary {
            currency("USD")
            tax("MA Sales Tax", percent = 6.25)
        }
    }

    // ── Stress Test ──

    /** Many line items to test dense layout. Fits on one A4 page. */
    val manyItems: InvoiceDocument = invoice {
        header {
            branding { primary { name("Enterprise Solutions") } }
            invoiceNumber("INV-LONG-001")
            issueDate(LocalDate(2026, 3, 1))
            dueDate(LocalDate(2026, 4, 1))
        }
        billTo {
            name("MegaCorp Ltd.")
            address("1 Corporate Plaza", "New York, NY 10001")
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            for (i in 1..10) {
                item("Service item #$i", qty = i, unitPrice = 10.0 + i)
            }
        }
        summary {
            currency("USD")
            tax("State Tax", percent = 8.0)
        }
    }

    /** 50+ items — designed to overflow and test multi-page pagination. */
    val pagination: InvoiceDocument = invoice {
        header {
            branding { primary { name("Enterprise Solutions") } }
            invoiceNumber("INV-PAGED-001")
            issueDate(LocalDate(2026, 3, 1))
            dueDate(LocalDate(2026, 4, 1))
        }
        billTo {
            name("MegaCorp Ltd.")
            address("1 Corporate Plaza", "New York, NY 10001")
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            for (i in 1..50) {
                item("Service item #$i", qty = i, unitPrice = 10.0 + i)
            }
        }
        summary {
            currency("USD")
            tax("State Tax", percent = 8.0)
        }
    }

    // ── Links & Images ──

    /** Exercises every link type (website, email, phone, payment) and image path (logo, custom). */
    val linksAndImages: InvoiceDocument = InvoiceFixtures.linksAndImages

    // ── Built-in Theme Showcase ──

    /** Corporate theme: navy, accent stripe, professional. */
    val themeCorporate: InvoiceDocument = themedInvoice(InvoiceThemes.Corporate, "CORP")

    /** Modern theme: indigo, airy, minimal decoration. */
    val themeModern: InvoiceDocument = themedInvoice(InvoiceThemes.Modern, "MOD")

    /** Bold theme: strong blue, grid lines, structured. */
    val themeBold: InvoiceDocument = themedInvoice(InvoiceThemes.Bold, "BOLD")

    /** Warm theme: amber earth tones, Georgia font, friendly. */
    val themeWarm: InvoiceDocument = themedInvoice(InvoiceThemes.Warm, "WARM")

    /** Minimal theme: near-monochrome, ultra-clean. */
    val themeMinimal: InvoiceDocument = themedInvoice(InvoiceThemes.Minimal, "MIN")

    /** Elegant theme: dark stone + gold, Georgia font. */
    val themeElegant: InvoiceDocument = themedInvoice(InvoiceThemes.Elegant, "ELGNT")

    /** Fresh theme: green/teal, eco-feeling. */
    val themeFresh: InvoiceDocument = themedInvoice(InvoiceThemes.Fresh, "FRESH")
}

/**
 * Creates a representative invoice using a given theme, exercising header,
 * parties, line items, summary, payment info, and footer.
 */
private fun themedInvoice(
    theme: com.chrisjenx.kinvoicing.InvoiceStyle,
    code: String,
): InvoiceDocument = invoice {
    style { theme(theme) }
    header {
        branding {
            primary {
                name("Kinvoicing Demo")
                address("1 Theme Lane", "San Francisco, CA 94102")
                email("hello@kinvoicing.dev")
            }
        }
        invoiceNumber("INV-$code-001")
        issueDate(LocalDate(2026, 3, 1))
        dueDate(LocalDate(2026, 3, 31))
    }
    billFrom {
        name("Kinvoicing Demo")
        address("1 Theme Lane", "San Francisco, CA 94102")
        email("hello@kinvoicing.dev")
        phone("+1 (555) 000-0000")
    }
    billTo {
        name("Showcase Client")
        address("200 Preview Blvd", "New York, NY 10001")
        email("client@example.com")
    }
    lineItems {
        columns("Description", "Qty", "Rate", "Amount")
        item("Strategic Consulting", qty = 20, unitPrice = 250.0)
        item("Technical Implementation", qty = 40, unitPrice = 175.0)
        item("Project Management", qty = 10, unitPrice = 150.0)
    }
    summary {
        currency("USD")
        discount("Early payment", percent = 5.0)
        tax("Sales Tax", percent = 8.25)
    }
    paymentInfo {
        bankName("First National Bank")
        accountNumber("****1234")
        paymentLink("https://pay.example.com/inv-$code-001")
        notes("Wire transfer preferred.")
    }
    footer {
        notes("Thank you for your business!")
        terms("Net 30. 1.5% monthly interest on overdue balances.")
    }
}

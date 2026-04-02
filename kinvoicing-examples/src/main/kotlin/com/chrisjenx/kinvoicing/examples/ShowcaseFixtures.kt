package com.chrisjenx.kinvoicing.examples

import com.chrisjenx.kinvoicing.BrandLayout
import com.chrisjenx.kinvoicing.HeaderLayout
import com.chrisjenx.kinvoicing.InvoiceDocument
import com.chrisjenx.kinvoicing.InvoiceThemes
import com.chrisjenx.kinvoicing.LogoPlacement
import com.chrisjenx.kinvoicing.invoice
import kotlinx.datetime.LocalDate

/**
 * Focused showcase fixtures that isolate individual DSL sections.
 * Each creates a minimal invoice highlighting one section type.
 * Used for component-level documentation.
 */
object InvoiceShowcases {

    val all: List<Pair<String, InvoiceDocument>> by lazy {
        listOf(
            "header-horizontal" to headerHorizontal,
            "header-stacked" to headerStacked,
            "header-dual-branding" to headerDualBranding,
            "bill-parties" to billParties,
            "line-items-basic" to lineItemsBasic,
            "line-items-sub" to lineItemsSub,
            "summary-adjustments" to summaryAdjustments,
            "payment-info" to paymentInfo,
            "meta-block" to metaBlock,
            "custom-elements" to customElements,
            "footer" to footer,
            "style-grid-accent" to styleGridAccent,
        )
    }

    // ── Header Variations ──

    /** Header with horizontal layout (default). */
    val headerHorizontal: InvoiceDocument = invoice {
        header {
            branding {
                primary {
                    name("Acme Corp")
                    address("123 Main St", "Springfield, IL 62701")
                    email("billing@acme.com")
                    website("https://acme.com")
                }
            }
            invoiceNumber("INV-2026-0001")
            issueDate(LocalDate(2026, 3, 1))
            dueDate(LocalDate(2026, 3, 31))
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            item("Consulting Services", qty = 10, unitPrice = 150.0)
        }
        summary { currency("USD") }
    }

    /** Header with stacked layout. */
    val headerStacked: InvoiceDocument = invoice {
        style {
            headerLayout = HeaderLayout.STACKED
            logoPlacement = LogoPlacement.CENTER
        }
        header {
            branding {
                primary {
                    name("Acme Corp")
                    address("123 Main St", "Springfield, IL 62701")
                    email("billing@acme.com")
                    website("https://acme.com")
                }
            }
            invoiceNumber("INV-2026-0001")
            issueDate(LocalDate(2026, 3, 1))
            dueDate(LocalDate(2026, 3, 31))
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            item("Consulting Services", qty = 10, unitPrice = 150.0)
        }
        summary { currency("USD") }
    }

    /** Header with dual branding (primary + powered-by). */
    val headerDualBranding: InvoiceDocument = invoice {
        header {
            branding {
                layout = BrandLayout.DUAL_HEADER
                primary {
                    name("Client Company")
                    address("100 Client Ave", "Chicago, IL 60601")
                    email("billing@client.com")
                }
                poweredBy {
                    name("Powered by Kinvoicing")
                    tagline("Professional Invoicing")
                }
            }
            invoiceNumber("INV-2026-0042")
            issueDate(LocalDate(2026, 3, 1))
            dueDate(LocalDate(2026, 3, 31))
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            item("Platform Services", qty = 1, unitPrice = 999.0)
        }
        summary { currency("USD") }
    }

    // ── Parties ──

    /** BillFrom and BillTo side-by-side. */
    val billParties: InvoiceDocument = invoice {
        header {
            branding { primary { name("Acme Corp") } }
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
            columns("Description", "Amount")
            item("Services Rendered", amount = 1500.0)
        }
        summary { currency("USD") }
    }

    // ── Line Items ──

    /** Simple line items with quantity and unit price. */
    val lineItemsBasic: InvoiceDocument = invoice {
        header {
            branding { primary { name("Acme Corp") } }
            invoiceNumber("INV-2026-0001")
            issueDate(LocalDate(2026, 3, 1))
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            item("Web Development", qty = 40, unitPrice = 150.0)
            item("Design Services", qty = 10, unitPrice = 100.0)
            item("Hosting (Monthly)", qty = 1, unitPrice = 49.99)
            item("Domain Registration", qty = 1, unitPrice = 14.99)
        }
        summary { currency("USD") }
    }

    /** Line items with nested sub-items. */
    val lineItemsSub: InvoiceDocument = invoice {
        header {
            branding { primary { name("Acme Corp") } }
            invoiceNumber("INV-2026-0001")
            issueDate(LocalDate(2026, 3, 1))
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            item("Project Alpha") {
                sub("Design Phase", qty = 20, unitPrice = 150.0)
                sub("Development Phase", qty = 40, unitPrice = 175.0)
                sub("Testing & QA", qty = 10, unitPrice = 125.0)
            }
            item("Project Beta") {
                sub("Requirements Gathering", qty = 8, unitPrice = 200.0)
                sub("Implementation", qty = 24, unitPrice = 175.0)
            }
        }
        summary { currency("USD") }
    }

    // ── Summary ──

    /** Summary with all adjustment types. */
    val summaryAdjustments: InvoiceDocument = invoice {
        header {
            branding { primary { name("Acme Corp") } }
            invoiceNumber("INV-2026-0001")
            issueDate(LocalDate(2026, 3, 1))
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            item("Consulting", qty = 20, unitPrice = 200.0)
            item("Implementation", qty = 40, unitPrice = 175.0)
        }
        summary {
            currency("USD")
            discount("Early Payment (10%)", percent = 10.0)
            tax("Sales Tax (8.25%)", percent = 8.25)
            fee("Processing Fee", fixed = 25.0)
            credit("Referral Credit", 100.0)
        }
    }

    // ── Payment Info ──

    /** Payment information section. */
    val paymentInfo: InvoiceDocument = invoice {
        header {
            branding { primary { name("Acme Corp") } }
            invoiceNumber("INV-2026-0001")
            issueDate(LocalDate(2026, 3, 1))
        }
        lineItems {
            columns("Description", "Amount")
            item("Services", amount = 5000.0)
        }
        summary { currency("USD") }
        paymentInfo {
            bankName("First National Bank")
            accountNumber("9876543210")
            routingNumber("021000021")
            paymentLink("https://pay.example.com/inv-001")
            notes("Wire transfers are preferred for amounts over \$1,000.")
        }
    }

    // ── Meta Block ──

    /** Metadata block with key-value entries. */
    val metaBlock: InvoiceDocument = invoice {
        header {
            branding { primary { name("Acme Corp") } }
            invoiceNumber("INV-2026-0001")
            issueDate(LocalDate(2026, 3, 1))
        }
        metaBlock {
            entry("PO Number", "PO-2026-0042")
            entry("Project", "Website Redesign")
            entry("Contract Ref", "C-2026-001")
            entry("Department", "Engineering")
            entry("Cost Center", "CC-4200")
            entry("Approver", "Sarah Johnson")
        }
        lineItems {
            columns("Description", "Amount")
            item("Services", amount = 5000.0)
        }
        summary { currency("USD") }
    }

    // ── Custom Elements ──

    /** Custom section exercising all element types. */
    val customElements: InvoiceDocument = invoice {
        header {
            branding { primary { name("Acme Corp") } }
            invoiceNumber("INV-2026-0001")
            issueDate(LocalDate(2026, 3, 1))
        }
        lineItems {
            columns("Description", "Amount")
            item("Services", amount = 2500.0)
        }
        summary { currency("USD") }
        custom("terms-and-conditions") {
            text("Terms & Conditions", styleRef = "bold")
            divider()
            text("Payment is due within 30 days of the invoice date.")
            spacer()
            row(1f, 1f) {
                text("Questions? Contact billing@acme.com")
                text("Ref: T&C v2.1 (2026)")
            }
            spacer(8)
            link("View full terms", "https://acme.com/terms")
        }
    }

    // ── Footer ──

    /** Footer with notes and terms. */
    val footer: InvoiceDocument = invoice {
        header {
            branding { primary { name("Acme Corp") } }
            invoiceNumber("INV-2026-0001")
            issueDate(LocalDate(2026, 3, 1))
        }
        lineItems {
            columns("Description", "Amount")
            item("Services", amount = 2500.0)
        }
        summary { currency("USD") }
        footer {
            notes("Thank you for your business! We appreciate your prompt payment.")
            terms("Net 30. A 1.5% monthly interest charge applies to overdue balances.")
        }
    }

    // ── Style Variations ──

    /** Grid lines and accent border style. */
    val styleGridAccent: InvoiceDocument = invoice {
        style {
            theme(InvoiceThemes.Bold)
            showGridLines = true
            accentBorder = true
        }
        header {
            branding { primary { name("Acme Corp") } }
            invoiceNumber("INV-2026-0001")
            issueDate(LocalDate(2026, 3, 1))
            dueDate(LocalDate(2026, 3, 31))
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            item("Consulting", qty = 10, unitPrice = 200.0)
            item("Development", qty = 20, unitPrice = 175.0)
            item("Testing", qty = 5, unitPrice = 125.0)
        }
        summary {
            currency("USD")
            tax("Sales Tax", percent = 8.25)
        }
    }
}

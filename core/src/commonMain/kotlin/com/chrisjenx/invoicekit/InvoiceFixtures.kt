package com.chrisjenx.invoicekit

import kotlinx.datetime.LocalDate

/**
 * Standard test fixtures for use across all modules and tests.
 */
public object InvoiceFixtures {

    /** All fixtures for parameterized testing. */
    public val all: List<InvoiceDocument> by lazy {
        listOf(basic, fullFeatured, negativeValues, long, minimal, styled)
    }

    /** Single brand, 3 line items, no sub-items, no discounts. */
    public val basic: InvoiceDocument = invoice {
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
            email("billing@acme.com")
        }
        billTo {
            name("Jane Smith")
            address("456 Oak Ave", "Boulder, CO 80301")
            email("jane@example.com")
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

    /** Dual branding, sub-items, discounts, taxes, fees, credits, metadata, payment info. */
    public val fullFeatured: InvoiceDocument = invoice {
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
                    website("https://clientstore.com")
                }
                poweredBy {
                    name("Acme Payments")
                    tagline("Powered by Acme Payments")
                    website("https://acme.com")
                }
                layout = BrandLayout.POWERED_BY_FOOTER
            }
            invoiceNumber("INV-2026-0042")
            issueDate(LocalDate(2026, 3, 23))
            dueDate(LocalDate(2026, 4, 22))
        }
        billFrom {
            name("Client's Store")
            email("billing@clientstore.com")
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
            item("Unused retainer credit", amount = -500.0)
        }
        meta {
            entry("PO Number", "PO-2026-1138")
            entry("Project", "Website Redesign")
            entry("Contract Ref", "MSA-2025-007")
        }
        summary {
            currency("USD")
            discount("Early payment", percent = 5.0)
            tax("CO Sales Tax", percent = 4.55)
            fee("Wire transfer fee", fixed = 25.0)
        }
        paymentInfo {
            bankName("First National")
            accountNumber("****4242")
            paymentLink("https://pay.acme.com/inv-0042")
        }
        footer {
            notes("Thank you for your business!")
            terms("Net 30. Late payments subject to 1.5% monthly interest.")
        }
    }

    /** Credit lines, negative adjustments, net-zero invoice. */
    public val negativeValues: InvoiceDocument = invoice {
        header {
            invoiceNumber("INV-2026-CREDIT")
            issueDate(LocalDate(2026, 3, 15))
        }
        billTo {
            name("Bob Johnson")
        }
        lineItems {
            columns("Description", "Amount")
            item("Overpayment refund", amount = -1000.0)
            item("Service credit", amount = -500.0)
            item("Adjustment fee", amount = 200.0)
            item("Remaining balance", amount = 1300.0)
        }
        summary {
            currency("USD")
        }
    }

    /** 50+ line items to trigger multi-page in PDF. */
    public val long: InvoiceDocument = invoice {
        header {
            branding {
                primary { name("Enterprise Solutions Inc.") }
            }
            invoiceNumber("INV-2026-LONG")
            issueDate(LocalDate(2026, 3, 1))
            dueDate(LocalDate(2026, 4, 1))
        }
        billTo {
            name("MegaCorp Ltd.")
            address("1 Corporate Plaza", "New York, NY 10001")
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            for (i in 1..55) {
                item("Service item #$i", qty = i, unitPrice = 10.0 + i)
            }
        }
        summary {
            currency("USD")
            tax("State Tax", percent = 8.0)
        }
    }

    /** Header + single line item + total only. Tests graceful handling of missing sections. */
    public val minimal: InvoiceDocument = invoice {
        header {
            invoiceNumber("INV-MIN")
        }
        billTo {
            name("Customer")
        }
        lineItems {
            columns("Description", "Amount")
            item("Service", amount = 100.0)
        }
        summary {
            currency("USD")
        }
    }

    /** Custom colors, accent borders, grid lines. Tests style propagation. */
    public val styled: InvoiceDocument = invoice {
        style {
            primaryColor = 0xFFDC2626
            secondaryColor = 0xFF991B1B
            textColor = 0xFF1C1917
            backgroundColor = 0xFFFEF2F2
            fontFamily = "Georgia"
            logoPlacement = LogoPlacement.CENTER
            headerLayout = HeaderLayout.STACKED
            showGridLines = true
            accentBorder = true
        }
        header {
            branding {
                primary {
                    name("Red Design Studio")
                    address("789 Color Blvd", "San Francisco, CA 94102")
                }
            }
            invoiceNumber("INV-STYLE-001")
            issueDate(LocalDate(2026, 3, 20))
            dueDate(LocalDate(2026, 4, 20))
        }
        billTo {
            name("Style Client")
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
        footer {
            notes("Colors and creativity are our passion!")
        }
    }
}

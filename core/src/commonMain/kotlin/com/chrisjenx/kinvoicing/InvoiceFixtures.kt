package com.chrisjenx.kinvoicing

import kotlinx.datetime.LocalDate

/**
 * Standard test fixtures for use across all modules and tests.
 */
public object InvoiceFixtures {

    /** Test logo PNG (200×60, red rounded rectangle with "LOGO" text, 1480 bytes). */
    @Suppress("MagicNumber")
    public val TEST_LOGO_PNG: ByteArray = byteArrayOf(
        -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, -56,
        0, 0, 0, 60, 8, 6, 0, 0, 0, 35, 91, -11, -111, 0, 0, 5, -113, 73, 68, 65,
        84, 120, 94, -19, -37, -55, 107, 83, 65, 28, 7, -16, -100, -4, 39, 60, 55, 46, 81, 11,
        46, 8, -30, 65, 16, 55, -68, 120, 16, 65, -15, 32, 122, 17, 73, 17, 21, 84, 20, 81,
        65, 60, -72, -96, 69, -68, -88, -88, 40, 42, 30, 68, 113, 1, 21, -36, -105, 8, -126, -67,
        88, -79, -104, -60, -90, 106, -105, 52, 105, -38, 36, 109, -102, -104, 102, 124, -65, -55, 75, -14,
        102, 38, -103, 54, -23, -53, -59, -7, -2, -32, 115, 48, -77, 116, 14, -13, -51, 123, 121, 111,
        -12, 120, -20, -22, -12, -7, 102, 4, 91, 90, -38, -126, 94, 111, -64, -110, -78, 48, 0, -125,
        -48, -98, 15, 80, 6, 40, 11, -91, 92, -16, -22, -14, 122, 103, -122, -68, -34, -114, 42, -125,
        0, -116, 67, 89, -96, 76, -108, -81, 28, 8, 7, -128, -120, 50, -63, -81, 36, -42, 63, -4,
        114, 35, 0, 112, 126, -113, -107, -108, 79, 85, 26, 0, -116, 71, -39, -96, 43, 72, 82, 110,
        0, 0, 46, 73, 1, -111, 63, 4, 0, 27, 2, 2, -96, -127, -128, 0, 104, 32, 32, 0,
        26, 8, 8, -128, 6, 2, 2, -96, -127, -128, 0, 104, 32, 32, 0, 26, 8, 8, -128, 6,
        2, 2, -96, -127, -128, 0, 104, 32, 32, 0, 26, 8, 8, -128, 6, 2, 2, -96, -127, -128,
        52, 81, 104, -18, 92, 22, -100, 61, 91, -7, 124, 58, 66, 11, 22, 40, -97, -43, -85, 25,
        -21, -6, 95, 33, 32, -74, -111, 59, 119, 88, -14, -34, 61, 110, -8, -6, 117, -91, 125, 42,
        126, 46, 91, -58, -122, 46, 92, 96, -23, -89, 79, 89, 46, 20, 98, -123, 124, -98, 21, -58,
        -57, -39, 120, 103, 39, 75, 62, 120, -64, 6, 79, -100, 96, -95, 121, -13, -108, 113, -75, -124,
        -105, 44, 97, -15, 115, -25, 88, -22, -47, 35, 54, -2, -11, 43, -101, 72, -89, 25, -43, -60,
        -56, 8, -53, 126, -1, -50, 70, 95, -66, 100, -61, -73, 110, -79, -40, -87, 83, 44, -68, 104,
        -111, 50, -66, 89, -21, 50, 9, 2, 98, 43, 100, -77, 124, -13, 81, -27, -29, 113, -91, 125,
        50, 3, -5, -9, -13, -115, 59, 89, -47, 6, -3, -67, 105, -109, 50, 94, 48, 107, 22, -117,
        30, 62, -52, -14, -119, -124, 60, -68, 102, 69, 86, -83, 82, -25, 113, 123, 93, 6, 66, 64,
        108, -45, 9, 72, -22, -15, 99, -57, 86, 43, -42, 68, 50, -55, -58, -34, -65, 103, -103, 47,
        95, -124, -71, -117, -115, 19, -4, 91, 91, -98, -121, -77, -62, 49, -6, -10, -83, -40, -97, -54,
        -6, -42, -49, 6, -125, -4, 42, 48, -6, -26, 13, -53, -2, -8, -63, -81, 4, -91, -86, 22,
        16, 87, -41, 101, 40, 4, -60, -42, 104, 64, -6, -9, -20, 113, -20, -80, 98, -59, -49, -97,
        -25, 27, -67, -44, 39, -36, -38, -54, -46, -49, -97, 11, 125, -24, -17, 85, -37, -44, -47, 35,
        71, -124, 126, 84, -23, 103, -49, -8, -19, -106, -36, -105, 110, -85, -6, -37, -38, -40, -56, -35,
        -69, -4, 54, -86, -103, -21, 50, 21, 2, 98, 107, 36, 32, -76, 65, -13, -125, -125, -114, -19,
        -59, 88, -20, -28, 73, -91, 31, 103, -3, 40, 78, 89, -33, -2, -50, 26, -5, -16, 65, -24,
        67, -101, 92, -66, 29, 74, 92, -66, -84, -50, 53, 9, -73, -41, 101, 50, 4, -60, -42, 72,
        64, 98, -89, 79, 59, -74, 21, 99, 127, 123, 123, -117, 79, -120, -86, -12, 37, -111, -43, -85,
        -7, 109, -116, -77, 126, 111, -34, 92, 110, -113, -73, -73, 11, 109, -7, 88, -116, 127, -53, -53,
        -13, 76, -58, -19, 117, -103, 12, 1, -79, 53, 18, -112, -44, -61, -121, -114, 45, -59, -8, -45,
        36, -71, -113, 44, -3, -30, -123, 48, 102, -16, -40, -79, -54, 124, 79, -98, 8, 109, -12, 4,
        75, 30, 63, 21, 110, -81, -53, 100, 8, -120, -83, -111, -128, -48, -93, 87, 103, -11, -18, -40,
        -95, -12, -111, 13, 93, -68, 40, -116, 25, -66, 121, -77, 50, -33, -73, 111, 66, 91, -33, -50,
        -99, -54, -8, -87, 112, 123, 93, 38, 67, 64, 108, 117, 7, -60, -6, -79, 59, 49, 54, -26,
        -40, 82, -116, -11, -84, 91, -89, -10, -109, 68, 15, 29, 18, -58, -116, 125, -4, 88, -98, -81,
        32, -49, -73, 126, -67, 50, 126, 82, 110, -81, -53, 112, 8, -120, -83, -34, -128, -124, 124, 62,
        -27, -66, -67, 123, -59, 10, -91, -97, -84, 127, -33, 62, 97, 76, -90, -93, -93, -26, 124, -111,
        -107, 43, -107, -15, 37, -35, -53, -105, -77, -56, -102, 53, 101, -63, 57, 115, 106, -50, 51, -99,
        117, -103, 14, 1, -79, -43, 27, 16, -110, -117, 68, 28, 91, -54, -6, 97, -69, 101, -117, -46,
        71, 22, 63, 123, 86, 24, -109, -68, 127, -65, 50, 95, 79, -113, -48, -10, 103, -37, 54, 101,
        124, 9, -67, -53, 112, 86, 100, -19, -38, -54, 60, 46, -81, -53, 100, 8, -120, -83, -111, -128,
        -116, -66, 122, 85, -39, 81, 86, 13, 28, 56, -96, -12, -111, -47, 59, 11, 103, -59, -50, -100,
        -87, -52, -9, -6, -75, -48, 54, 120, -4, -72, 50, -66, 68, 23, 16, -73, -41, 101, 50, 4,
        -60, -42, 72, 64, 18, 87, -82, 56, -74, -108, 117, -33, 30, 8, 40, 125, -100, 66, -83, -83,
        44, 63, 52, 36, -116, -23, -37, -75, -85, 50, -33, -75, 107, 66, 91, -74, -85, -85, 124, -21,
        36, -45, 5, -60, -19, 117, -103, 12, 1, -79, 53, 18, -112, 95, 27, 54, -16, 35, 32, -50,
        -6, -75, 113, -93, -46, -81, -124, -114, 113, 56, -117, 94, -26, 57, 15, 25, -2, -39, -70, 85,
        104, -89, -118, 30, 61, -86, -52, 67, 116, 1, 113, 123, 93, 38, 67, 64, 108, -115, 4, -124,
        12, -33, -72, -31, -40, 90, -116, -3, -19, -21, -85, -6, -12, -119, -114, -124, 20, 50, 25, -95,
        111, -1, -34, -67, 74, 63, -70, -9, 119, 86, 33, -105, -29, 47, -2, -28, -29, -23, -70, -128,
        52, 99, 93, -90, 66, 64, 108, -62, -63, 61, -21, -37, -105, 110, 75, 106, 73, 92, -70, 84,
        30, 23, 94, -72, -112, -27, -93, -47, -54, 88, 86, 60, 16, 72, 27, -108, -18, -3, -23, -123,
        27, 63, 52, 88, 40, 8, 125, 106, 29, -25, -8, -71, 116, 105, -43, 83, -68, 116, 52, 61,
        113, -11, 42, 27, 56, 120, -112, 63, 113, -54, -123, -61, 66, -69, 28, 16, -73, -41, 101, 42,
        4, -60, -90, -100, 108, -43, -108, -68, -119, -24, 49, 42, 5, 103, 74, 101, 109, -56, -111, -37,
        -73, -7, 6, -106, -41, 80, 66, -33, -12, -103, -49, -97, -27, -111, -38, -110, 3, -46, -116, 117,
        -103, 8, 1, -79, -55, -73, 25, -70, 26, 125, -9, 78, 25, 79, 47, -24, -24, -87, 19, -99,
        123, -86, 86, 116, 52, -99, -34, 112, -45, -17, 12, 101, 108, 13, -12, 127, 57, 114, -35, -35,
        -54, 123, 13, 103, -47, -31, 70, -70, -35, -110, 79, -13, 54, 115, 93, 38, 65, 64, -102, 32,
        -68, 120, 49, 63, -20, -57, 111, -121, 118, -17, -26, 87, 4, 122, -127, 39, -9, -101, -86, -48,
        -4, -7, 124, -114, 62, -65, -97, 111, 118, -70, -59, -22, -35, -66, -67, -18, 99, -23, 110, -81,
        -53, 4, 8, 8, -128, 6, 2, 2, -96, -127, -128, 0, 104, 32, 32, 0, 26, 8, 8, -128,
        6, 2, 2, -96, -127, -128, 0, 104, 32, 32, 0, 26, 8, 8, -128, 6, 2, 2, -96, -127,
        -128, 0, 104, 32, 32, 0, 26, 8, 8, -128, 6, 2, 2, -96, 65, 1, 73, -54, 31, 2,
        0, -105, -12, -124, -68, -34, 79, 85, 26, 0, -116, 71, -39, -96, 43, -120, 95, 110, 0, 0,
        -50, -17, -23, -12, -7, 102, 88, 73, -23, -88, -46, 8, 96, 44, -54, 4, 101, -61, 67, -43,
        -27, -11, -50, 68, 72, 0, -118, 40, 11, -108, 9, 30, -114, 82, 81, 90, -126, 45, 45, 109,
        86, -121, -128, 37, 37, 15, 2, -8, -49, -47, -98, 15, 80, 6, -54, 87, 14, -85, -2, 1,
        74, -19, -67, -115, 7, 50, 123, -9, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126,
    )

    /** All fixtures for parameterized testing. */
    public val all: List<InvoiceDocument> by lazy {
        listOf(basic, fullFeatured, negativeValues, long, minimal, styled, linksAndImages)
    }

    /** Single brand, 3 line items, no sub-items, no discounts. */
    public val basic: InvoiceDocument = invoice {
        header {
            branding {
                primary {
                    name("Acme Corp")
                    logo(TEST_LOGO_PNG, "image/png")
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
                    logo(TEST_LOGO_PNG, "image/png")
                    address("456 Oak Ave", "Boulder, CO 80301")
                    website("https://clientstore.com")
                }
                poweredBy {
                    name("Acme Payments")
                    logo(TEST_LOGO_PNG, "image/png")
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
        metaBlock {
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

    /** Exercises every link type (website, email, phone, payment) and image path (logo, custom). */
    public val linksAndImages: InvoiceDocument = invoice {
        header {
            branding {
                primary {
                    name("Image & Link Corp")
                    logo(TEST_LOGO_PNG, "image/png")
                    address("1 Link Lane", "Webtown, CA 90000")
                    email("brand@example.com")
                    phone("+1-555-0100")
                    website("https://example.com")
                }
            }
            invoiceNumber("INV-LINK-001")
            issueDate(LocalDate(2026, 4, 1))
            dueDate(LocalDate(2026, 5, 1))
        }
        billFrom {
            name("Image & Link Corp")
            email("from@example.com")
            phone("+1-555-0200")
        }
        billTo {
            name("Link Client")
            email("to@example.com")
            phone("+1-555-0300")
        }
        lineItems {
            columns("Description", "Amount")
            item("Service", amount = 100.0)
        }
        summary { currency("USD") }
        paymentInfo {
            paymentLink("https://pay.example.com/inv-001")
        }
        custom("image-test") {
            image(TEST_LOGO_PNG, "image/png", width = 100, height = 100)
            link("Visit Our Website", "https://example.com/custom-link")
        }
        footer {
            notes("Links and images test fixture.")
        }
    }
}

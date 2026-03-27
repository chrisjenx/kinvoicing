package com.chrisjenx.kinvoicing

import kotlinx.datetime.LocalDate
import kotlin.test.*
import kotlin.test.assertFailsWith

class DslBuilderTest {

    @Test
    fun basicInvoiceBuildsSections() {
        val doc = invoice {
            header {
                invoiceNumber("INV-001")
                issueDate(LocalDate(2026, 1, 1))
                dueDate(LocalDate(2026, 1, 31))
            }
            billTo {
                name("Jane Smith")
                address("123 Main St")
            }
            lineItems {
                columns("Description", "Amount")
                item("Service", amount = 100.0)
            }
            summary {
                currency("USD")
            }
        }

        val sectionTypes = doc.sections.map { it::class }
        assertTrue(InvoiceSection.Header::class in sectionTypes)
        assertTrue(InvoiceSection.BillTo::class in sectionTypes)
        assertTrue(InvoiceSection.LineItems::class in sectionTypes)
        assertTrue(InvoiceSection.Summary::class in sectionTypes)
    }

    @Test
    fun headerContainsCorrectValues() {
        val doc = invoice {
            header {
                invoiceNumber("INV-001")
                issueDate(LocalDate(2026, 3, 1))
                dueDate(LocalDate(2026, 3, 31))
            }
            billTo { name("Test") }
            lineItems {
                item("Item", amount = 1.0)
            }
            summary {}
        }

        val header = doc.sections.filterIsInstance<InvoiceSection.Header>().single()
        assertEquals("INV-001", header.invoiceNumber)
        assertEquals(LocalDate(2026, 3, 1), header.issueDate)
        assertEquals(LocalDate(2026, 3, 31), header.dueDate)
    }

    @Test
    fun billToAndBillFromBuild() {
        val doc = invoice {
            billFrom {
                name("Seller Corp")
                address("123 Sell St", "Suite 1")
                email("sell@corp.com")
                phone("555-0100")
            }
            billTo {
                name("Buyer Inc")
                address("456 Buy Ave")
                email("buy@inc.com")
            }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }

        val from = doc.sections.filterIsInstance<InvoiceSection.BillFrom>().single()
        assertEquals("Seller Corp", from.contact.name)
        assertEquals(listOf("123 Sell St", "Suite 1"), from.contact.address)
        assertEquals("sell@corp.com", from.contact.email)
        assertEquals("555-0100", from.contact.phone)

        val to = doc.sections.filterIsInstance<InvoiceSection.BillTo>().single()
        assertEquals("Buyer Inc", to.contact.name)
    }

    @Test
    fun lineItemsWithQtyAndUnitPrice() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                columns("Description", "Qty", "Rate", "Amount")
                item("Consulting", qty = 10, unitPrice = 150.0)
            }
            summary {}
        }

        val items = doc.sections.filterIsInstance<InvoiceSection.LineItems>().single()
        assertEquals(1, items.rows.size)
        assertEquals("Consulting", items.rows[0].description)
        assertEquals(10.0, items.rows[0].quantity)
        assertEquals(150.0, items.rows[0].unitPrice)
        assertEquals(1500.0, items.rows[0].amount)
    }

    @Test
    fun lineItemWithSubItems() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Main item", qty = 1, unitPrice = 100.0) {
                    sub("Sub A", qty = 1, unitPrice = 60.0)
                    sub("Sub B", amount = 40.0)
                }
            }
            summary {}
        }

        val item = doc.sections.filterIsInstance<InvoiceSection.LineItems>().single().rows[0]
        assertEquals(2, item.subItems.size)
        assertEquals("Sub A", item.subItems[0].description)
        assertEquals(60.0, item.subItems[0].amount)
        assertEquals("Sub B", item.subItems[1].description)
        assertEquals(40.0, item.subItems[1].amount)
    }

    @Test
    fun lineItemWithDiscount() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Service", qty = 10, unitPrice = 100.0) {
                    discount("10% off", percent = 10.0)
                }
            }
            summary {}
        }

        val item = doc.sections.filterIsInstance<InvoiceSection.LineItems>().single().rows[0]
        // 10 * 100 = 1000, minus 10% = 900
        assertEquals(900.0, item.amount)
        assertEquals(1, item.discounts.size)
    }

    @Test
    fun negativeAmountLineItem() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Credit", amount = -500.0)
            }
            summary {}
        }

        val item = doc.sections.filterIsInstance<InvoiceSection.LineItems>().single().rows[0]
        assertEquals(-500.0, item.amount)
    }

    @Test
    fun metaBlocksMerge() {
        val doc = invoice {
            billTo { name("Test") }
            metaBlock {
                entry("PO", "PO-001")
            }
            metaBlock {
                entry("Project", "Alpha")
            }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }

        val meta = doc.sections.filterIsInstance<InvoiceSection.MetaBlock>().single()
        assertEquals(2, meta.entries.size)
        assertEquals("PO", meta.entries[0].label)
        assertEquals("PO-001", meta.entries[0].value)
        assertEquals("Project", meta.entries[1].label)
        assertEquals("Alpha", meta.entries[1].value)
    }

    @Test
    fun sectionCanonicalOrder() {
        // Declare out of order
        val doc = invoice {
            footer { notes("Footer") }
            billTo { name("Customer") }
            header { invoiceNumber("INV-001") }
            lineItems { item("Item", amount = 100.0) }
            summary {}
            billFrom { name("Seller") }
            metaBlock { entry("Key", "Val") }
            paymentInfo { bankName("Bank") }
        }

        val types = doc.sections.map { it::class.simpleName }
        val expected = listOf("Header", "BillFrom", "BillTo", "MetaBlock", "LineItems", "Summary", "PaymentInfo", "Footer")
        assertEquals(expected, types)
    }

    @Test
    fun customSectionEscapeHatch() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
            custom("disclaimer") {
                text("Important notice")
                divider()
                text("Terms apply")
            }
        }

        val custom = doc.sections.filterIsInstance<InvoiceSection.Custom>().single()
        assertEquals("disclaimer", custom.key)
        assertEquals(3, custom.content.size)
        assertTrue(custom.content[0] is InvoiceElement.Text)
        assertTrue(custom.content[1] is InvoiceElement.Divider)
    }

    @Test
    fun brandingBuilder() {
        val doc = invoice {
            header {
                branding {
                    primary {
                        name("Primary Co")
                        address("123 Main St")
                        email("hello@primary.com")
                        website("https://primary.com")
                    }
                    poweredBy {
                        name("Platform Inc")
                        tagline("Powered by Platform")
                    }
                    layout = BrandLayout.DUAL_HEADER
                }
            }
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }

        val header = doc.sections.filterIsInstance<InvoiceSection.Header>().single()
        assertNotNull(header.branding)
        assertEquals("Primary Co", header.branding!!.primary.name)
        assertNotNull(header.branding!!.poweredBy)
        assertEquals("Platform Inc", header.branding!!.poweredBy!!.name)
        assertEquals(BrandLayout.DUAL_HEADER, header.branding!!.layout)
    }

    @Test
    fun styleBuilderAppliesValues() {
        val doc = invoice {
            style {
                primaryColor = 0xFFFF0000
                fontFamily = "Helvetica"
                showGridLines = true
                accentBorder = true
                logoPlacement = LogoPlacement.CENTER
                headerLayout = HeaderLayout.STACKED
            }
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }

        assertEquals(ArgbColor(0xFFFF0000), doc.style.primaryColor)
        assertEquals("Helvetica", doc.style.fontFamily)
        assertTrue(doc.style.showGridLines)
        assertTrue(doc.style.accentBorder)
        assertEquals(LogoPlacement.CENTER, doc.style.logoPlacement)
        assertEquals(HeaderLayout.STACKED, doc.style.headerLayout)
    }

    @Test
    fun defaultStyleValues() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }

        assertEquals(InvoiceStyle(), doc.style)
    }

    @Test
    fun paymentInfoBuilder() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
            paymentInfo {
                bankName("First National")
                accountNumber("****4242")
                routingNumber("021000021")
                paymentLink("https://pay.example.com")
                qrCodeData("https://qr.example.com")
                notes("Wire preferred")
            }
        }

        val payment = doc.sections.filterIsInstance<InvoiceSection.PaymentInfo>().single()
        assertEquals("First National", payment.bankName)
        assertEquals("****4242", payment.accountNumber)
        assertEquals("021000021", payment.routingNumber)
        assertEquals("https://pay.example.com", payment.paymentLink)
    }

    @Test
    fun lineItemWithoutAmountSourceFails() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                billTo { name("Test") }
                lineItems { item("No amount") }
                summary {}
            }
        }
    }

    @Test
    fun invalidCurrencyCodeFails() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                billTo { name("Test") }
                lineItems { item("Item", amount = 1.0) }
                summary { currency("usd") }
            }
        }
    }

    @Test
    fun documentLevelCurrency() {
        val doc = invoice {
            currency("EUR")
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }
        assertEquals("EUR", doc.currency)
    }

    @Test
    fun summaryCurrencyOverridesDocumentLevel() {
        val doc = invoice {
            currency("USD")
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary { currency("GBP") }
        }
        assertEquals("GBP", doc.currency)
    }

    @Test
    fun customSectionImage() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
            custom("img") {
                image(InvoiceFixtures.TEST_LOGO_PNG, "image/png", width = 50, height = 50)
            }
        }

        val custom = doc.sections.filterIsInstance<InvoiceSection.Custom>().single()
        val image = custom.content.single() as InvoiceElement.Image
        assertEquals("image/png", image.source.contentType)
        assertEquals(50, image.width)
        assertEquals(50, image.height)
        assertTrue(image.source.bytes.contentEquals(InvoiceFixtures.TEST_LOGO_PNG))
    }

    @Test
    fun customSectionImageSource() {
        val source = ImageSource.Bytes(InvoiceFixtures.TEST_LOGO_PNG, "image/jpeg")
        val doc = invoice {
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
            custom("img") {
                image(source, width = 80, height = 40)
            }
        }

        val custom = doc.sections.filterIsInstance<InvoiceSection.Custom>().single()
        val image = custom.content.single() as InvoiceElement.Image
        assertEquals(source, image.source)
        assertEquals(80, image.width)
        assertEquals(40, image.height)
    }

    @Test
    fun brandingLogoImageSource() {
        val source = ImageSource.Bytes(InvoiceFixtures.TEST_LOGO_PNG, "image/png")
        val doc = invoice {
            header {
                branding {
                    primary {
                        name("Test Corp")
                        logo(source)
                    }
                }
            }
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }

        val header = doc.sections.filterIsInstance<InvoiceSection.Header>().single()
        assertEquals(source, header.branding!!.primary.logo)
    }

    @Test
    fun customSectionLink() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
            custom("links") {
                link("Visit Our Site", "https://example.com")
            }
        }

        val custom = doc.sections.filterIsInstance<InvoiceSection.Custom>().single()
        val link = custom.content.single() as InvoiceElement.Link
        assertEquals("Visit Our Site", link.text)
        assertEquals("https://example.com", link.href)
    }

    @Test
    fun allFixturesBuildWithoutError() {
        InvoiceFixtures.all.forEachIndexed { i, doc ->
            assertTrue(doc.sections.isNotEmpty(), "Fixture $i has no sections")
        }
    }
}

package com.chrisjenx.kinvoicing

import kotlinx.datetime.LocalDate
import kotlin.test.*

class InvoiceSectionTest {

    @Test
    fun allSealedVariantsConstructible() {
        val sections: List<InvoiceSection> = listOf(
            InvoiceSection.Header(invoiceNumber = "INV-001"),
            InvoiceSection.BillFrom(ContactInfo(name = "Seller")),
            InvoiceSection.BillTo(ContactInfo(name = "Buyer")),
            InvoiceSection.LineItems(
                columns = listOf(
                    ColumnHeader(LineItemColumn.DESCRIPTION, "Desc"),
                    ColumnHeader(LineItemColumn.AMOUNT, "Amount"),
                ),
                rows = listOf(LineItem(description = "Item", amount = 100.0))
            ),
            InvoiceSection.Summary(
                subtotal = 100.0,
                adjustments = emptyList(),
                total = 100.0,
            ),
            InvoiceSection.PaymentInfo(bankName = "Bank"),
            InvoiceSection.Footer(notes = "Thanks"),
            InvoiceSection.Custom(
                key = "custom",
                content = listOf(InvoiceElement.Text("Hello"))
            ),
            InvoiceSection.MetaBlock(
                entries = listOf(MetaEntry("Key", "Value"))
            ),
        )

        assertEquals(9, sections.size)
    }

    @Test
    fun exhaustiveMatchCompiles() {
        val section: InvoiceSection = InvoiceSection.Header(invoiceNumber = "INV-001")
        val name = when (section) {
            is InvoiceSection.Header -> "header"
            is InvoiceSection.BillFrom -> "billFrom"
            is InvoiceSection.BillTo -> "billTo"
            is InvoiceSection.LineItems -> "lineItems"
            is InvoiceSection.Summary -> "summary"
            is InvoiceSection.PaymentInfo -> "paymentInfo"
            is InvoiceSection.Footer -> "footer"
            is InvoiceSection.Custom -> "custom"
            is InvoiceSection.MetaBlock -> "metaBlock"
        }
        assertEquals("header", name)
    }

    @Test
    fun headerDefaults() {
        val header = InvoiceSection.Header()
        assertNull(header.branding)
        assertNull(header.invoiceNumber)
        assertNull(header.issueDate)
        assertNull(header.dueDate)
    }

    @Test
    fun lineItemDataIntegrity() {
        val item = LineItem(
            description = "Test",
            quantity = 5.0,
            unitPrice = 10.0,
            amount = 50.0,
            metadata = mapOf("category" to "services"),
            subItems = listOf(LineSubItem("Sub", amount = 25.0)),
            discounts = listOf(
                Adjustment("Disc", AdjustmentType.DISCOUNT, AdjustmentValue.Percent(10.0))
            ),
        )

        assertEquals("Test", item.description)
        assertEquals(5.0, item.quantity)
        assertEquals(10.0, item.unitPrice)
        assertEquals(50.0, item.amount)
        assertEquals(1, item.subItems.size)
        assertEquals(1, item.discounts.size)
    }

    @Test
    fun adjustmentValueSealed() {
        val values: List<AdjustmentValue> = listOf(
            AdjustmentValue.Percent(10.0),
            AdjustmentValue.Fixed(-50.0),
            AdjustmentValue.Absolute(100.0),
        )

        values.forEach { value ->
            when (value) {
                is AdjustmentValue.Percent -> assertEquals(10.0, value.rate)
                is AdjustmentValue.Fixed -> assertEquals(-50.0, value.amount)
                is AdjustmentValue.Absolute -> assertEquals(100.0, value.amount)
            }
        }
    }

    @Test
    fun invoiceElementSealed() {
        val elements: List<InvoiceElement> = listOf(
            InvoiceElement.Text("Hello"),
            InvoiceElement.Spacer(24),
            InvoiceElement.Divider,
            InvoiceElement.Row(listOf(InvoiceElement.Text("A")), listOf(1f)),
            InvoiceElement.Image(byteArrayOf(1, 2, 3), "image/png", 100, 50),
        )

        assertEquals(5, elements.size)
        elements.forEach { el ->
            when (el) {
                is InvoiceElement.Text -> assertEquals("Hello", el.value)
                is InvoiceElement.Spacer -> assertEquals(24, el.height)
                is InvoiceElement.Divider -> {} // singleton
                is InvoiceElement.Row -> assertEquals(1, el.children.size)
                is InvoiceElement.Image -> assertEquals("image/png", el.contentType)
            }
        }
    }

    @Test
    fun brandingModel() {
        val branding = Branding(
            primary = BrandIdentity(name = "Primary"),
            poweredBy = BrandIdentity(name = "Platform", tagline = "Powered by Platform"),
            layout = BrandLayout.POWERED_BY_HEADER,
        )

        assertEquals("Primary", branding.primary.name)
        assertEquals("Platform", branding.poweredBy?.name)
        assertEquals(BrandLayout.POWERED_BY_HEADER, branding.layout)
    }
}

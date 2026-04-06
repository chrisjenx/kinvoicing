package com.chrisjenx.kinvoicing

import kotlin.test.*

class InvoiceStatusTest {

    @Test
    fun predefinedStatusesHaveExpectedLabels() {
        assertEquals("DRAFT", InvoiceStatus.Draft.label)
        assertEquals("SENT", InvoiceStatus.Sent.label)
        assertEquals("PAID", InvoiceStatus.Paid.label)
        assertEquals("OVERDUE", InvoiceStatus.Overdue.label)
        assertEquals("VOID", InvoiceStatus.Void.label)
        assertEquals("UNCOLLECTABLE", InvoiceStatus.Uncollectable.label)
        assertEquals("REFUNDED", InvoiceStatus.Refunded.label)
    }

    @Test
    fun predefinedStatusesHaveDistinctColors() {
        val colors = listOf(
            InvoiceStatus.Draft, InvoiceStatus.Sent, InvoiceStatus.Paid,
            InvoiceStatus.Overdue, InvoiceStatus.Void, InvoiceStatus.Uncollectable,
            InvoiceStatus.Refunded,
        ).map { it.color.value }.toSet()
        assertEquals(7, colors.size, "All predefined statuses should have unique colors")
    }

    @Test
    fun customStatusUsesProvidedValues() {
        val status = InvoiceStatus.Custom("PENDING APPROVAL", ArgbColor(0xFFF59E0B))
        assertEquals("PENDING APPROVAL", status.label)
        assertEquals(0xFFF59E0B, status.color.value)
    }

    @Test
    fun customStatusEquality() {
        val a = InvoiceStatus.Custom("TEST", ArgbColor(0xFF000000))
        val b = InvoiceStatus.Custom("TEST", ArgbColor(0xFF000000))
        assertEquals(a, b)
    }

    @Test
    fun documentDefaultsToNullStatus() {
        val doc = invoice {
            header { invoiceNumber("INV-001") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }
        assertNull(doc.status)
        assertEquals(StatusDisplay.Badge, doc.statusDisplay)
    }

    @Test
    fun dslSimpleStatusSetter() {
        val doc = invoice {
            status(InvoiceStatus.Paid)
            header { invoiceNumber("INV-001") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }
        assertEquals(InvoiceStatus.Paid, doc.status)
        assertEquals(StatusDisplay.Badge, doc.statusDisplay)
    }

    @Test
    fun dslStatusBuilderWithDisplayMode() {
        val doc = invoice {
            status {
                paid()
                watermark(0.10f)
            }
            header { invoiceNumber("INV-001") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }
        assertEquals(InvoiceStatus.Paid, doc.status)
        val display = doc.statusDisplay
        assertIs<StatusDisplay.Watermark>(display)
        assertEquals(0.10f, display.opacity)
    }

    @Test
    fun dslStatusBuilderCustomWithBanner() {
        val doc = invoice {
            status {
                custom("PARTIAL PAYMENT", 0xFFF59E0B)
                banner()
            }
            header { invoiceNumber("INV-001") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }
        val status = doc.status
        assertIs<InvoiceStatus.Custom>(status)
        assertEquals("PARTIAL PAYMENT", status.label)
        assertEquals(StatusDisplay.Banner, doc.statusDisplay)
    }

    @Test
    fun dslStatusBuilderHidden() {
        val doc = invoice {
            status {
                voided()
                hidden()
            }
            header { invoiceNumber("INV-001") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }
        assertEquals(InvoiceStatus.Void, doc.status)
        assertEquals(StatusDisplay.None, doc.statusDisplay)
    }

    @Test
    fun dslStatusBuilderStamp() {
        val doc = invoice {
            status {
                overdue()
                stamp(0.20f)
            }
            header { invoiceNumber("INV-001") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }
        assertEquals(InvoiceStatus.Overdue, doc.status)
        val display = doc.statusDisplay
        assertIs<StatusDisplay.Stamp>(display)
        assertEquals(0.20f, display.opacity)
    }

    @Test
    fun allStatusBuilderConvenienceMethods() {
        val statuses = listOf(
            InvoiceStatus.Draft to "draft",
            InvoiceStatus.Sent to "sent",
            InvoiceStatus.Paid to "paid",
            InvoiceStatus.Overdue to "overdue",
            InvoiceStatus.Void to "voided",
            InvoiceStatus.Uncollectable to "uncollectable",
            InvoiceStatus.Refunded to "refunded",
        )
        for ((expected, _) in statuses) {
            val doc = invoice {
                status(expected)
                header { invoiceNumber("INV-001") }
                lineItems { item("Item", amount = 1.0) }
                summary {}
            }
            assertEquals(expected, doc.status)
        }
    }

    @Test
    fun statusDisplayDefaultOpacities() {
        assertEquals(0.08f, StatusDisplay.Watermark().opacity)
        assertEquals(0.15f, StatusDisplay.Stamp().opacity)
    }
}

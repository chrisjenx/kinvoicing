package com.chrisjenx.kinvoicing.compose

import com.chrisjenx.kinvoicing.*
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * End-to-end tests exercising the full DSL → InvoiceDocument → renderer wiring
 * pipeline on every platform. These verify that the IR, DSL, image decoding,
 * and composable locals all work together correctly.
 */
class InvoiceRenderE2eTest {

    @Test
    fun allFixturesProduceValidDocuments() {
        InvoiceFixtures.all.forEachIndexed { i, doc ->
            assertTrue(doc.sections.isNotEmpty(), "Fixture $i should have sections")
        }
    }

    @Test
    fun dslProducesCompleteDocument() {
        val doc = invoice {
            header {
                branding { primary { name("E2E Corp") } }
                invoiceNumber("INV-E2E-001")
                issueDate(LocalDate(2026, 4, 1))
                dueDate(LocalDate(2026, 5, 1))
            }
            billFrom {
                name("E2E Corp")
                address("100 Test St")
                email("test@e2e.com")
            }
            billTo {
                name("Customer Inc")
                address("200 Verify Ave")
            }
            lineItems {
                columns("Description", "Qty", "Rate", "Amount")
                item("Platform Testing", qty = 1, unitPrice = 500.0)
                item("Cross-platform Verification", qty = 2, unitPrice = 250.0)
            }
            summary { currency("USD") }
            footer {
                notes("Tested on all platforms")
                terms("Net 30")
            }
        }

        assertTrue(doc.sections.size >= 6, "Should have at least 6 sections, got ${doc.sections.size}")
        val header = doc.sections.filterIsInstance<InvoiceSection.Header>().single()
        assertEquals("INV-E2E-001", header.invoiceNumber)
    }

    @Test
    fun imageSourceBytesRoundTrip() {
        val logo = ImageSource.Bytes(InvoiceFixtures.TEST_LOGO_PNG, "image/png")
        val decoded = decodeImageBytes(logo.bytes)
        assertEquals(200, decoded.intrinsicWidth)
        assertEquals(60, decoded.intrinsicHeight)
    }

    @Test
    fun themedDocumentPreservesStyle() {
        InvoiceThemes.all.forEach { (name, theme) ->
            val doc = invoice {
                style { theme(theme) }
                billTo { name("Customer") }
                lineItems { item("Service", amount = 100.0) }
                summary {}
            }
            assertNotNull(doc.style, "Theme '$name' should produce a style")
            assertTrue(doc.style.primaryColor.value != 0L, "Theme '$name' should have a primary color")
        }
    }

    @Test
    fun fixtureWithLogoDecodesOnAllPlatforms() {
        val doc = InvoiceFixtures.basic
        val header = doc.sections.filterIsInstance<InvoiceSection.Header>().single()
        val logo = header.branding?.primary?.logo
        assertNotNull(logo, "Basic fixture should have a logo")
        val decoded = decodeImageBytes(logo.bytes)
        assertTrue(decoded.intrinsicWidth > 0, "Logo should decode to positive width")
        assertTrue(decoded.intrinsicHeight > 0, "Logo should decode to positive height")
    }
}

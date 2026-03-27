package com.chrisjenx.kinvoicing

import kotlin.math.abs
import kotlin.test.*

class SummaryComputationTest {

    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 0.01) {
        assertTrue(abs(expected - actual) < tolerance, "Expected $expected but got $actual")
    }

    @Test
    fun subtotalFromLineItems() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("A", amount = 100.0)
                item("B", amount = 200.0)
                item("C", amount = 50.0)
            }
            summary {}
        }

        val summary = doc.sections.filterIsInstance<InvoiceSection.Summary>().single()
        assertClose(350.0, summary.subtotal)
        assertClose(350.0, summary.total)
    }

    @Test
    fun subtotalWithNegativeAmounts() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Service", amount = 1000.0)
                item("Credit", amount = -200.0)
            }
            summary {}
        }

        val summary = doc.sections.filterIsInstance<InvoiceSection.Summary>().single()
        assertClose(800.0, summary.subtotal)
        assertClose(800.0, summary.total)
    }

    @Test
    fun percentDiscount() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Service", amount = 1000.0)
            }
            summary {
                discount("10% off", percent = 10.0)
            }
        }

        val summary = doc.sections.filterIsInstance<InvoiceSection.Summary>().single()
        assertClose(1000.0, summary.subtotal)
        assertClose(900.0, summary.total)
    }

    @Test
    fun fixedDiscount() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Service", amount = 1000.0)
            }
            summary {
                discount("Flat discount", fixed = -50.0)
            }
        }

        val summary = doc.sections.filterIsInstance<InvoiceSection.Summary>().single()
        assertClose(1000.0, summary.subtotal)
        assertClose(950.0, summary.total)
    }

    @Test
    fun percentTax() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Service", amount = 1000.0)
            }
            summary {
                tax("Sales Tax", percent = 8.0)
            }
        }

        val summary = doc.sections.filterIsInstance<InvoiceSection.Summary>().single()
        assertClose(1000.0, summary.subtotal)
        assertClose(1080.0, summary.total)
    }

    @Test
    fun fixedFee() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Service", amount = 1000.0)
            }
            summary {
                fee("Processing fee", fixed = 25.0)
            }
        }

        val summary = doc.sections.filterIsInstance<InvoiceSection.Summary>().single()
        assertClose(1000.0, summary.subtotal)
        assertClose(1025.0, summary.total)
    }

    @Test
    fun credit() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Service", amount = 1000.0)
            }
            summary {
                credit("Account credit", 100.0)
            }
        }

        val summary = doc.sections.filterIsInstance<InvoiceSection.Summary>().single()
        assertClose(1000.0, summary.subtotal)
        assertClose(900.0, summary.total)
    }

    @Test
    fun adjustmentOrderDiscountThenFeeThenTax() {
        // Subtotal: 1000
        // Discount 10%: 1000 - 100 = 900
        // Fee +25: 900 + 25 = 925
        // Tax 8%: 925 + 74 = 999
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Service", amount = 1000.0)
            }
            summary {
                tax("Tax", percent = 8.0)
                discount("Discount", percent = 10.0)
                fee("Fee", fixed = 25.0)
            }
        }

        val summary = doc.sections.filterIsInstance<InvoiceSection.Summary>().single()
        assertClose(1000.0, summary.subtotal)
        assertClose(999.0, summary.total)
    }

    @Test
    fun itemLevelDiscountReducesSubtotal() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Service", qty = 10, unitPrice = 100.0) {
                    discount("10% off", percent = 10.0)
                }
            }
            summary {}
        }

        val summary = doc.sections.filterIsInstance<InvoiceSection.Summary>().single()
        // 10*100 = 1000, minus 10% = 900
        assertClose(900.0, summary.subtotal)
        assertClose(900.0, summary.total)
    }

    @Test
    fun itemLevelAndSummaryLevelAdjustments() {
        // Item: 1000, item discount 10% = 900
        // Summary tax 5%: 900 + 45 = 945
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Service", qty = 10, unitPrice = 100.0) {
                    discount("10% off", percent = 10.0)
                }
            }
            summary {
                tax("Tax", percent = 5.0)
            }
        }

        val summary = doc.sections.filterIsInstance<InvoiceSection.Summary>().single()
        assertClose(900.0, summary.subtotal)
        assertClose(945.0, summary.total)
    }

    @Test
    fun emptyLineItemsZeroTotal() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems { }
            summary {}
        }

        val summary = doc.sections.filterIsInstance<InvoiceSection.Summary>().single()
        assertClose(0.0, summary.subtotal)
        assertClose(0.0, summary.total)
    }

    @Test
    fun qtyTimesUnitPriceAutoCalculation() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Service", qty = 5, unitPrice = 20.0)
            }
            summary {}
        }

        val items = doc.sections.filterIsInstance<InvoiceSection.LineItems>().single()
        assertClose(100.0, items.rows[0].amount)
    }

    @Test
    fun subItemRollupWhenNoExplicitAmount() {
        val doc = invoice {
            billTo { name("Test") }
            lineItems {
                item("Bundle") {
                    sub("Part A", amount = 30.0)
                    sub("Part B", amount = 70.0)
                }
            }
            summary {}
        }

        val item = doc.sections.filterIsInstance<InvoiceSection.LineItems>().single().rows[0]
        assertClose(100.0, item.amount)
    }

    @Test
    fun fullFeaturedFixtureComputation() {
        val summary = InvoiceFixtures.fullFeatured.sections
            .filterIsInstance<InvoiceSection.Summary>().single()

        // Consulting: 40*150=6000, minus 10% = 5400
        // Travel: 1*350 = 350
        // Credit: -500
        // Subtotal: 5400 + 350 + (-500) = 5250
        assertClose(5250.0, summary.subtotal)
        assertTrue(summary.total > 0, "Total should be positive")
        assertTrue(summary.adjustments.isNotEmpty(), "Should have adjustments")
    }
}

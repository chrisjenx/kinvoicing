package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.invoice
import kotlin.test.Test
import kotlin.test.assertFailsWith

/** Tests that DSL builders reject malicious input at construction time. */
class SecurityTest {

    // ── URL scheme validation ──

    @Test
    fun websiteRejectsJavascriptUrl() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                header {
                    branding {
                        primary {
                            name("Test")
                            website("javascript:alert(1)")
                        }
                    }
                }
                lineItems { item("Item", amount = 1.0) }
                summary {}
            }
        }
    }

    @Test
    fun websiteRejectsDataUrl() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                header {
                    branding {
                        primary {
                            name("Test")
                            website("data:text/html,<script>alert(1)</script>")
                        }
                    }
                }
                lineItems { item("Item", amount = 1.0) }
                summary {}
            }
        }
    }

    @Test
    fun websiteAcceptsHttpsUrl() {
        invoice {
            header {
                branding {
                    primary {
                        name("Test")
                        website("https://example.com")
                    }
                }
            }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }
    }

    @Test
    fun paymentLinkRejectsJavascriptUrl() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                lineItems { item("Item", amount = 1.0) }
                summary {}
                paymentInfo { paymentLink("javascript:alert(1)") }
            }
        }
    }

    @Test
    fun paymentLinkRejectsVbscriptUrl() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                lineItems { item("Item", amount = 1.0) }
                summary {}
                paymentInfo { paymentLink("vbscript:MsgBox") }
            }
        }
    }

    @Test
    fun customLinkRejectsJavascriptUrl() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                lineItems { item("Item", amount = 1.0) }
                summary {}
                custom("xss") { link("Click me", "javascript:alert(1)") }
            }
        }
    }

    @Test
    fun customLinkAcceptsHttpsUrl() {
        invoice {
            lineItems { item("Item", amount = 1.0) }
            summary {}
            custom("safe") { link("Click me", "https://example.com") }
        }
    }

    // ── NaN/Infinity validation ──

    @Test
    fun lineItemRejectsNaNAmount() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                lineItems { item("Item", amount = Double.NaN) }
                summary {}
            }
        }
    }

    @Test
    fun lineItemRejectsInfinityAmount() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                lineItems { item("Item", amount = Double.POSITIVE_INFINITY) }
                summary {}
            }
        }
    }

    @Test
    fun lineItemRejectsNaNQuantity() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                lineItems { item("Item", qty = Double.NaN, unitPrice = 10.0) }
                summary {}
            }
        }
    }

    @Test
    fun discountRejectsNaNPercent() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                lineItems {
                    item("Item", amount = 100.0) {
                        discount("Bad", percent = Double.NaN)
                    }
                }
                summary {}
            }
        }
    }

    @Test
    fun summaryTaxRejectsInfinityPercent() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                lineItems { item("Item", amount = 100.0) }
                summary { tax("Tax", percent = Double.POSITIVE_INFINITY) }
            }
        }
    }

    @Test
    fun summaryCreditRejectsNaN() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                lineItems { item("Item", amount = 100.0) }
                summary { credit("Credit", amount = Double.NaN) }
            }
        }
    }

    // ── CSS injection via fontFamily ──

    @Test
    fun fontFamilyRejectsSemicolon() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                style { fontFamily = "Inter; background: url(evil)" }
                lineItems { item("Item", amount = 1.0) }
                summary {}
            }
        }
    }

    @Test
    fun fontFamilyRejectsBraces() {
        assertFailsWith<IllegalArgumentException> {
            invoice {
                style { fontFamily = "Inter} body { display:none" }
                lineItems { item("Item", amount = 1.0) }
                summary {}
            }
        }
    }

    @Test
    fun fontFamilyAcceptsNormalName() {
        invoice {
            style { fontFamily = "Fira Code" }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }
    }
}

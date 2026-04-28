package com.chrisjenx.kinvoicing

import kotlin.test.Test
import kotlin.test.assertEquals

class InvoiceElementTest {
    @Test
    fun linkDefaultsToTextStyle() {
        val link = InvoiceElement.Link(text = "Pay Now", href = "https://pay.example.com")
        assertEquals(LinkStyle.TEXT, link.style)
    }

    @Test
    fun linkAcceptsButtonStyle() {
        val link = InvoiceElement.Link(
            text = "Pay Now",
            href = "https://pay.example.com",
            style = LinkStyle.BUTTON,
        )
        assertEquals(LinkStyle.BUTTON, link.style)
    }
}

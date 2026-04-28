package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.InvoiceElement
import com.chrisjenx.kinvoicing.LinkStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ContentBuilderTest {
    @Test
    fun textAddsTextElement() {
        val out = ContentBuilder().apply { text("hello") }.buildPublic()
        assertEquals(1, out.size)
        val t = assertIs<InvoiceElement.Text>(out[0])
        assertEquals("hello", t.value)
    }

    @Test
    fun linkAddsTextStyleLink() {
        val out = ContentBuilder().apply { link("Pay Now", "https://pay.example.com") }.buildPublic()
        val l = assertIs<InvoiceElement.Link>(out[0])
        assertEquals("Pay Now", l.text)
        assertEquals("https://pay.example.com", l.href)
        assertEquals(LinkStyle.TEXT, l.style)
    }

    @Test
    fun buttonAddsButtonStyleLink() {
        val out = ContentBuilder().apply { button("Pay Now", "https://pay.example.com") }.buildPublic()
        val l = assertIs<InvoiceElement.Link>(out[0])
        assertEquals(LinkStyle.BUTTON, l.style)
    }

    @Test
    fun linkRejectsUnsafeUrlScheme() {
        assertFailsWith<IllegalArgumentException> {
            ContentBuilder().apply { link("X", "javascript:alert(1)") }
        }
    }

    @Test
    fun buttonRejectsUnsafeUrlScheme() {
        assertFailsWith<IllegalArgumentException> {
            ContentBuilder().apply { button("X", "javascript:alert(1)") }
        }
    }

    @Test
    fun spacerDividerAndRowComposeAsExpected() {
        val out = ContentBuilder().apply {
            spacer(8)
            divider()
            row(1f, 2f) {
                text("L")
                text("R")
            }
        }.buildPublic()
        assertEquals(3, out.size)
        assertIs<InvoiceElement.Spacer>(out[0])
        assertEquals(InvoiceElement.Divider, out[1])
        val r = assertIs<InvoiceElement.Row>(out[2])
        assertEquals(2, r.children.size)
        assertEquals(listOf(1f, 2f), r.weights)
    }
}

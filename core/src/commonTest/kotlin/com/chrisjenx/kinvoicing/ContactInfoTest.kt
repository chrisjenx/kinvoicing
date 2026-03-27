package com.chrisjenx.kinvoicing

import kotlin.test.*

class ContactInfoTest {

    @Test
    fun constructWithAllFields() {
        val contact = ContactInfo(
            name = "Jane Smith",
            address = listOf("123 Main St", "Springfield, IL"),
            email = "jane@example.com",
            phone = "555-0100",
        )
        assertEquals("Jane Smith", contact.name)
        assertEquals(2, contact.address.size)
        assertEquals("jane@example.com", contact.email)
        assertEquals("555-0100", contact.phone)
    }

    @Test
    fun defaultsAreEmptyAndNull() {
        val contact = ContactInfo(name = "Test")
        assertEquals(emptyList(), contact.address)
        assertNull(contact.email)
        assertNull(contact.phone)
    }

    @Test
    fun equality() {
        val a = ContactInfo(name = "A", email = "a@b.com")
        val b = ContactInfo(name = "A", email = "a@b.com")
        assertEquals(a, b)
    }

    @Test
    fun billFromUsesContactInfo() {
        val contact = ContactInfo(name = "Seller", email = "sell@corp.com")
        val section = InvoiceSection.BillFrom(contact)
        assertEquals("Seller", section.contact.name)
        assertEquals("sell@corp.com", section.contact.email)
    }

    @Test
    fun billToUsesContactInfo() {
        val contact = ContactInfo(name = "Buyer", phone = "555-0200")
        val section = InvoiceSection.BillTo(contact)
        assertEquals("Buyer", section.contact.name)
        assertEquals("555-0200", section.contact.phone)
    }
}

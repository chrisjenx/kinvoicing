package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.InvoiceDsl

/** DSL builder for sender/recipient contact details ([InvoiceSection.BillFrom] or [InvoiceSection.BillTo]). */
@InvoiceDsl
public class PartyBuilder {
    private var name: String? = null
    private var address: List<String> = emptyList()
    private var email: String? = null
    private var phone: String? = null

    /** Set the party name (required). */
    public fun name(value: String) { name = value }
    /** Set the party address as one or more lines. */
    public fun address(vararg lines: String) { address = lines.toList() }
    /** Set the party contact email. */
    public fun email(value: String) { email = value }
    /** Set the party contact phone number. */
    public fun phone(value: String) { phone = value }

    internal fun build(): PartyData = PartyData(
        name = requireNotNull(name) { "Party requires a name" },
        address = address,
        email = email,
        phone = phone,
    )
}

internal data class PartyData(
    val name: String,
    val address: List<String>,
    val email: String?,
    val phone: String?,
)

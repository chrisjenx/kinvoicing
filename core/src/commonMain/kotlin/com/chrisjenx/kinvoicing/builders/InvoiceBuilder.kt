package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.*

/**
 * Top-level builder for constructing an [InvoiceDocument].
 *
 * Sections can be declared in any order; [build] sorts them into canonical document order:
 * Header → BillFrom → BillTo → MetaBlock → LineItems → Summary → PaymentInfo → Footer → Custom
 */
@InvoiceDsl
public class InvoiceBuilder {
    private var style: InvoiceStyle = InvoiceStyle()
    private var currencyCode: String = "USD"
    private var header: InvoiceSection.Header? = null
    private var billFrom: InvoiceSection.BillFrom? = null
    private var billTo: InvoiceSection.BillTo? = null
    private var lineItems: InvoiceSection.LineItems? = null
    private var summaryBuilder: SummaryBuilder? = null
    private var paymentInfo: InvoiceSection.PaymentInfo? = null
    private var footer: InvoiceSection.Footer? = null
    private val metaEntries: MutableList<MetaEntry> = mutableListOf()
    private val customSections: MutableList<InvoiceSection.Custom> = mutableListOf()

    /** Set the document currency code (e.g., "USD", "EUR"). Must be a 3-letter uppercase code. */
    public fun currency(value: String) {
        require(value.length == 3 && value.all { it.isUpperCase() }) {
            "Currency code must be a 3-letter uppercase code (e.g., 'USD'), got: '$value'"
        }
        currencyCode = value
    }

    /** Configure the visual style (colors, fonts, layout options). */
    public fun style(init: StyleBuilder.() -> Unit) {
        style = StyleBuilder().apply(init).build()
    }

    /** Configure the invoice header (branding, invoice number, dates). */
    public fun header(init: HeaderBuilder.() -> Unit) {
        header = HeaderBuilder().apply(init).build()
    }

    /** Configure the sender/issuer contact details. */
    public fun billFrom(init: PartyBuilder.() -> Unit) {
        val party = PartyBuilder().apply(init).build()
        billFrom = InvoiceSection.BillFrom(
            name = party.name,
            address = party.address,
            email = party.email,
            phone = party.phone,
        )
    }

    /** Configure the recipient/customer contact details. */
    public fun billTo(init: PartyBuilder.() -> Unit) {
        val party = PartyBuilder().apply(init).build()
        billTo = InvoiceSection.BillTo(
            name = party.name,
            address = party.address,
            email = party.email,
            phone = party.phone,
        )
    }

    /** Configure the line items table (columns and individual items). */
    public fun lineItems(init: LineItemsBuilder.() -> Unit) {
        lineItems = LineItemsBuilder().apply(init).build()
    }

    /** Add key-value metadata entries (e.g., PO number, project name). */
    public fun meta(init: MetaBuilder.() -> Unit) {
        metaEntries.addAll(MetaBuilder().apply(init).build())
    }

    /** Configure the financial summary (currency, discounts, taxes, fees). */
    public fun summary(init: SummaryBuilder.() -> Unit) {
        summaryBuilder = SummaryBuilder().apply(init)
    }

    /** Configure payment/remittance details (bank info, payment link). */
    public fun paymentInfo(init: PaymentInfoBuilder.() -> Unit) {
        paymentInfo = PaymentInfoBuilder().apply(init).build()
    }

    /** Configure the invoice footer (notes, terms). */
    public fun footer(init: FooterBuilder.() -> Unit) {
        footer = FooterBuilder().apply(init).build()
    }

    /** Add a custom section identified by [key], built from [InvoiceElement] primitives. */
    public fun custom(key: String, init: CustomBuilder.() -> Unit) {
        customSections.add(CustomBuilder(key).apply(init).build())
    }

    /** Build the [InvoiceDocument], sorting sections into canonical order. */
    public fun build(): InvoiceDocument {
        val sections = mutableListOf<InvoiceSection>()

        header?.let { sections.add(it) }
        billFrom?.let { sections.add(it) }
        billTo?.let { sections.add(it) }
        if (metaEntries.isNotEmpty()) {
            sections.add(InvoiceSection.MetaBlock(metaEntries.toList()))
        }
        val li = lineItems
        val summary = if (li != null) {
            sections.add(li)
            summaryBuilder?.build(li.rows, currencyCode)?.also { sections.add(it) }
        } else {
            summaryBuilder?.build(emptyList(), currencyCode)?.also { sections.add(it) }
        }
        paymentInfo?.let { sections.add(it) }
        footer?.let { sections.add(it) }
        customSections.forEach { sections.add(it) }

        val effectiveCurrency = summary?.currency ?: currencyCode
        return InvoiceDocument(sections = sections, style = style, currency = effectiveCurrency)
    }
}

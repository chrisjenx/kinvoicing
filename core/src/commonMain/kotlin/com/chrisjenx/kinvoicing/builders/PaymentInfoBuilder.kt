package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.InvoiceDsl
import com.chrisjenx.kinvoicing.InvoiceElement
import com.chrisjenx.kinvoicing.InvoiceSection
import com.chrisjenx.kinvoicing.LinkStyle
import com.chrisjenx.kinvoicing.util.requireSafeUrl

/** DSL builder for [InvoiceSection.PaymentInfo] with bank and payment link details. */
@InvoiceDsl
public class PaymentInfoBuilder {
    private var bankName: String? = null
    private var accountNumber: String? = null
    private var routingNumber: String? = null
    private var paymentLink: InvoiceElement.Link? = null
    private var qrCodeData: String? = null
    private var notes: List<InvoiceElement>? = null

    /** Set the bank or financial institution name. */
    public fun bankName(value: String) { bankName = value }

    /** Set the account number (consider masking for display, e.g., "****4242"). */
    public fun accountNumber(value: String) { accountNumber = value }

    /** Set the bank routing/sort code number. */
    public fun routingNumber(value: String) { routingNumber = value }

    /** Set a URL where the recipient can pay online. Renders as a "Pay Now" inline TEXT link. */
    public fun paymentLink(href: String): Unit = paymentLink("Pay Now", href, LinkStyle.TEXT)

    /** Set a payment link with custom display [text] and explicit [style]. */
    public fun paymentLink(
        text: String,
        href: String,
        style: LinkStyle = LinkStyle.TEXT,
    ) {
        paymentLink = InvoiceElement.Link(
            text = text,
            href = requireSafeUrl(href, "paymentLink"),
            style = style,
        )
    }

    /** Convenience: render the payment CTA as a styled BUTTON. */
    public fun paymentButton(text: String, href: String): Unit = paymentLink(text, href, LinkStyle.BUTTON)

    /** Set raw data to encode as a QR code (e.g., a payment URL). */
    public fun qrCodeData(value: String) { qrCodeData = value }

    /** Set additional payment-related notes from a single string of plain text. */
    public fun notes(value: String) {
        notes = listOf(InvoiceElement.Text(value))
    }

    /** Set rich notes from a content lambda — supports text(), link(), button(), spacer(), etc. */
    public fun notes(init: ContentBuilder.() -> Unit) {
        notes = ContentBuilder().apply(init).build()
    }

    internal fun build(): InvoiceSection.PaymentInfo = InvoiceSection.PaymentInfo(
        bankName = bankName,
        accountNumber = accountNumber,
        routingNumber = routingNumber,
        paymentLink = paymentLink,
        qrCodeData = qrCodeData,
        notes = notes,
    )
}

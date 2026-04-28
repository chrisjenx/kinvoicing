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
    /** Set a URL where the recipient can pay online. Defaults to "Pay Now" inline TEXT link. */
    public fun paymentLink(value: String) {
        paymentLink = InvoiceElement.Link("Pay Now", requireSafeUrl(value, "paymentLink"), LinkStyle.TEXT)
    }
    /** Set raw data to encode as a QR code (e.g., a payment URL). */
    public fun qrCodeData(value: String) { qrCodeData = value }
    /** Set additional payment-related notes. */
    public fun notes(value: String) { notes = listOf(InvoiceElement.Text(value)) }

    internal fun build(): InvoiceSection.PaymentInfo = InvoiceSection.PaymentInfo(
        bankName = bankName,
        accountNumber = accountNumber,
        routingNumber = routingNumber,
        paymentLink = paymentLink,
        qrCodeData = qrCodeData,
        notes = notes,
    )
}

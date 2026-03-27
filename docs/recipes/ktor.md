---
title: Ktor
parent: Recipes
nav_order: 1
---

# Ktor Integration

Serve invoices from a Ktor HTTP server as PDF downloads or email-ready HTML.

## Dependencies

Add Kinvoicing alongside your Ktor dependencies:

```kotlin
dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core:3.1.0")
    implementation("io.ktor:ktor-server-netty:3.1.0")

    // Kinvoicing
    implementation("com.chrisjenx.kinvoicing:core:<version>")
    implementation("com.chrisjenx.kinvoicing:render-pdf:<version>")
    implementation("com.chrisjenx.kinvoicing:render-html-email:<version>")
}
```

{: .note }
No Kinvoicing dependency is needed in the library itself — these are purely consumer-side patterns.

## PDF Endpoint

Generate and serve a PDF invoice as a download:

```kotlin
import com.chrisjenx.kinvoicing.pdf.toPdf
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.invoiceRoutes() {
    get("/invoice/{id}/pdf") {
        val doc = buildInvoice(call.parameters["id"]!!)
        val pdf = doc.toPdf()
        call.respondBytes(pdf, ContentType.Application.Pdf) {
            header(
                HttpHeaders.ContentDisposition,
                "attachment; filename=\"invoice-${call.parameters["id"]}.pdf\""
            )
        }
    }
}
```

## Email HTML Endpoint

Return email-safe HTML for embedding in email templates or previewing in a browser:

```kotlin
import com.chrisjenx.kinvoicing.html.email.toHtml
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.invoiceHtmlRoutes() {
    get("/invoice/{id}/html") {
        val doc = buildInvoice(call.parameters["id"]!!)
        val html = doc.toHtml()
        call.respondText(html, ContentType.Text.Html)
    }
}
```

## Building the Invoice

Construct the `InvoiceDocument` from your application data:

```kotlin
import com.chrisjenx.kinvoicing.invoice
import kotlinx.datetime.LocalDate

fun buildInvoice(id: String): InvoiceDocument {
    // Fetch from your database, API, etc.
    val order = orderRepository.findById(id)

    return invoice {
        header {
            branding {
                primary {
                    name("My Company")
                    email("billing@mycompany.com")
                }
            }
            invoiceNumber(order.invoiceNumber)
            issueDate(order.createdAt)
            dueDate(order.dueDate)
        }
        billTo {
            name(order.customerName)
            email(order.customerEmail)
            address(*order.addressLines.toTypedArray())
        }
        lineItems {
            columns("Description", "Qty", "Rate", "Amount")
            for (item in order.items) {
                item(item.description, qty = item.quantity, unitPrice = item.unitPrice)
            }
        }
        summary {
            currency(order.currency)
            if (order.taxRate > 0) {
                tax("Tax", percent = order.taxRate)
            }
        }
        footer {
            terms("Net ${order.paymentTermsDays}")
        }
    }
}
```

## Sending via Email

Combine with an email library like `javax.mail` or `kotlinx-mailer`:

```kotlin
import com.chrisjenx.kinvoicing.html.email.toHtml
import com.chrisjenx.kinvoicing.pdf.toPdf

suspend fun sendInvoiceEmail(doc: InvoiceDocument, recipientEmail: String) {
    val htmlBody = doc.toHtml()
    val pdfAttachment = doc.toPdf()

    // Use your email library of choice
    emailService.send {
        to(recipientEmail)
        subject("Invoice ${doc.sections.filterIsInstance<InvoiceSection.Header>()
            .firstOrNull()?.invoiceNumber ?: ""}")
        htmlContent(htmlBody)
        attachment("invoice.pdf", pdfAttachment, "application/pdf")
    }
}
```

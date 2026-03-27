package com.chrisjenx.invoicekit

/**
 * Platform-agnostic renderer that converts an [InvoiceDocument] into output type [T].
 *
 * Implementations include [com.chrisjenx.invoicekit.html.HtmlRenderer] (HTML string),
 * [com.chrisjenx.invoicekit.compose.ComposeRenderer] (composable lambda), and
 * [com.chrisjenx.invoicekit.pdf.PdfRenderer] (PDF bytes).
 */
public interface InvoiceRenderer<T> {
    /** Render the given [document] and return the result. */
    public fun render(document: InvoiceDocument): T
}

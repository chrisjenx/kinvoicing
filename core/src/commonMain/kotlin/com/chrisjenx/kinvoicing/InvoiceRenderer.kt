package com.chrisjenx.kinvoicing

/**
 * Platform-agnostic renderer that converts an [InvoiceDocument] into output type [T].
 *
 * Implementations include [com.chrisjenx.kinvoicing.html.HtmlRenderer] (HTML string),
 * [com.chrisjenx.kinvoicing.compose.ComposeRenderer] (composable lambda), and
 * [com.chrisjenx.kinvoicing.pdf.PdfRenderer] (PDF bytes).
 */
public interface InvoiceRenderer<T> {
    /** Render the given [document] and return the result. */
    public fun render(document: InvoiceDocument): T
}

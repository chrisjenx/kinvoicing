package com.chrisjenx.invoicekit.compose

import androidx.compose.runtime.Composable
import com.chrisjenx.invoicekit.InvoiceDocument
import com.chrisjenx.invoicekit.InvoiceRenderer

/**
 * Renderer that wraps [InvoiceContent] as an [InvoiceRenderer].
 * The output is a composable lambda suitable for display or PDF rendering.
 */
public class ComposeRenderer : InvoiceRenderer<@Composable () -> Unit> {
    override fun render(document: InvoiceDocument): @Composable () -> Unit = {
        InvoiceContent(document)
    }
}

/**
 * Convenience extension to get a composable lambda from an [InvoiceDocument].
 */
public fun InvoiceDocument.toComposable(): @Composable () -> Unit = {
    InvoiceContent(this)
}

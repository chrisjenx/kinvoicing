package com.chrisjenx.kinvoicing.pdf

import androidx.compose.runtime.CompositionLocalProvider
import com.chrisjenx.compose2pdf.PdfLink
import com.chrisjenx.compose2pdf.renderToPdf
import com.chrisjenx.kinvoicing.InvoiceDocument
import com.chrisjenx.kinvoicing.InvoiceRenderer
import com.chrisjenx.kinvoicing.StatusDisplay
import com.chrisjenx.kinvoicing.compose.InvoiceSections
import com.chrisjenx.kinvoicing.compose.InvoiceStyleProvider
import com.chrisjenx.kinvoicing.compose.LocalInvoiceStatus
import com.chrisjenx.kinvoicing.compose.LocalLinkWrapper
import com.chrisjenx.kinvoicing.compose.LocalStatusDisplay
import com.chrisjenx.kinvoicing.compose.sections.StatusBanner
import java.io.OutputStream

/**
 * Renders an [InvoiceDocument] to PDF bytes via compose2pdf.
 *
 * Uses [InvoiceSections] for intelligent section grouping (e.g.,
 * adjacent BillFrom + BillTo rendered side-by-side). Each logical group is
 * a direct child of the `renderToPdf` lambda, enabling auto-pagination.
 */
public class PdfRenderer(
    private val config: PdfRenderConfig = PdfRenderConfig.Default,
) : InvoiceRenderer<ByteArray> {

    override fun render(document: InvoiceDocument): ByteArray {
        return renderToPdf(
            config = config.pageConfig,
            density = config.density,
            mode = config.renderMode,
            pagination = config.pagination,
        ) {
            CompositionLocalProvider(
                LocalLinkWrapper provides { href, content ->
                    PdfLink(href = href) { content() }
                },
                LocalInvoiceStatus provides document.status,
                LocalStatusDisplay provides document.statusDisplay,
            ) {
                InvoiceStyleProvider(document.style) {
                    val status = document.status
                    if (status != null && document.statusDisplay is StatusDisplay.Banner) {
                        StatusBanner(status, document.style)
                    }
                    InvoiceSections(document.sections, document.currency)
                }
            }
        }
    }

    /** Render the [document] and write the PDF bytes directly to [outputStream]. */
    public fun render(document: InvoiceDocument, outputStream: OutputStream) {
        outputStream.write(render(document))
    }
}

/**
 * Convenience extension to render an [InvoiceDocument] to PDF bytes.
 */
public fun InvoiceDocument.toPdf(
    config: PdfRenderConfig = PdfRenderConfig.Default,
): ByteArray = PdfRenderer(config).render(this)

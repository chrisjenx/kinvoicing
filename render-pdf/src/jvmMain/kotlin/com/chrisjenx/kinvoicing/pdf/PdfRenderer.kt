package com.chrisjenx.kinvoicing.pdf

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chrisjenx.compose2pdf.renderToPdf
import com.chrisjenx.kinvoicing.InvoiceDocument
import com.chrisjenx.kinvoicing.InvoiceRenderer
import com.chrisjenx.kinvoicing.compose.InvoiceSectionContent
import com.chrisjenx.kinvoicing.compose.InvoiceStyleProvider
import com.chrisjenx.kinvoicing.currency
import java.io.OutputStream

/**
 * Renders an [InvoiceDocument] to PDF bytes via compose2pdf.
 *
 * Each invoice section is emitted as a direct child of the `renderToPdf`
 * content lambda, enabling compose2pdf's auto-pagination to keep sections
 * together and split between them at page boundaries.
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
            InvoiceStyleProvider(document.style) {
                for (section in document.sections) {
                    InvoiceSectionContent(section, document.currency)
                    Spacer(Modifier.height(8.dp))
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

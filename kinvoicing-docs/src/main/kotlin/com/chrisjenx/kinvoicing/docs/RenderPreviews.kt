package com.chrisjenx.kinvoicing.docs

import com.chrisjenx.kinvoicing.InvoiceDocument
import com.chrisjenx.kinvoicing.InvoiceFixtures
import com.chrisjenx.kinvoicing.html.email.toHtml
import com.chrisjenx.kinvoicing.pdf.toPdf
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import java.io.File
import javax.imageio.ImageIO

/**
 * One-shot preview generator. Renders selected fixtures to HTML, PDF, and a
 * rasterised PNG of the first PDF page, into an output directory.
 *
 * Args: [outputDir] (defaults to /tmp/kinvoicing-renders).
 */
fun main(args: Array<String>) {
    val outDir = File(args.firstOrNull() ?: "/tmp/kinvoicing-renders").apply { mkdirs() }

    val previews: List<Pair<String, InvoiceDocument>> = listOf(
        "payButton" to InvoiceFixtures.payButton,
        "linksAndImages" to InvoiceFixtures.linksAndImages,
    )

    for ((name, doc) in previews) {
        val html = File(outDir, "$name.html").apply { writeText(doc.toHtml()) }
        val pdf = File(outDir, "$name.pdf").apply { writeBytes(doc.toPdf()) }
        val png = File(outDir, "$name.page1.png")

        Loader.loadPDF(pdf).use { pdfDoc ->
            val image = PDFRenderer(pdfDoc).renderImageWithDPI(0, 144f, ImageType.RGB)
            ImageIO.write(image, "PNG", png)
        }

        println("Rendered $name → $html, $pdf, $png")
    }

    println("Done. Open the PNGs/PDFs in ${outDir.absolutePath}")
}

package com.chrisjenx.composepdf.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import com.chrisjenx.composepdf.PdfPageConfig
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Renders composable content to PDF bytes.
 *
 * v0.1: Uses ImageComposeScene for raster rendering + PDFBox for PDF creation.
 * Future: Will use Skia's native PDF backend for vector output.
 */
internal object PdfRenderer {

    fun renderSinglePage(
        config: PdfPageConfig,
        density: Density,
        content: @Composable () -> Unit,
    ): ByteArray {
        return renderMultiPage(
            pageCount = 1,
            config = config,
            density = density,
            content = { content() },
        )
    }

    fun renderMultiPage(
        pageCount: Int,
        config: PdfPageConfig,
        density: Density,
        content: @Composable (pageIndex: Int) -> Unit,
    ): ByteArray {
        require(pageCount > 0) { "pageCount must be positive, was $pageCount" }

        val contentWidthPx = (config.contentWidth.value * density.density).toInt()
        val contentHeightPx = (config.contentHeight.value * density.density).toInt()

        val doc = PDDocument()
        try {
            for (pageIndex in 0 until pageCount) {
                val bitmap = renderComposeToBitmap(
                    width = contentWidthPx,
                    height = contentHeightPx,
                    density = density,
                    content = { content(pageIndex) },
                )
                addBitmapPage(doc, config, bitmap)
            }
            val baos = ByteArrayOutputStream()
            doc.save(baos)
            return baos.toByteArray()
        } finally {
            doc.close()
        }
    }

    private fun renderComposeToBitmap(
        width: Int,
        height: Int,
        density: Density,
        content: @Composable () -> Unit,
    ): BufferedImage {
        val scene = ImageComposeScene(
            width = width,
            height = height,
            density = density,
            content = content,
        )
        try {
            val image = scene.render()
            return skiaImageToBufferedImage(image)
        } finally {
            scene.close()
        }
    }

    private fun skiaImageToBufferedImage(image: org.jetbrains.skia.Image): BufferedImage {
        val data = image.encodeToData() ?: error("Failed to encode Skia Image to PNG")
        return ImageIO.read(ByteArrayInputStream(data.bytes))
    }

    private fun addBitmapPage(
        doc: PDDocument,
        config: PdfPageConfig,
        bitmap: BufferedImage,
    ) {
        val mediaBox = PDRectangle(config.width.value, config.height.value)
        val page = PDPage(mediaBox)
        doc.addPage(page)

        val pdImage = LosslessFactory.createFromImage(doc, bitmap)
        val contentStream = PDPageContentStream(doc, page)
        try {
            // Draw the image at the content area (inside margins)
            contentStream.drawImage(
                pdImage,
                config.margins.left.value,
                config.margins.bottom.value,
                config.contentWidth.value,
                config.contentHeight.value,
            )
        } finally {
            contentStream.close()
        }
    }
}

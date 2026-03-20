@file:OptIn(InternalComposeUiApi::class)

package com.chrisjenx.composepdf.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.chrisjenx.composepdf.PdfPageConfig
import com.chrisjenx.composepdf.RenderMode
import kotlinx.coroutines.Dispatchers
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.jetbrains.skia.OutputWStream
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect
import org.jetbrains.skia.svg.SVGCanvas
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

internal object PdfRenderer {

    fun renderSinglePage(
        config: PdfPageConfig,
        density: Density,
        mode: RenderMode,
        content: @Composable () -> Unit,
    ): ByteArray {
        return renderMultiPage(
            pageCount = 1,
            config = config,
            density = density,
            mode = mode,
            content = { content() },
        )
    }

    fun renderMultiPage(
        pageCount: Int,
        config: PdfPageConfig,
        density: Density,
        mode: RenderMode,
        content: @Composable (pageIndex: Int) -> Unit,
    ): ByteArray {
        require(pageCount > 0) { "pageCount must be positive, was $pageCount" }
        return when (mode) {
            RenderMode.VECTOR -> renderVector(pageCount, config, density, content)
            RenderMode.RASTER -> renderRaster(pageCount, config, density, content)
        }
    }

    // --- Vector path: Compose → PictureRecorder → SVGCanvas → SVG → PDF ---

    private fun renderVector(
        pageCount: Int,
        config: PdfPageConfig,
        density: Density,
        content: @Composable (pageIndex: Int) -> Unit,
    ): ByteArray {
        val pxW = (config.contentWidth.value * density.density).toInt()
        val pxH = (config.contentHeight.value * density.density).toInt()

        val pdfDoc = PDDocument()
        try {
            for (pageIndex in 0 until pageCount) {
                val svg = renderComposeToSvg(pxW, pxH, density) { content(pageIndex) }
                SvgToPdfConverter.addPage(pdfDoc, svg, config.width.value, config.height.value)
            }
            val baos = ByteArrayOutputStream()
            pdfDoc.save(baos)
            return baos.toByteArray()
        } finally {
            pdfDoc.close()
        }
    }

    private fun renderComposeToSvg(
        widthPx: Int,
        heightPx: Int,
        density: Density,
        content: @Composable () -> Unit,
    ): String {
        // Step 1: Record Compose draw commands via PictureRecorder
        val recorder = PictureRecorder()
        val recordCanvas = recorder.beginRecording(
            Rect.makeWH(widthPx.toFloat(), heightPx.toFloat())
        )

        val scene = CanvasLayersComposeScene(
            density = density,
            size = IntSize(widthPx, heightPx),
            coroutineContext = Dispatchers.Unconfined,
            invalidate = {},
        )
        scene.setContent(content)
        scene.render(recordCanvas.asComposeCanvas(), nanoTime = 0)
        scene.close()

        val picture = recorder.finishRecordingAsPicture()

        // Step 2: Replay onto SVGCanvas to get vector SVG
        val baos = ByteArrayOutputStream()
        val wstream = OutputWStream(baos)
        val svgCanvas = SVGCanvas.make(
            Rect.makeWH(widthPx.toFloat(), heightPx.toFloat()),
            wstream,
            convertTextToPaths = false,
            prettyXML = false,
        )

        picture.playback(svgCanvas)
        svgCanvas.close()
        wstream.close()

        return baos.toString(Charsets.UTF_8)
    }

    // --- Raster path: Compose → ImageComposeScene → bitmap → PDF ---

    private fun renderRaster(
        pageCount: Int,
        config: PdfPageConfig,
        density: Density,
        content: @Composable (pageIndex: Int) -> Unit,
    ): ByteArray {
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

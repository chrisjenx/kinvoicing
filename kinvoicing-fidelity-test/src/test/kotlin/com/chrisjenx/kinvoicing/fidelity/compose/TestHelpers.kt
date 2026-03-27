@file:OptIn(InternalComposeUiApi::class)

package com.chrisjenx.kinvoicing.fidelity.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.chrisjenx.compose2pdf.PdfPageConfig
import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import kotlinx.coroutines.Dispatchers
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.PDFRenderer
import org.jetbrains.skia.OutputWStream
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.svg.SVGCanvas
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import org.jetbrains.skia.Rect as SkiaRect

internal fun renderComposeReference(
    widthPx: Int,
    heightPx: Int,
    density: Density,
    content: @Composable () -> Unit,
): BufferedImage {
    val scene = ImageComposeScene(
        width = widthPx,
        height = heightPx,
        density = density,
        content = content,
    )
    try {
        val image = scene.render()
        val data = image.encodeToData() ?: error("Failed to encode reference image")
        return ImageIO.read(ByteArrayInputStream(data.bytes))
    } finally {
        scene.close()
    }
}

internal fun compositeOnPage(
    content: BufferedImage,
    pageW: Int,
    pageH: Int,
    config: PdfPageConfig,
    density: Density,
): BufferedImage {
    val image = BufferedImage(pageW, pageH, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()
    g.color = java.awt.Color.WHITE
    g.fillRect(0, 0, pageW, pageH)
    val marginLeft = (config.margins.left.value * density.density).toInt()
    val marginTop = (config.margins.top.value * density.density).toInt()
    g.drawImage(content, marginLeft, marginTop, null)
    g.dispose()
    return image
}

internal fun rasterizePdf(pdfBytes: ByteArray, renderDpi: Float): BufferedImage {
    Loader.loadPDF(pdfBytes).use { doc ->
        val renderer = PDFRenderer(doc)
        renderer.setRenderingHints(RenderingHints(mapOf(
            RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
            RenderingHints.KEY_TEXT_ANTIALIASING to RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
            RenderingHints.KEY_FRACTIONALMETRICS to RenderingHints.VALUE_FRACTIONALMETRICS_ON,
            RenderingHints.KEY_STROKE_CONTROL to RenderingHints.VALUE_STROKE_PURE,
            RenderingHints.KEY_RENDERING to RenderingHints.VALUE_RENDER_QUALITY,
            RenderingHints.KEY_INTERPOLATION to RenderingHints.VALUE_INTERPOLATION_BICUBIC,
        )))
        return renderer.renderImageWithDPI(0, renderDpi)
    }
}

internal fun saveImage(image: BufferedImage, imagesDir: File, filename: String) {
    val file = File(imagesDir, filename)
    ImageIO.write(image, "PNG", file)
}

internal fun renderComposeToSvg(
    widthPx: Int,
    heightPx: Int,
    density: Density,
    content: @Composable () -> Unit,
): String {
    val recorder = PictureRecorder()
    val recordCanvas = recorder.beginRecording(
        SkiaRect.makeWH(widthPx.toFloat(), heightPx.toFloat())
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

    val baos = ByteArrayOutputStream()
    val wstream = OutputWStream(baos)
    val svgCanvas = SVGCanvas.make(
        SkiaRect.makeWH(widthPx.toFloat(), heightPx.toFloat()),
        wstream,
        convertTextToPaths = false,
        prettyXML = true,
    )
    picture.playback(svgCanvas)
    svgCanvas.close()
    wstream.close()
    return baos.toString(Charsets.UTF_8)
}

internal fun screenshotHtml(
    browser: Browser,
    htmlFile: File,
    config: PdfPageConfig,
    density: Density,
): BufferedImage {
    // HTML page uses CSS pt units (595pt × 842pt for A4). In CSS, 1pt = 96/72 px,
    // so 595pt = 793px. To get screenshot pixels matching the Compose reference
    // (pageWidthDp * density = 1190px), scale by density * 72/96 instead of bare density.
    val cssWidth = config.width.value.toInt()
    val cssHeight = config.height.value.toInt()
    val scaleFactor = density.density * 72.0 / 96.0
    val page = browser.newPage(
        Browser.NewPageOptions()
            .setViewportSize(cssWidth, cssHeight)
            .setDeviceScaleFactor(scaleFactor)
    )
    try {
        page.navigate("file://${htmlFile.absolutePath}")
        page.waitForLoadState()

        val pageElement = page.querySelector(".page")
        val screenshot = if (pageElement != null) {
            pageElement.screenshot()
        } else {
            page.screenshot(Page.ScreenshotOptions().setFullPage(true))
        }
        return ImageIO.read(ByteArrayInputStream(screenshot))
    } finally {
        page.close()
    }
}

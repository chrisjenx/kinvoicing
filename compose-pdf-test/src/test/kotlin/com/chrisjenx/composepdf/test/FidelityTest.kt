@file:OptIn(InternalComposeUiApi::class)

package com.chrisjenx.composepdf.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrisjenx.composepdf.PdfFontFamily
import com.chrisjenx.composepdf.PdfPageConfig
import com.chrisjenx.composepdf.RenderMode
import com.chrisjenx.composepdf.renderToPdf
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
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.jetbrains.skia.Rect as SkiaRect

class FidelityTest {

    private val config = PdfPageConfig.A4
    private val density = Density(2f)
    private val renderDpi = 144f // 2x scaling matches density=2

    private val reportDir = File("build/reports/fidelity")
    private val imagesDir = File(reportDir, "images")

    @Test
    fun `fidelity comparison of all fixtures`() {
        imagesDir.mkdirs()

        val results = fidelityFixtures.map { fixture ->
            runFixture(fixture)
        }

        // Generate HTML report
        val reportFile = File(reportDir, "index.html")
        generateFidelityReport(results, reportFile)
        println("Fidelity report: ${reportFile.absolutePath}")

        // Print summary table
        println()
        println(
            "${"Fixture".padEnd(25)} " +
                "${"Vector".padEnd(50)} " +
                "Raster",
        )
        println("-".repeat(120))
        for (result in results) {
            val vStatus = result.vectorStatus.label
            val rStatus = result.rasterStatus.label
            println(
                "${result.name.padEnd(25)} " +
                    "$vStatus RMSE=${"%.4f".format(result.vectorRmse)} SSIM=${"%.4f".format(result.vectorSsim)} Match=${"%.1f".format(result.vectorExactMatch * 100)}%".padEnd(50) + " " +
                    "$rStatus RMSE=${"%.4f".format(result.rasterRmse)} SSIM=${"%.4f".format(result.rasterSsim)} Match=${"%.1f".format(result.rasterExactMatch * 100)}%",
            )
        }
        println()

        // Collect failures
        val failures = mutableListOf<String>()
        for (result in results) {
            val fixture = fidelityFixtures.first { it.name == result.name }
            if (result.vectorRmse > fixture.vectorThreshold) {
                failures.add("${result.name}: vector RMSE ${"%.4f".format(result.vectorRmse)} > threshold ${fixture.vectorThreshold}")
            }
            if (result.rasterExactMatch < 0.999) {
                failures.add("${result.name}: raster match ${"%.4f".format(result.rasterExactMatch)} < 0.999")
            }
        }
        assertFalse(
            failures.isNotEmpty(),
            "Fidelity failures:\n${failures.joinToString("\n") { "  - $it" }}",
        )
    }

    private fun runFixture(fixture: Fixture): FidelityResult {
        val fixtureConfig = fixture.config
        // Use FULL PAGE dimensions so reference matches PDF rasterization
        val pageW = (fixtureConfig.width.value * density.density).toInt()
        val pageH = (fixtureConfig.height.value * density.density).toInt()
        val contentW = (fixtureConfig.contentWidth.value * density.density).toInt()
        val contentH = (fixtureConfig.contentHeight.value * density.density).toInt()

        // Wrap fixture content with bundled font to match renderToPdf default
        val wrappedContent: @Composable () -> Unit = {
            ProvideTextStyle(TextStyle(fontFamily = PdfFontFamily)) {
                fixture.content()
            }
        }

        // 1. Reference render: Compose content at content dimensions, then composite onto full page
        val contentImage = renderComposeReference(contentW, contentH, wrappedContent)
        val composeImage = compositeOnPage(contentImage, pageW, pageH, fixtureConfig)
        val flatCompose = ImageMetrics.flattenOnWhite(composeImage)
        saveImage(flatCompose, "${fixture.name}-compose.png")

        // 2. Save raw SVG for diagnostic inspection
        val svg = renderComposeToSvg(contentW, contentH, wrappedContent)
        File(imagesDir, "${fixture.name}-vector.svg").writeText(svg)

        // 3. Vector PDF render → rasterize + save PDF
        val vectorPdfBytes = renderToPdf(config = fixtureConfig, density = density, mode = RenderMode.VECTOR) {
            fixture.content()
        }
        File(imagesDir, "${fixture.name}-vector.pdf").writeBytes(vectorPdfBytes)
        val vectorImage = rasterizePdf(vectorPdfBytes)
        saveImage(vectorImage, "${fixture.name}-vector.png")

        // 4. Raster PDF render → rasterize + save PDF
        val rasterPdfBytes = renderToPdf(config = fixtureConfig, density = density, mode = RenderMode.RASTER) {
            fixture.content()
        }
        File(imagesDir, "${fixture.name}-raster.pdf").writeBytes(rasterPdfBytes)
        val rasterImage = rasterizePdf(rasterPdfBytes)
        saveImage(rasterImage, "${fixture.name}-raster.png")

        // 5. Compute all metrics
        val vectorRmse = ImageMetrics.computeRmse(composeImage, vectorImage)
        val vectorSsim = ImageMetrics.computeSsim(composeImage, vectorImage)
        val vectorExactMatch = ImageMetrics.computeExactMatchPercent(composeImage, vectorImage)
        val vectorMaxError = ImageMetrics.computeMaxPixelError(composeImage, vectorImage)

        val rasterRmse = ImageMetrics.computeRmse(composeImage, rasterImage)
        val rasterSsim = ImageMetrics.computeSsim(composeImage, rasterImage)
        val rasterExactMatch = ImageMetrics.computeExactMatchPercent(composeImage, rasterImage)
        val rasterMaxError = ImageMetrics.computeMaxPixelError(composeImage, rasterImage)

        // 6. Generate diff images
        val vectorDiff = ImageMetrics.generateDiffImage(composeImage, vectorImage)
        saveImage(vectorDiff, "${fixture.name}-vector-diff.png")

        val rasterDiff = ImageMetrics.generateDiffImage(composeImage, rasterImage)
        saveImage(rasterDiff, "${fixture.name}-raster-diff.png")

        return FidelityResult(
            name = fixture.name,
            category = fixture.category,
            description = fixture.description,
            vectorRmse = vectorRmse,
            vectorSsim = vectorSsim,
            vectorExactMatch = vectorExactMatch,
            vectorMaxError = vectorMaxError,
            vectorStatus = vectorStatus(vectorRmse, fixture.vectorThreshold),
            rasterRmse = rasterRmse,
            rasterSsim = rasterSsim,
            rasterExactMatch = rasterExactMatch,
            rasterMaxError = rasterMaxError,
            rasterStatus = rasterStatus(rasterExactMatch),
            composePath = "images/${fixture.name}-compose.png",
            vectorPath = "images/${fixture.name}-vector.png",
            rasterPath = "images/${fixture.name}-raster.png",
            vectorDiffPath = "images/${fixture.name}-vector-diff.png",
            rasterDiffPath = "images/${fixture.name}-raster-diff.png",
            vectorPdfPath = "images/${fixture.name}-vector.pdf",
            rasterPdfPath = "images/${fixture.name}-raster.pdf",
        )
    }

    @Test
    fun `PdfLink annotations are present in vector PDF`() {
        val pdfBytes = renderToPdf(config = config, density = density, mode = RenderMode.VECTOR) {
            PdfLinkFixture()
        }

        Loader.loadPDF(pdfBytes).use { doc ->
            val page = doc.getPage(0)
            val annotations = page.annotations.filterIsInstance<PDAnnotationLink>()
            val uris = annotations.mapNotNull { link ->
                val action = link.action
                if (action is org.apache.pdfbox.pdmodel.interactive.action.PDActionURI) {
                    action.uri
                } else null
            }

            val expectedUris = listOf(
                "https://example.com",
                "https://github.com",
                "https://a.com",
                "https://b.com",
                "https://c.com",
                "https://large-area.com",
            )
            for (expected in expectedUris) {
                assertTrue(
                    uris.contains(expected),
                    "Expected link annotation for $expected, found: $uris",
                )
            }

            // Verify all link rectangles have positive dimensions
            for (link in annotations) {
                val rect = link.rectangle
                assertTrue(rect.width > 0, "Link rectangle width must be positive: $rect")
                assertTrue(rect.height > 0, "Link rectangle height must be positive: $rect")
            }
        }
    }

    @Test
    fun `multipage PDF renders all pages`() {
        imagesDir.mkdirs()
        val pageCount = 3
        val pageContents: List<@Composable () -> Unit> = listOf(
            {
                Column(Modifier.fillMaxSize().padding(24.dp)) {
                    Text("Page 1: Cover", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.fillMaxWidth().height(80.dp).background(Color(0xFF1A237E), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("Multi-Page Document", color = Color.White, fontSize = 20.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("This tests that each page in a multi-page PDF renders correctly.", fontSize = 14.sp)
                }
            },
            {
                Column(Modifier.fillMaxSize().padding(24.dp)) {
                    Text("Page 2: Data", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    for (i in 1..8) {
                        Row(Modifier.fillMaxWidth().background(if (i % 2 == 0) Color(0xFFF5F5F5) else Color.White).padding(8.dp)) {
                            Text("Row $i", Modifier.weight(1f), fontSize = 12.sp)
                            Text("Value ${i * 42}", Modifier.weight(1f), fontSize = 12.sp)
                            Text("${i * 10}%", Modifier.weight(1f), fontSize = 12.sp)
                        }
                    }
                }
            },
            {
                Column(Modifier.fillMaxSize().padding(24.dp)) {
                    Text("Page 3: Summary", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().height(60.dp).background(Color(0xFF2E7D32), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Text("Status: Complete", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("All items processed successfully.", fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Total: $12,757.97", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
        )

        for (mode in RenderMode.entries) {
            val modeName = mode.name.lowercase()

            // Render multipage PDF
            val pdfBytes = renderToPdf(pages = pageCount, config = config, density = density, mode = mode) { pageIndex ->
                ProvideTextStyle(TextStyle(fontFamily = PdfFontFamily)) {
                    pageContents[pageIndex]()
                }
            }
            File(imagesDir, "multipage-$modeName.pdf").writeBytes(pdfBytes)

            Loader.loadPDF(pdfBytes).use { doc ->
                assertEquals(pageCount, doc.numberOfPages, "$mode: wrong page count")
                val renderer = PDFRenderer(doc)
                renderer.setRenderingHints(java.awt.RenderingHints(mapOf(
                    java.awt.RenderingHints.KEY_ANTIALIASING to java.awt.RenderingHints.VALUE_ANTIALIAS_ON,
                    java.awt.RenderingHints.KEY_TEXT_ANTIALIASING to java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                    java.awt.RenderingHints.KEY_FRACTIONALMETRICS to java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON,
                    java.awt.RenderingHints.KEY_STROKE_CONTROL to java.awt.RenderingHints.VALUE_STROKE_PURE,
                    java.awt.RenderingHints.KEY_RENDERING to java.awt.RenderingHints.VALUE_RENDER_QUALITY,
                    java.awt.RenderingHints.KEY_INTERPOLATION to java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC,
                )))

                for (i in 0 until pageCount) {
                    val pageW = (config.width.value * density.density).toInt()
                    val pageH = (config.height.value * density.density).toInt()
                    val contentW = (config.contentWidth.value * density.density).toInt()
                    val contentH = (config.contentHeight.value * density.density).toInt()

                    // Compose reference for this page: render at content size, composite onto full page
                    val contentImage = renderComposeReference(contentW, contentH) {
                        ProvideTextStyle(TextStyle(fontFamily = PdfFontFamily)) {
                            pageContents[i]()
                        }
                    }
                    val composeImage = compositeOnPage(contentImage, pageW, pageH, config)
                    saveImage(ImageMetrics.flattenOnWhite(composeImage), "multipage-$modeName-p${i}-compose.png")

                    // Rasterize this page from the PDF
                    val pdfImage = renderer.renderImageWithDPI(i, renderDpi)
                    saveImage(pdfImage, "multipage-$modeName-p${i}-pdf.png")

                    // Compare
                    val rmse = ImageMetrics.computeRmse(composeImage, pdfImage)
                    val diff = ImageMetrics.generateDiffImage(composeImage, pdfImage)
                    saveImage(diff, "multipage-$modeName-p${i}-diff.png")

                    val threshold = if (mode == RenderMode.VECTOR) 0.15 else 0.001
                    assertTrue(
                        rmse <= threshold,
                        "$mode page $i: RMSE ${"%.4f".format(rmse)} > threshold $threshold",
                    )
                }
            }
        }
    }

    private fun compositeOnPage(
        content: BufferedImage,
        pageW: Int,
        pageH: Int,
        config: PdfPageConfig,
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

    private fun renderComposeReference(
        widthPx: Int,
        heightPx: Int,
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

    private fun rasterizePdf(pdfBytes: ByteArray): BufferedImage {
        Loader.loadPDF(pdfBytes).use { doc ->
            val renderer = PDFRenderer(doc)
            // Configure rendering hints to better match Skia's rasterization
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

    private fun saveImage(image: BufferedImage, filename: String): String {
        val file = File(imagesDir, filename)
        ImageIO.write(image, "PNG", file)
        return file.name
    }

    private fun renderComposeToSvg(
        widthPx: Int,
        heightPx: Int,
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
}

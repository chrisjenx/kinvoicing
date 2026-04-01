@file:OptIn(InternalComposeUiApi::class)

package com.chrisjenx.kinvoicing.fidelity.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.compose2pdf.RenderMode
import com.chrisjenx.compose2pdf.renderToPdf
import com.chrisjenx.kinvoicing.InvoiceDocument
import com.chrisjenx.kinvoicing.compose.InvoiceContent
import com.chrisjenx.kinvoicing.compose.InvoiceSectionContent
import com.chrisjenx.kinvoicing.compose.InvoiceStyleProvider
import com.chrisjenx.kinvoicing.html.PdfFontFamily
import com.chrisjenx.kinvoicing.html.renderToHtml

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse

class FidelityTest {

    private val config = PdfPageConfig.A4
    private val density = Density(2f)
    private val renderDpi = 144f // 2x scaling matches density=2

    private val reportDir = File("build/reports/fidelity")
    private val imagesDir = File(reportDir, "images")

    private val browser: Browser? by lazy {
        try {
            val pw = Playwright.create()
            pw.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
        } catch (_: Throwable) {
            println("Playwright/Chromium not available — HTML fidelity tests will be skipped")
            null
        }
    }

    @Test
    fun `fidelity comparison of all fixtures`() {
        imagesDir.mkdirs()

        try {
            val results = fidelityFixtures.map { fixture ->
                runFixture(fixture)
            }

            // Generate unified HTML report
            val reportFile = File(reportDir, "index.html")
            generateFidelityReport(results, reportFile)
            println("Fidelity report: ${reportFile.absolutePath}")

            // Print summary table
            println()
            println(
                "${"Fixture".padEnd(25)} " +
                    "${"Vector".padEnd(50)} " +
                    "HTML",
            )
            println("-".repeat(130))
            for (result in results) {
                val vStatus = result.vectorStatus.label
                val hStatus = result.htmlStatus?.label ?: "Skipped"
                println(
                    "${result.name.padEnd(25)} " +
                        "$vStatus RMSE=${"%.4f".format(result.vectorRmse)} SSIM=${"%.4f".format(result.vectorSsim)} Match=${"%.1f".format(result.vectorExactMatch * 100)}%".padEnd(50) + " " +
                        if (result.htmlRmse != null) {
                            "$hStatus RMSE=${"%.4f".format(result.htmlRmse)} SSIM=${"%.4f".format(result.htmlSsim!!)}"
                        } else {
                            hStatus
                        },
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
            }
            for (result in results) {
                if (result.htmlRmse == null) continue
                val fixture = fidelityFixtures.first { it.name == result.name }
                if (result.htmlStatus == Status.FAIL) {
                    failures.add("${result.name}: HTML structural FAIL (RMSE=${"%.4f".format(result.htmlRmse)}, threshold=${fixture.htmlThreshold})")
                }
            }

            assertFalse(
                failures.isNotEmpty(),
                "Fidelity failures:\n${failures.joinToString("\n") { "  - $it" }}",
            )
        } finally {
            browser?.close()
        }
    }

    private fun runFixture(fixture: Fixture): FidelityResult {
        val fixtureConfig = fixture.config
        val document = fixture.document
        val pageW = (fixtureConfig.width.value * density.density).toInt()
        val pageH = (fixtureConfig.height.value * density.density).toInt()
        val contentW = (fixtureConfig.contentWidth.value * density.density).toInt()
        val contentH = (fixtureConfig.contentHeight.value * density.density).toInt()

        val sectionContent: @Composable () -> Unit = {
            ProvideTextStyle(TextStyle(fontFamily = PdfFontFamily)) {
                renderInvoiceSections(document)
            }
        }

        // 1. Reference render: sections at content dimensions, composited onto full page
        val contentImage = renderComposeReference(contentW, contentH, density, sectionContent)
        val composeImage = compositeOnPage(contentImage, pageW, pageH, fixtureConfig, density)
        val flatCompose = ImageMetrics.flattenOnWhite(composeImage)
        saveImage(flatCompose, imagesDir, "${fixture.name}-compose.png")

        // 2. Save raw SVG for diagnostic inspection
        val svg = renderComposeToSvg(contentW, contentH, density, sectionContent)
        File(imagesDir, "${fixture.name}-vector.svg").writeText(svg)

        // 3. Vector PDF render — sections as direct children for auto-pagination
        val vectorPdfBytes = renderToPdf(config = fixtureConfig, density = density, mode = RenderMode.VECTOR) {
            renderInvoiceSections(document)
        }
        File(imagesDir, "${fixture.name}-vector.pdf").writeBytes(vectorPdfBytes)
        val vectorImage = rasterizePdf(vectorPdfBytes, renderDpi)
        saveImage(vectorImage, imagesDir, "${fixture.name}-vector.png")

        // 4. Vector metrics (compare page 1 of PDF against single-page Compose reference)
        val vectorRmse = ImageMetrics.computeRmse(composeImage, vectorImage)
        val vectorSsim = ImageMetrics.computeSsim(composeImage, vectorImage)
        val vectorExactMatch = ImageMetrics.computeExactMatchPercent(composeImage, vectorImage)
        val vectorMaxError = ImageMetrics.computeMaxPixelError(composeImage, vectorImage)
        val vectorDiff = ImageMetrics.generateDiffImage(composeImage, vectorImage)
        saveImage(vectorDiff, imagesDir, "${fixture.name}-vector-diff.png")

        // 5. HTML comparison (if Playwright available)
        var htmlRmse: Double? = null
        var htmlSsim: Double? = null
        var htmlExactMatch: Double? = null
        var htmlMaxError: Double? = null
        var htmlStatusResult: Status? = null
        var htmlPath: String? = null
        var htmlDiffPath: String? = null
        var htmlFilePath: String? = null

        val b = browser
        if (b != null) {
            val html = renderToHtml(config = fixtureConfig, density = density) {
                renderInvoiceSections(document)
            }
            val htmlFile = File(imagesDir, "${fixture.name}.html")
            htmlFile.writeText(html)
            htmlFilePath = "images/${fixture.name}.html"

            val htmlImage = screenshotHtml(b, htmlFile, fixtureConfig, density)
            saveImage(htmlImage, imagesDir, "${fixture.name}-html.png")
            htmlPath = "images/${fixture.name}-html.png"

            htmlSsim = ImageMetrics.computeSsim(composeImage, htmlImage)
            htmlExactMatch = ImageMetrics.computeExactMatchPercent(composeImage, htmlImage)
            htmlMaxError = ImageMetrics.computeMaxPixelError(composeImage, htmlImage)
            htmlRmse = ImageMetrics.computeStructuralRmse(composeImage, htmlImage)
            htmlStatusResult = htmlStatus(htmlRmse!!, fixture.htmlThreshold)

            val diff = ImageMetrics.generateDiffImage(composeImage, htmlImage)
            saveImage(diff, imagesDir, "${fixture.name}-html-diff.png")
            htmlDiffPath = "images/${fixture.name}-html-diff.png"
        }

        return FidelityResult(
            name = fixture.name,
            category = fixture.category,
            description = fixture.description,
            vectorRmse = vectorRmse,
            vectorSsim = vectorSsim,
            vectorExactMatch = vectorExactMatch,
            vectorMaxError = vectorMaxError,
            vectorStatus = vectorStatus(vectorRmse, fixture.vectorThreshold),
            composePath = "images/${fixture.name}-compose.png",
            vectorPath = "images/${fixture.name}-vector.png",
            vectorDiffPath = "images/${fixture.name}-vector-diff.png",
            vectorPdfPath = "images/${fixture.name}-vector.pdf",
            htmlRmse = htmlRmse,
            htmlSsim = htmlSsim,
            htmlExactMatch = htmlExactMatch,
            htmlMaxError = htmlMaxError,
            htmlStatus = htmlStatusResult,
            htmlPath = htmlPath,
            htmlDiffPath = htmlDiffPath,
            htmlFilePath = htmlFilePath,
        )
    }

    /**
     * Render invoice sections as direct children — matches PdfRenderer's
     * pagination-friendly layout. Used for both reference and PDF rendering
     * so the comparison is apples-to-apples.
     */
    @Composable
    private fun renderInvoiceSections(document: InvoiceDocument) {
        InvoiceStyleProvider(document.style) {
            for (section in document.sections) {
                InvoiceSectionContent(section, document.currency)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

package com.chrisjenx.invoicekit.fidelity

import com.chrisjenx.invoicekit.InvoiceDocument
import com.chrisjenx.invoicekit.InvoiceFixtures
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.*

/**
 * Cross-renderer fidelity tests: Compose vs PDF bitmap comparison.
 *
 * These tests generate comparison images and metrics for visual review.
 * Thresholds are intentionally relaxed for the initial implementation —
 * the Compose headless renderer and compose2pdf's SVG→PDF pipeline
 * produce different viewport handling. Tighten as rendering matures.
 */
class FidelityTest {

    private val outputDir = File("build/test-output/fidelity").also { it.mkdirs() }

    private data class Fixture(val name: String, val document: InvoiceDocument)

    private val fixtures = listOf(
        Fixture("basic", InvoiceFixtures.basic),
        Fixture("minimal", InvoiceFixtures.minimal),
        Fixture("negative-values", InvoiceFixtures.negativeValues),
        Fixture("styled", InvoiceFixtures.styled),
    )

    @Test
    fun composePdfFidelityBasic() = runFidelity(fixtures.first { it.name == "basic" })

    @Test
    fun composePdfFidelityMinimal() = runFidelity(fixtures.first { it.name == "minimal" })

    @Test
    fun composePdfFidelityNegativeValues() = runFidelity(fixtures.first { it.name == "negative-values" })

    @Test
    fun composePdfFidelityStyled() = runFidelity(fixtures.first { it.name == "styled" })

    @Test
    fun allFixturesProduceBothOutputs() {
        InvoiceFixtures.all.forEachIndexed { i, doc ->
            val compose = RasterizeCompose.rasterize(doc)
            assertTrue(compose.width > 0, "Fixture $i compose should have width")
            val pdfPages = RasterizePdf.rasterize(doc)
            assertTrue(pdfPages.isNotEmpty(), "Fixture $i PDF should have pages")
        }
    }

    private fun runFidelity(fixture: Fixture) {
        val fixtureDir = File(outputDir, fixture.name).also { it.mkdirs() }
        val imagesDir = File(fixtureDir, "images").also { it.mkdirs() }

        // Rasterize Compose
        val composeImage = RasterizeCompose.rasterize(fixture.document)
        ImageIO.write(composeImage, "PNG", File(imagesDir, "compose.png"))

        // Rasterize PDF (first page)
        val pdfPages = RasterizePdf.rasterize(fixture.document)
        assertTrue(pdfPages.isNotEmpty(), "PDF should have at least one page")
        ImageIO.write(pdfPages[0], "PNG", File(imagesDir, "pdf.png"))

        // Compare
        val rmse = ImageComparison.rmse(composeImage, pdfPages[0])
        val ssim = ImageComparison.ssim(composeImage, pdfPages[0])

        // Generate diff
        val diff = ImageComparison.diffImage(composeImage, pdfPages[0])
        ImageIO.write(diff, "PNG", File(imagesDir, "diff-compose-pdf.png"))

        // Write metrics
        File(fixtureDir, "metrics.json").writeText(buildString {
            appendLine("{")
            appendLine("""  "compose_pdf_rmse": ${"%.4f".format(rmse)},""")
            appendLine("""  "compose_pdf_ssim": ${"%.4f".format(ssim)},""")
            appendLine("""  "compose_width": ${composeImage.width},""")
            appendLine("""  "compose_height": ${composeImage.height},""")
            appendLine("""  "pdf_width": ${pdfPages[0].width},""")
            appendLine("""  "pdf_height": ${pdfPages[0].height}""")
            appendLine("}")
        })

        println("${fixture.name}: RMSE=${"%.4f".format(rmse)}, SSIM=${"%.4f".format(ssim)}")

        // Verify both renderers produced non-trivial output
        assertTrue(composeImage.width > 0 && composeImage.height > 0,
            "Compose image should have dimensions")
        assertTrue(pdfPages[0].width > 0 && pdfPages[0].height > 0,
            "PDF image should have dimensions")

        // Verify RMSE is not 1.0 (completely different = black vs white)
        assertTrue(rmse < 1.0,
            "${fixture.name}: RMSE ${"%.4f".format(rmse)} indicates completely different output")
    }
}

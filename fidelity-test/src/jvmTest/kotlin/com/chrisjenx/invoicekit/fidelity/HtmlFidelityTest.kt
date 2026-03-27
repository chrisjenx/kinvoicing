package com.chrisjenx.invoicekit.fidelity

import com.chrisjenx.invoicekit.InvoiceFixtures
import com.microsoft.playwright.Browser
import org.junit.jupiter.api.Assumptions
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.*

/**
 * HTML fidelity tests — Compose vs HTML bitmap comparison.
 * Skips automatically if Playwright is not available.
 *
 * HTML will differ significantly from Compose (table layout vs flexbox),
 * so these tests are primarily informational — they generate comparison
 * images and log metrics for visual review.
 */
class HtmlFidelityTest {

    private val outputDir = File("build/test-output/fidelity").also { it.mkdirs() }
    private var browser: Browser? = null

    @BeforeTest
    fun setup() {
        browser = try {
            RasterizeHtml.createBrowser()
        } catch (e: Exception) {
            null
        }
    }

    @AfterTest
    fun teardown() {
        browser?.close()
    }

    @Test
    fun composeHtmlFidelityBasic() {
        Assumptions.assumeTrue(browser != null, "Playwright not available, skipping HTML fidelity test")
        val b = browser!!

        val fixtureDir = File(outputDir, "basic/images").also { it.mkdirs() }

        val composeImage = RasterizeCompose.rasterize(InvoiceFixtures.basic, width = 1200, height = 1684)
        val htmlImage = RasterizeHtml.rasterize(InvoiceFixtures.basic, b)

        ImageIO.write(htmlImage, "PNG", File(fixtureDir, "html.png"))

        val rmse = ImageComparison.rmse(composeImage, htmlImage)
        val ssim = ImageComparison.ssim(composeImage, htmlImage)

        val diff = ImageComparison.diffImage(composeImage, htmlImage)
        ImageIO.write(diff, "PNG", File(fixtureDir, "diff-compose-html.png"))

        println("Basic Compose↔HTML: RMSE=${"%.4f".format(rmse)}, SSIM=${"%.4f".format(ssim)}")

        // Informational — HTML uses table layout, Compose uses flexbox
        // Just verify output is non-trivial
        assertTrue(htmlImage.width > 0, "HTML image should have width")
        assertTrue(rmse < 1.0, "RMSE should not be 1.0 (completely different)")
    }
}

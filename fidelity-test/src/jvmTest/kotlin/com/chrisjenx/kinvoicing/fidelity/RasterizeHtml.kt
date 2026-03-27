package com.chrisjenx.kinvoicing.fidelity

import com.chrisjenx.kinvoicing.InvoiceDocument
import com.chrisjenx.kinvoicing.html.toHtml
import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Renders an InvoiceDocument to HTML, then screenshots with Playwright.
 * Returns null if Playwright is not available.
 */
internal object RasterizeHtml {

    fun createBrowser(): Browser? {
        return try {
            val playwright = Playwright.create()
            playwright.chromium().launch()
        } catch (e: Exception) {
            null
        }
    }

    fun rasterize(
        document: InvoiceDocument,
        browser: Browser,
        viewportWidth: Int = 600,
        deviceScaleFactor: Double = 2.0,
    ): BufferedImage {
        val html = document.toHtml()
        val tmpFile = File.createTempFile("invoice-", ".html")
        tmpFile.writeText(html)

        val context = browser.newContext(
            Browser.NewContextOptions()
                .setViewportSize(viewportWidth, 2000)
                .setDeviceScaleFactor(deviceScaleFactor)
        )

        val page = context.newPage()
        page.navigate("file://${tmpFile.absolutePath}")
        page.waitForLoadState()

        val screenshot = page.screenshot(Page.ScreenshotOptions().setFullPage(true))
        page.close()
        context.close()
        tmpFile.delete()

        return ImageIO.read(screenshot.inputStream())
    }
}

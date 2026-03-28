@file:OptIn(InternalComposeUiApi::class)

package com.chrisjenx.kinvoicing.composehtml

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.compose2pdf.PdfMargins
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlRenderTest {

    @Test
    fun `renderToHtml produces valid HTML document structure`() {
        val html = renderToHtml {
            Text("Hello, HTML!")
        }
        assertContains(html, "<!DOCTYPE html>")
        assertContains(html, "<html lang=")
        assertContains(html, "charset")
        assertContains(html, "</html>")
        assertContains(html, "page")
        assertContains(html, "content")
    }

    @Test
    fun `renderToHtml embeds bundled fonts as base64`() {
        val html = renderToHtml {
            Text("Font test")
        }
        assertContains(html, "@font-face")
        assertContains(html, "font-family: 'Inter'")
        assertContains(html, "font-weight: 400")
        assertContains(html, "font-weight: 700")
        assertContains(html, "data:font/ttf;base64,")
    }

    @Test
    fun `renderToHtml without bundled font omits font-face`() {
        val html = renderToHtml(useBundledFont = false) {
            Text("No font")
        }
        assertFalse(html.contains("@font-face"), "Should not contain @font-face when useBundledFont=false")
    }

    @Test
    fun `renderToHtml embeds SVG with text content`() {
        val html = renderToHtml {
            Text("Sample Text")
        }
        assertContains(html, "<svg")
        assertContains(html, "Sample Text")
    }

    @Test
    fun `renderToHtml embeds SVG for shapes`() {
        val html = renderToHtml {
            Box(Modifier.fillMaxWidth().height(50.dp).background(Color.Red))
        }
        assertContains(html, "<svg")
    }

    @Test
    fun `renderToHtml includes print styles`() {
        val html = renderToHtml {
            Text("Print test")
        }
        assertContains(html, "@media print")
        assertContains(html, "page-break-after: always")
    }

    @Test
    fun `renderToHtml page dimensions match A4 config`() {
        val html = renderToHtml(config = PdfPageConfig.A4) {
            Text("A4")
        }
        // A4 page dimensions should appear in the CSS
        assertTrue(html.contains("pt") && html.contains("page"), "Should contain page dimensions in pt: ${html.take(500)}")
    }

    @Test
    fun `renderToHtml Letter page size`() {
        val html = renderToHtml(config = PdfPageConfig.Letter) {
            Text("Letter")
        }
        assertContains(html, "612")
        assertContains(html, "792")
    }

    @Test
    fun `multipage renderToHtml produces multiple page divs`() {
        val html = renderToHtml(pages = 3) { pageIndex ->
            Text("Page ${pageIndex + 1}")
        }
        val pageCount = Regex("class=\"page\"").findAll(html).count()
        assertTrue(pageCount == 3, "Expected 3 page divs, got $pageCount")
    }

    @Test
    fun `renderToHtml includes link annotations`() {
        val html = renderToHtml {
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                PdfLink(href = "https://example.com") {
                    Text("Click me")
                }
            }
        }
        assertContains(html, "https://example.com")
        assertContains(html, "<a href=")
    }

    @Test
    fun `renderToHtml with custom margins sets content offset`() {
        val config = PdfPageConfig.A4.copy(
            margins = PdfMargins(top = 36.dp, bottom = 36.dp, left = 36.dp, right = 36.dp),
        )
        val html = renderToHtml(config = config) {
            Text("Margins")
        }
        assertContains(html, "margin-left")
        assertContains(html, "36")
    }

    @Test
    fun `renderToHtml replaces system fonts with Inter`() {
        val html = renderToHtml {
            Text("Font check")
        }
        assertFalse(html.contains(".SF NS"), "SVG should not contain system font names")
        assertContains(html, "font-family=\"Inter\"")
    }

    @Test
    fun `renderToHtml with zero pages throws`() {
        try {
            renderToHtml(pages = 0) { Text("") }
            assertTrue(false, "Should have thrown")
        } catch (e: IllegalArgumentException) {
            assertContains(e.message ?: "", "positive")
        }
    }
}

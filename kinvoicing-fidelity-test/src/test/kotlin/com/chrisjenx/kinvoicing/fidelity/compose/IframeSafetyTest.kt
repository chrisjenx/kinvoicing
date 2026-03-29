package com.chrisjenx.kinvoicing.fidelity.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.kinvoicing.compose.InvoiceSectionContent
import com.chrisjenx.kinvoicing.compose.InvoiceStyleProvider
import com.chrisjenx.kinvoicing.composehtml.renderToHtml
import com.chrisjenx.kinvoicing.currency
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Validates that kinvoicing-html (Compose→SVG→HTML) output is safe for iframe embedding
 * and free of XSS vectors.
 *
 * Every test runs against all [fidelityFixtures] to ensure no fixture introduces
 * unsafe patterns. Uses jsoup for DOM-level assertions.
 */
class IframeSafetyTest {

    companion object {
        private val EVENT_HANDLER_ATTRS = listOf(
            "onclick", "onload", "onerror", "onmouseover", "onmouseout",
            "onfocus", "onblur", "onsubmit", "onchange", "onkeydown",
            "onkeyup", "onkeypress", "ondblclick", "oncontextmenu",
        )
    }

    private val config = PdfPageConfig.A4
    private val density = Density(2f)

    private val parsedFixtures: List<Pair<String, Document>> by lazy {
        fidelityFixtures.map { fixture ->
            val html = renderToHtml(config = config, density = density) {
                InvoiceStyleProvider(fixture.document.style) {
                    for (section in fixture.document.sections) {
                        InvoiceSectionContent(section, fixture.document.currency)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            fixture.name to Jsoup.parse(html)
        }
    }

    // ── XSS prevention ──

    @Test
    fun noScriptTags() {
        forEachFixture { name, doc ->
            val scripts = doc.select("script")
            assertTrue(scripts.isEmpty(), "$name: found ${scripts.size} <script> element(s)")
        }
    }

    @Test
    fun noEventHandlerAttributes() {
        val selector = EVENT_HANDLER_ATTRS.joinToString(", ") { "[$it]" }
        forEachFixture { name, doc ->
            val found = doc.select(selector)
            assertTrue(found.isEmpty(), "$name: found element(s) with event handlers: " +
                found.joinToString { "<${it.tagName()}>" })
        }
    }

    @Test
    fun noJavascriptUrls() {
        forEachFixture { name, doc ->
            doc.select("[href], [src], [action]").forEach { el ->
                listOf("href", "src", "action").forEach { attr ->
                    val value = el.attr(attr).lowercase().trim()
                    if (value.isNotEmpty()) {
                        assertTrue(
                            !value.startsWith("javascript:"),
                            "$name: <${el.tagName()}> $attr contains javascript: URL"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun noBaseTag() {
        forEachFixture { name, doc ->
            val bases = doc.select("base")
            assertTrue(bases.isEmpty(), "$name: found <base> tag (redirect risk)")
        }
    }

    @Test
    fun noMetaRefresh() {
        forEachFixture { name, doc ->
            val refreshMeta = doc.select("meta[http-equiv=refresh]")
            assertTrue(refreshMeta.isEmpty(), "$name: found <meta http-equiv=\"refresh\"> (redirect risk)")
        }
    }

    // ── Containment ──

    @Test
    fun noFormElements() {
        val tags = listOf("form", "input", "select", "textarea", "button")
        forEachFixture { name, doc ->
            tags.forEach { tag ->
                val found = doc.select(tag)
                assertTrue(found.isEmpty(), "$name: found ${found.size} <$tag> element(s)")
            }
        }
    }

    @Test
    fun noIframeElements() {
        forEachFixture { name, doc ->
            val iframes = doc.select("iframe")
            assertTrue(iframes.isEmpty(), "$name: found nested <iframe> element(s)")
        }
    }

    @Test
    fun noObjectEmbedApplet() {
        val tags = listOf("object", "embed", "applet")
        forEachFixture { name, doc ->
            tags.forEach { tag ->
                val found = doc.select(tag)
                assertTrue(found.isEmpty(), "$name: found ${found.size} <$tag> element(s)")
            }
        }
    }

    // ── Self-contained ──

    @Test
    fun noExternalStylesheetLinks() {
        forEachFixture { name, doc ->
            val links = doc.select("link[rel=stylesheet]")
            assertTrue(links.isEmpty(), "$name: found external stylesheet <link>")
        }
    }

    @Test
    fun styleBlocksHaveNoImportOrExpression() {
        forEachFixture { name, doc ->
            doc.select("style").forEach { style ->
                val css = style.html().lowercase()
                assertTrue(
                    !css.contains("@import"),
                    "$name: <style> contains @import (external resource)"
                )
                assertTrue(
                    !css.contains("expression("),
                    "$name: <style> contains expression() (IE XSS vector)"
                )
            }
        }
    }

    @Test
    fun noExternalUrlsInStyleBlocks() {
        val externalUrlPattern = Regex("""url\s*\(\s*['"]?https?://""", RegexOption.IGNORE_CASE)
        forEachFixture { name, doc ->
            doc.select("style").forEach { style ->
                val css = style.html()
                val match = externalUrlPattern.find(css)
                if (match != null) {
                    fail("$name: <style> references external URL: ${match.value}")
                }
            }
        }
    }

    // ── SVG safety ──

    @Test
    fun svgElementsAreSafe() {
        forEachFixture { name, doc ->
            doc.select("svg").forEach { svg ->
                assertTrue(
                    svg.select("script").isEmpty(),
                    "$name: SVG contains <script>"
                )
                assertTrue(
                    svg.select("foreignObject").isEmpty(),
                    "$name: SVG contains <foreignObject>"
                )
                svg.allElements.forEach { el ->
                    el.attributes().forEach { attr ->
                        assertTrue(
                            !attr.key.startsWith("on"),
                            "$name: SVG <${el.tagName()}> has event handler: ${attr.key}"
                        )
                    }
                }
            }
        }
    }

    // ── Links ──

    @Test
    fun linksHaveTargetBlank() {
        forEachFixture { name, doc ->
            doc.select("a[href]").forEach { el ->
                val href = el.attr("href")
                // Skip empty/anchor-only links
                if (href.isNotBlank() && !href.startsWith("#")) {
                    val target = el.attr("target")
                    assertTrue(
                        target == "_blank",
                        "$name: <a href=\"$href\"> should have target=\"_blank\" but has target=\"$target\""
                    )
                }
            }
        }
    }

    // ── Helpers ──

    private fun forEachFixture(block: (name: String, doc: Document) -> Unit) {
        parsedFixtures.forEach { (name, doc) -> block(name, doc) }
    }
}

package com.chrisjenx.kinvoicing.fidelity

import com.chrisjenx.kinvoicing.InvoiceFixtures
import com.chrisjenx.kinvoicing.html.email.toHtml
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Validates that render-html output is safe for email clients and free of XSS vectors.
 *
 * Every test runs against all [InvoiceFixtures.all] to ensure no fixture introduces
 * unsafe elements. Uses jsoup for DOM-level assertions rather than string matching.
 */
class EmailSafetyTest {

    companion object {
        private val EVENT_HANDLER_ATTRS = listOf(
            "onclick", "onload", "onerror", "onmouseover", "onmouseout",
            "onfocus", "onblur", "onsubmit", "onchange", "onkeydown",
            "onkeyup", "onkeypress", "ondblclick", "oncontextmenu",
        )
    }

    private data class ParsedFixture(val name: String, val rawHtml: String, val doc: Document)

    private val fixtures: List<ParsedFixture> by lazy {
        InvoiceFixtures.all.mapIndexed { i, invoice ->
            val html = invoice.toHtml()
            ParsedFixture("fixture-$i", html, Jsoup.parse(html))
        }
    }

    // ── Forbidden elements ──

    @Test
    fun noScriptTags() = assertNoElements("script")

    @Test
    fun noStyleBlocks() = assertNoElements("style")

    @Test
    fun noLinkElements() = assertNoElements("link")

    @Test
    fun noIframeElements() = assertNoElements("iframe")

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
    fun noMediaElements() {
        val tags = listOf("video", "audio", "source", "embed", "object")
        forEachFixture { name, doc ->
            tags.forEach { tag ->
                val found = doc.select(tag)
                assertTrue(found.isEmpty(), "$name: found ${found.size} <$tag> element(s)")
            }
        }
    }

    @Test
    fun noSvgElements() = assertNoElements("svg")

    // ── XSS vectors ──

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

    // ── Inline-only styles ──

    @Test
    fun noClassAttributes() {
        forEachFixture { name, doc ->
            // Skip <html> and <body> which jsoup may add classes to
            val withClass = doc.select("[class]")
                .filter { it.tagName() !in listOf("html", "body") }
            assertTrue(
                withClass.isEmpty(),
                "$name: found ${withClass.size} element(s) with class attribute: " +
                    withClass.joinToString { "<${it.tagName()} class=\"${it.attr("class")}\">" }
            )
        }
    }

    @Test
    fun hasInlineStyles() {
        forEachFixture { name, doc ->
            val styled = doc.select("[style]")
            assertTrue(styled.isNotEmpty(), "$name: no elements have inline styles")
        }
    }

    // ── CSS compatibility ──

    @Test
    fun noPositionInStyles() = assertNoCssProperty("position\\s*:")

    @Test
    fun noFloatInStyles() = assertNoCssProperty("float\\s*:")

    @Test
    fun noFlexboxOrGridInStyles() = assertNoCssProperty("""display\s*:\s*(flex|grid)""")

    @Test
    fun noBackgroundImageInStyles() = assertNoCssProperty("background-image\\s*:")

    // ── Structure ──

    @Test
    fun validDoctype() {
        fixtures.forEach { (name, rawHtml, _) ->
            assertTrue(
                rawHtml.trimStart().startsWith("<!DOCTYPE html>", ignoreCase = true),
                "$name: HTML should start with DOCTYPE"
            )
        }
    }

    @Test
    fun validHtmlStructure() {
        forEachFixture { name, doc ->
            assertTrue(doc.select("html").isNotEmpty(), "$name: missing <html>")
            assertTrue(doc.select("head").isNotEmpty(), "$name: missing <head>")
            assertTrue(doc.select("body").isNotEmpty(), "$name: missing <body>")
        }
    }

    @Test
    fun rootLayoutIsTable() {
        forEachFixture { name, doc ->
            val body = doc.body()
            val tables = body.select("> table, > center > table, > div > table")
            assertTrue(
                tables.isNotEmpty(),
                "$name: root layout should be table-based"
            )
        }
    }

    @Test
    fun maxWidth600px() {
        val maxWidthPattern = Regex("""max-width\s*:\s*(\d+)px""", RegexOption.IGNORE_CASE)
        forEachFixture { name, doc ->
            val body = doc.body()
            val tables = body.select("> table, > center > table, > div > table")
            val hasConstrainedWidth = tables.any { table ->
                val style = table.attr("style")
                val match = maxWidthPattern.find(style)
                match != null && match.groupValues[1].toInt() <= 600
            }
            assertTrue(hasConstrainedWidth, "$name: root table should have max-width <= 600px")
        }
    }

    // ── Resources ──

    @Test
    fun noExternalResourceUrls() {
        forEachFixture { name, doc ->
            doc.select("[src]").forEach { el ->
                val src = el.attr("src")
                assertTrue(
                    !src.startsWith("http://") && !src.startsWith("https://"),
                    "$name: <${el.tagName()}> has external src: $src"
                )
            }
        }
    }

    @Test
    fun linksHaveValidHref() {
        forEachFixture { name, doc ->
            doc.select("a[href]").forEach { el ->
                val href = el.attr("href")
                assertTrue(href.isNotBlank(), "$name: <a> has blank href")
            }
        }
    }

    // ── Colors ──

    @Test
    fun colorsAreHexFormat() {
        val rgbPattern = Regex("""(?:rgb|hsl)a?\s*\(""", RegexOption.IGNORE_CASE)
        forEachFixture { name, doc ->
            doc.select("[style]").forEach { el ->
                val style = el.attr("style")
                val match = rgbPattern.find(style)
                if (match != null) {
                    fail("$name: <${el.tagName()}> uses non-hex color in style: ${match.value}")
                }
            }
        }
    }

    // ── Helpers ──

    private fun forEachFixture(block: (name: String, doc: Document) -> Unit) {
        fixtures.forEach { (name, _, doc) -> block(name, doc) }
    }

    private fun assertNoElements(tag: String) {
        forEachFixture { name, doc ->
            val found = doc.select(tag)
            assertTrue(found.isEmpty(), "$name: found ${found.size} <$tag> element(s)")
        }
    }

    private fun assertNoCssProperty(propertyPattern: String) {
        val regex = Regex(propertyPattern, RegexOption.IGNORE_CASE)
        forEachFixture { name, doc ->
            doc.select("[style]").forEach { el ->
                val style = el.attr("style")
                assertTrue(
                    !regex.containsMatchIn(style),
                    "$name: <${el.tagName()}> has forbidden CSS property in style: $style"
                )
            }
        }
    }
}

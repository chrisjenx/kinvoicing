package com.chrisjenx.kinvoicing.composehtml.internal

import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.kinvoicing.composehtml.PdfElementAnnotation
import com.chrisjenx.kinvoicing.composehtml.PdfHoverAnnotation
import com.chrisjenx.kinvoicing.composehtml.PdfLinkAnnotation

/**
 * Converts Compose-rendered SVG pages into a self-contained HTML document.
 *
 * Rather than converting individual SVG elements to HTML+CSS (lossy),
 * this embeds the SVG directly — Skia's SVGCanvas output is already a
 * pixel-perfect representation of the Compose rendering.
 */
internal object SvgToSemanticHtmlConverter {

    fun convert(
        svgPages: List<String>,
        config: PdfPageConfig,
        density: Float,
        linksByPage: List<List<PdfLinkAnnotation>>,
        elementsByPage: List<List<PdfElementAnnotation>>,
        fontBase64: Map<String, String>,
    ): String {
        val pageWidthPt = config.width.value
        val pageHeightPt = config.height.value
        val marginLeft = config.margins.left.value
        val marginTop = config.margins.top.value
        val contentWidthPt = config.contentWidth.value
        val contentHeightPt = config.contentHeight.value

        val hoverCss = buildHoverCss(elementsByPage)

        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang='en'><head><meta charset='utf-8'>")
            appendLine("<meta name='viewport' content='width=device-width, initial-scale=1'>")
            appendLine("<style>")

            // Embed bundled fonts
            for ((variant, base64) in fontBase64) {
                val parts = variant.split("-")
                val weight = parts.getOrElse(0) { "400" }
                val style = parts.getOrElse(1) { "normal" }
                appendLine("@font-face { font-family: 'Inter'; font-weight: $weight; font-style: $style; src: url(data:font/ttf;base64,$base64) format('truetype'); }")
            }

            appendLine("* { margin: 0; padding: 0; box-sizing: border-box; }")
            appendLine("body { background: #f5f5f5; font-family: 'Inter', sans-serif; }")
            appendLine(".page { position: relative; width: ${fmt(pageWidthPt)}pt; height: ${fmt(pageHeightPt)}pt; background: #fff; overflow: hidden; margin: 0 auto; }")
            appendLine(".page + .page { margin-top: 20px; }")
            appendLine(".content { position: relative; margin-left: ${fmt(marginLeft)}pt; margin-top: ${fmt(marginTop)}pt; width: ${fmt(contentWidthPt)}pt; height: ${fmt(contentHeightPt)}pt; overflow: hidden; }")
            appendLine(".content svg { display: block; width: ${fmt(contentWidthPt)}pt; height: ${fmt(contentHeightPt)}pt; }")
            appendLine(".link-overlay { position: absolute; z-index: 10; }")
            appendLine("@media print { body { background: none; } .page { margin: 0; page-break-after: always; } .page + .page { margin-top: 0; } }")
            if (hoverCss.isNotEmpty()) append(hoverCss)
            appendLine("</style></head><body>")

            for ((pageIndex, svg) in svgPages.withIndex()) {
                val links = linksByPage.getOrElse(pageIndex) { emptyList() }

                appendLine("<div class=\"page\">")
                appendLine("<div class=\"content\">")

                // Embed SVG with font fix: replace system font names with Inter
                // and inject @font-face so Chromium uses the same font as Skia
                val fixedSvg = fixSvgFonts(svg, fontBase64)
                val scaledSvg = injectSvgViewBox(fixedSvg, contentWidthPt * density, contentHeightPt * density)
                appendLine(scaledSvg)

                // Link overlays on top of SVG
                for (link in links) {
                    val left = fmt(link.x)
                    val top = fmt(link.y)
                    val w = fmt(link.width)
                    val h = fmt(link.height)
                    appendLine("<a href=\"${escapeAttr(link.href)}\" target=\"_blank\" class=\"link-overlay\" style=\"left:${left}pt;top:${top}pt;width:${w}pt;height:${h}pt\"></a>")
                }

                appendLine("</div>")
                appendLine("</div>")
            }

            appendLine("</body></html>")
        }
    }

    /**
     * Ensures the SVG has a viewBox attribute matching the pixel dimensions,
     * so CSS can scale it to pt dimensions without distortion.
     */
    private fun injectSvgViewBox(svg: String, widthPx: Float, heightPx: Float): String {
        val w = widthPx.toInt()
        val h = heightPx.toInt()
        if (svg.contains("viewBox")) return svg
        return svg.replaceFirst("<svg", "<svg viewBox=\"0 0 $w $h\"")
    }

    /**
     * Replaces system font names (e.g. ".SF NS") with "Inter" in the SVG,
     * and injects @font-face declarations inside a <defs><style> block
     * so Chromium renders text with the exact same font Skia used.
     */
    private fun fixSvgFonts(svg: String, fontBase64: Map<String, String>): String {
        // Replace ALL font-family attributes with Inter — Skia emits system font
        // names (e.g. ".SF NS, System Font, ...") that Chromium can't resolve
        var fixed = svg.replace(FONT_FAMILY_ATTR_RE, "font-family=\"Inter\"")

        // Inject @font-face inside SVG <defs><style> so the embedded Inter font is used
        if (fontBase64.isNotEmpty()) {
            val fontCss = buildString {
                for ((variant, base64) in fontBase64) {
                    val parts = variant.split("-")
                    val weight = parts.getOrElse(0) { "400" }
                    val style = parts.getOrElse(1) { "normal" }
                    appendLine("@font-face { font-family: 'Inter'; font-weight: $weight; font-style: $style; src: url(data:font/ttf;base64,$base64) format('truetype'); }")
                }
            }
            val defsStyle = "<defs><style>$fontCss</style></defs>"
            val insertPos = fixed.indexOf(">", fixed.indexOf("<svg")) + 1
            if (insertPos > 0) {
                fixed = fixed.substring(0, insertPos) + defsStyle + fixed.substring(insertPos)
            }
        }

        return fixed
    }

    private val FONT_FAMILY_ATTR_RE = Regex("""font-family="[^"]*"""")


    private fun buildHoverCss(elementsByPage: List<List<PdfElementAnnotation>>): String {
        val sb = StringBuilder()
        for (elements in elementsByPage) {
            for (element in elements) {
                if (element is PdfHoverAnnotation) {
                    val styles = element.hoverStyles
                    val cssProps = mutableListOf<String>()
                    styles.backgroundColor?.let { cssProps.add("background-color:$it") }
                    styles.opacity?.let { cssProps.add("opacity:$it") }
                    styles.transform?.let { cssProps.add("transform:$it") }
                    styles.boxShadow?.let { cssProps.add("box-shadow:$it") }
                    styles.cursor?.let { cssProps.add("cursor:$it") }
                    if (cssProps.isNotEmpty()) {
                        sb.appendLine("#${element.id}:hover { ${cssProps.joinToString(";")} }")
                    }
                }
            }
        }
        return sb.toString()
    }

    private fun fmt(v: Float): String {
        if (v == 0f) return "0"
        val s = "%.4f".format(v)
        return s.trimEnd('0').trimEnd('.')
    }

    private fun escapeAttr(s: String): String = s
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}

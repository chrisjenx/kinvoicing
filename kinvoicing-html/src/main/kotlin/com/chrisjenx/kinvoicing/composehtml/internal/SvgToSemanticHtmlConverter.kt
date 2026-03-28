package com.chrisjenx.kinvoicing.composehtml.internal

import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.kinvoicing.composehtml.PdfElementAnnotation
import com.chrisjenx.kinvoicing.composehtml.PdfHoverAnnotation
import com.chrisjenx.kinvoicing.composehtml.PdfLinkAnnotation

/**
 * Converts Compose-rendered SVG pages into a self-contained HTML document.
 *
 * Embeds the SVG directly — Skia's SVGCanvas output is already a
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
        val fontFaceCss = buildFontFaceCss(fontBase64)
        val hoverCss = buildHoverCss(elementsByPage)

        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang='en'><head><meta charset='utf-8'>")
            appendLine("<meta name='viewport' content='width=device-width, initial-scale=1'>")
            appendLine("<style>")
            append(fontFaceCss)
            appendLine("* { margin: 0; padding: 0; box-sizing: border-box; }")
            appendLine("body { background: #f5f5f5; font-family: 'Inter', sans-serif; }")
            appendLine(".page { position: relative; width: ${fmtPt(pageWidthPt)}; height: ${fmtPt(pageHeightPt)}; background: #fff; overflow: hidden; margin: 0 auto; }")
            appendLine(".page + .page { margin-top: 20px; }")
            appendLine(".content { position: relative; margin-left: ${fmtPt(marginLeft)}; margin-top: ${fmtPt(marginTop)}; width: ${fmtPt(contentWidthPt)}; height: ${fmtPt(contentHeightPt)}; overflow: hidden; }")
            appendLine(".content svg { display: block; width: ${fmtPt(contentWidthPt)}; height: ${fmtPt(contentHeightPt)}; }")
            appendLine(".link-overlay { position: absolute; z-index: 10; }")
            appendLine("@media print { body { background: none; } .page { margin: 0; page-break-after: always; } .page + .page { margin-top: 0; } }")
            if (hoverCss.isNotEmpty()) append(hoverCss)
            appendLine("</style></head><body>")

            for ((pageIndex, svg) in svgPages.withIndex()) {
                val links = linksByPage.getOrElse(pageIndex) { emptyList() }

                appendLine("<div class=\"page\">")
                appendLine("<div class=\"content\">")

                // Replace system font names with Inter so Chromium uses the same font as Skia
                val fixedSvg = fixSvgFonts(svg)
                val scaledSvg = injectSvgViewBox(fixedSvg, contentWidthPt * density, contentHeightPt * density)
                appendLine(scaledSvg)

                for (link in links) {
                    appendLine("<a href=\"${escapeAttr(link.href)}\" target=\"_blank\" class=\"link-overlay\" style=\"left:${fmtPt(link.x)};top:${fmtPt(link.y)};width:${fmtPt(link.width)};height:${fmtPt(link.height)}\"></a>")
                }

                appendLine("</div>")
                appendLine("</div>")
            }

            appendLine("</body></html>")
        }
    }

    private fun injectSvgViewBox(svg: String, widthPx: Float, heightPx: Float): String {
        if (svg.contains("viewBox")) return svg
        return svg.replaceFirst("<svg", "<svg viewBox=\"0 0 ${widthPx.toInt()} ${heightPx.toInt()}\"")
    }

    /** Replaces system font names (e.g. ".SF NS, System Font, ...") with "Inter" */
    private fun fixSvgFonts(svg: String): String =
        svg.replace(FONT_FAMILY_ATTR_RE, "font-family=\"Inter\"")

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

    private fun escapeAttr(s: String): String = s
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}

internal fun buildFontFaceCss(fontBase64: Map<String, String>): String = buildString {
    for ((variant, base64) in fontBase64) {
        val parts = variant.split("-")
        val weight = parts.getOrElse(0) { "400" }
        val style = parts.getOrElse(1) { "normal" }
        appendLine("@font-face { font-family: 'Inter'; font-weight: $weight; font-style: $style; src: url(data:font/ttf;base64,$base64) format('truetype'); }")
    }
}

/** Format a float as a pt CSS value, trimming unnecessary trailing zeros. */
internal fun fmtPt(v: Float): String {
    if (v == 0f) return "0"
    val s = "%.4f".format(v)
    return s.trimEnd('0').trimEnd('.') + "pt"
}

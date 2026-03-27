package com.chrisjenx.kinvoicing.composehtml.internal

import com.chrisjenx.compose2pdf.PdfPageConfig

/**
 * Renders an [HtmlNode] tree to a complete HTML document string
 * with embedded CSS, fonts, and print styles.
 */
internal object CssEmitter {

    fun render(
        pages: List<HtmlNode>,
        config: PdfPageConfig,
        fontBase64: Map<String, String>,
        hoverCss: String = "",
    ): String = buildString {
        val pageWidthPt = config.width.value
        val pageHeightPt = config.height.value
        val marginLeft = config.margins.left.value
        val marginTop = config.margins.top.value
        val contentWidthPt = config.contentWidth.value
        val contentHeightPt = config.contentHeight.value

        appendLine("<!DOCTYPE html>")
        appendLine("<html lang=\"en\">")
        appendLine("<head>")
        appendLine("<meta charset=\"utf-8\">")
        appendLine("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
        appendLine("<style>")

        // Font faces
        for ((variant, base64) in fontBase64) {
            val parts = variant.split("-")
            val weight = parts.getOrElse(0) { "400" }
            val style = parts.getOrElse(1) { "normal" }
            appendLine("@font-face { font-family: 'Inter'; font-weight: $weight; font-style: $style; src: url(data:font/ttf;base64,$base64) format('truetype'); }")
        }

        // Normalize + base styles
        appendLine("* { margin: 0; padding: 0; box-sizing: border-box; }")
        appendLine("body { background: #f5f5f5; font-family: 'Inter', sans-serif; }")
        appendLine("h1, h2, h3, h4, h5, h6 { margin: 0; }")
        appendLine("hr { border: none; }")
        appendLine("img { display: block; }")
        appendLine(".page { position: relative; width: ${fmt(pageWidthPt)}pt; height: ${fmt(pageHeightPt)}pt; background: #fff; overflow: hidden; margin: 0 auto; }")
        appendLine(".page + .page { margin-top: 20px; }")
        appendLine(".content { position: relative; margin-left: ${fmt(marginLeft)}pt; margin-top: ${fmt(marginTop)}pt; width: ${fmt(contentWidthPt)}pt; min-height: ${fmt(contentHeightPt)}pt; }")
        appendLine("@media print { body { background: none; } .page { margin: 0; page-break-after: always; } .page + .page { margin-top: 0; } }")
        if (hoverCss.isNotEmpty()) append(hoverCss)
        appendLine("</style>")
        appendLine("</head>")
        appendLine("<body>")

        for (page in pages) {
            appendLine("<div class=\"page\">")
            appendLine("<div class=\"content\">")
            renderNode(this, page, indent = 2)
            appendLine("</div>")
            appendLine("</div>")
        }

        appendLine("</body>")
        appendLine("</html>")
    }

    private fun renderNode(sb: StringBuilder, node: HtmlNode, indent: Int) {
        val prefix = "  ".repeat(indent)

        // Raw SVG fallback
        if (node.rawSvg != null) {
            sb.appendLine("$prefix${node.rawSvg}")
            return
        }

        val styleAttr = if (node.css.isNotEmpty()) {
            " style=\"${node.css.entries.joinToString(";") { (k, v) -> "$k:$v" }}\""
        } else ""

        val htmlAttrs = node.attributes.entries.joinToString("") { (k, v) ->
            " $k=\"${escapeAttr(v)}\""
        }

        if (node.selfClosing) {
            sb.appendLine("$prefix<${node.tag}$htmlAttrs$styleAttr>")
            return
        }

        // Leaf text node
        if (node.textContent != null && node.children.isEmpty()) {
            sb.appendLine("$prefix<${node.tag}$htmlAttrs$styleAttr>${escapeHtml(node.textContent)}</${node.tag}>")
            return
        }

        // Container node
        sb.appendLine("$prefix<${node.tag}$htmlAttrs$styleAttr>")
        for (child in node.children) {
            renderNode(sb, child, indent + 1)
        }
        sb.appendLine("$prefix</${node.tag}>")
    }

    private fun fmt(v: Float): String {
        if (v == 0f) return "0"
        val s = "%.4f".format(v)
        return s.trimEnd('0').trimEnd('.')
    }

    private fun escapeHtml(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    private fun escapeAttr(s: String): String = s
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}

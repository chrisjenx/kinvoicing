package com.chrisjenx.kinvoicing.composehtml.internal

import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.kinvoicing.composehtml.PdfElementAnnotation
import com.chrisjenx.kinvoicing.composehtml.PdfLinkAnnotation

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

        // Parse each SVG page and convert to HTML nodes
        val pageNodes = svgPages.mapIndexed { pageIndex, svg ->
            val svgNodes = SvgParser.parse(svg, density)
            val htmlNode = SvgToHtmlConverter.convert(svgNodes, density)
            val links = linksByPage.getOrElse(pageIndex) { emptyList() }
            addLinkOverlays(htmlNode, links)
        }

        val hoverCss = buildHoverCss(elementsByPage)

        return CssEmitter.render(pageNodes, config, fontBase64, hoverCss)
    }

    private fun addLinkOverlays(baseNode: HtmlNode, links: List<PdfLinkAnnotation>): HtmlNode {
        if (links.isEmpty()) return baseNode
        val linkNodes = links.map { link ->
            HtmlNode(
                tag = "a",
                attributes = mutableMapOf("href" to link.href, "target" to "_blank"),
                css = mutableMapOf(
                    "position" to "absolute",
                    "left" to "${fmt(link.x)}pt",
                    "top" to "${fmt(link.y)}pt",
                    "width" to "${fmt(link.width)}pt",
                    "height" to "${fmt(link.height)}pt",
                    "z-index" to "10",
                ),
            )
        }
        return baseNode.copy(children = (baseNode.children + linkNodes).toMutableList())
    }

    private fun buildHoverCss(elementsByPage: List<List<PdfElementAnnotation>>): String {
        val sb = StringBuilder()
        for (elements in elementsByPage) {
            for (element in elements) {
                if (element is com.chrisjenx.kinvoicing.composehtml.PdfHoverAnnotation) {
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
}

internal object SvgToHtmlConverter {
    fun convert(svgNodes: List<SvgNode>, density: Float): HtmlNode {
        val children = svgNodes.map { convertNode(it, density) }.toMutableList()
        return HtmlNode(tag = "div", children = children)
    }

    private fun convertNode(node: SvgNode, density: Float): HtmlNode {
        val css = mutableMapOf<String, String>()
        val attrs = mutableMapOf<String, String>()

        // Convert SVG positioning to CSS
        val transform = node.attributes["transform"] ?: ""
        val translateMatch = Regex("translate\\(([\\d.e+-]+)[,\\s]+([\\d.e+-]+)\\)").find(transform)
        if (translateMatch != null) {
            val tx = translateMatch.groupValues[1].toFloatOrNull() ?: 0f
            val ty = translateMatch.groupValues[2].toFloatOrNull() ?: 0f
            css["position"] = "absolute"
            css["left"] = "${fmt(tx / density)}pt"
            css["top"] = "${fmt(ty / density)}pt"
        }

        val tag: String
        var textContent: String? = null

        when (node.type) {
            SvgNodeType.TEXT -> {
                tag = "span"
                textContent = node.textContent ?: ""
                css["position"] = "absolute"
                css["white-space"] = "pre"
                node.attributes["x"]?.toFloatOrNull()?.let { css["left"] = "${fmt(it / density)}pt" }
                node.attributes["y"]?.toFloatOrNull()?.let { css["top"] = "${fmt(it / density)}pt" }
                node.attributes["font-size"]?.replace("px", "")?.toFloatOrNull()?.let {
                    css["font-size"] = "${fmt(it / density)}pt"
                }
                parseFillColor(node)?.let { css["color"] = it }
            }
            SvgNodeType.RECT -> {
                tag = "div"
                val x = (node.attributes["x"]?.toFloatOrNull() ?: 0f) / density
                val y = (node.attributes["y"]?.toFloatOrNull() ?: 0f) / density
                val w = (node.attributes["width"]?.toFloatOrNull() ?: 0f) / density
                val h = (node.attributes["height"]?.toFloatOrNull() ?: 0f) / density
                css["position"] = "absolute"
                css["left"] = "${fmt(x)}pt"
                css["top"] = "${fmt(y)}pt"
                css["width"] = "${fmt(w)}pt"
                css["height"] = "${fmt(h)}pt"
                parseFillColor(node)?.let { css["background"] = it }
                node.attributes["rx"]?.toFloatOrNull()?.let { css["border-radius"] = "${fmt(it / density)}pt" }
            }
            SvgNodeType.IMAGE -> {
                tag = "img"
                val href = node.attributes["href"] ?: node.attributes["xlink:href"] ?: ""
                attrs["src"] = href
                val x = (node.attributes["x"]?.toFloatOrNull() ?: 0f) / density
                val y = (node.attributes["y"]?.toFloatOrNull() ?: 0f) / density
                val w = (node.attributes["width"]?.toFloatOrNull() ?: 0f) / density
                val h = (node.attributes["height"]?.toFloatOrNull() ?: 0f) / density
                css["position"] = "absolute"
                css["left"] = "${fmt(x)}pt"
                css["top"] = "${fmt(y)}pt"
                css["width"] = "${fmt(w)}pt"
                css["height"] = "${fmt(h)}pt"
            }
            else -> {
                tag = "div"
                if (node.type == SvgNodeType.PATH || node.type == SvgNodeType.CIRCLE || node.type == SvgNodeType.ELLIPSE || node.type == SvgNodeType.LINE) {
                    // Complex SVG shapes fall back to inline SVG
                    return HtmlNode(tag = "div", rawSvg = nodeToSvgString(node))
                }
            }
        }

        val children = node.children.map { convertNode(it, density) }.toMutableList()

        return HtmlNode(
            tag = tag,
            css = css,
            attributes = attrs,
            textContent = textContent,
            children = children,
            selfClosing = tag == "img",
        )
    }

    private fun parseFillColor(node: SvgNode): String? {
        val fill = node.attributes["fill"]
        if (fill == "none" || fill.isNullOrEmpty()) return null
        return fill
    }

    private fun nodeToSvgString(node: SvgNode): String {
        val attrs = node.attributes.entries.joinToString(" ") { (k, v) -> "$k=\"$v\"" }
        return "<svg style=\"position:absolute;overflow:visible\"><${node.type.name.lowercase()} $attrs/></svg>"
    }

    private fun fmt(v: Float): String {
        if (v == 0f) return "0"
        val s = "%.4f".format(v)
        return s.trimEnd('0').trimEnd('.')
    }
}

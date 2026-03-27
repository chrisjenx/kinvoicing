package com.chrisjenx.kinvoicing.composehtml.internal

/**
 * Converts flat positioned SVG elements into an HTML document tree.
 *
 * Phase 1 (current): Produces semantic HTML elements with absolute positioning.
 * Each SVG element maps to the best HTML element (h1, span, hr, img, div)
 * while preserving exact pixel positions for fidelity.
 *
 * Phase 2 (future): Groups of absolutely-positioned elements that form obvious
 * Row/Column patterns can be promoted to CSS flexbox containers.
 */
internal object LayoutAnalyzer {

    /** Maximum height in pt for a rect to be treated as a divider/hr. */
    private const val DIVIDER_MAX_HEIGHT = 4f

    /**
     * Builds an HTML document tree from flat SVG nodes.
     * All elements are absolutely positioned for pixel-perfect fidelity.
     *
     * @param nodes Flat list of SVG elements with absolute positions (in pt).
     * @param pageWidth Page content width in pt.
     * @return Root HtmlNode containing all elements.
     */
    fun buildTree(nodes: List<SvgNode>, pageWidth: Float): HtmlNode {
        val root = HtmlNode(
            tag = "div",
            css = mutableMapOf("position" to "relative", "width" to "100%", "height" to "100%"),
        )

        for (node in nodes) {
            root.children.add(svgNodeToHtml(node, pageWidth))
        }

        return root
    }

    private fun svgNodeToHtml(node: SvgNode, pageWidth: Float): HtmlNode {
        return when (node.type) {
            SvgNodeType.TEXT -> buildText(node)
            SvgNodeType.RECT -> buildRect(node, pageWidth)
            SvgNodeType.IMAGE -> buildImage(node)
            SvgNodeType.PATH -> buildPath(node)
            SvgNodeType.CIRCLE, SvgNodeType.ELLIPSE -> buildShape(node)
            SvgNodeType.LINE -> buildLine(node)
            SvgNodeType.POLY -> buildInlineSvg(node)
            SvgNodeType.GROUP -> buildClipContainer(node, pageWidth)
            else -> HtmlNode(tag = "div")
        }
    }

    private fun buildClipContainer(node: SvgNode, pageWidth: Float): HtmlNode {
        val css = mutableMapOf<String, String>()
        css["position"] = "absolute"
        css["left"] = "${fmt(node.x)}pt"
        css["top"] = "${fmt(node.y)}pt"
        css["width"] = "${fmt(node.width)}pt"
        css["height"] = "${fmt(node.height)}pt"
        css.putAll(node.cssProperties)

        val container = HtmlNode(tag = "div", css = css)

        // Render children with positions relative to the clip container
        for (child in node.children) {
            val childHtml = svgNodeToHtml(child, node.width)
            // Adjust child positions to be relative to the clip container
            childHtml.css["left"]?.let { left ->
                val childX = left.removeSuffix("pt").toFloatOrNull() ?: return@let
                childHtml.css["left"] = "${fmt(childX - node.x)}pt"
            }
            childHtml.css["top"]?.let { top ->
                val childY = top.removeSuffix("pt").toFloatOrNull() ?: return@let
                childHtml.css["top"] = "${fmt(childY - node.y)}pt"
            }
            container.children.add(childHtml)
        }

        return container
    }

    private fun buildText(node: SvgNode): HtmlNode {
        val tag = detectTextTag(node)
        val css = mutableMapOf<String, String>()

        // Absolute positioning for fidelity
        css["position"] = "absolute"
        css["left"] = "${fmt(node.x)}pt"
        css["top"] = "${fmt(node.y)}pt"
        css["white-space"] = "pre"
        css["line-height"] = "1"

        // Text styles from SVG
        css.putAll(node.cssProperties)

        return HtmlNode(
            tag = tag,
            css = css,
            textContent = node.textContent,
        )
    }

    private fun buildRect(node: SvgNode, pageWidth: Float): HtmlNode {
        val isDivider = node.height <= DIVIDER_MAX_HEIGHT && node.width >= pageWidth * 0.5f
        val css = mutableMapOf<String, String>()

        css["position"] = "absolute"
        css["left"] = "${fmt(node.x)}pt"
        css["top"] = "${fmt(node.y)}pt"
        css["width"] = "${fmt(node.width)}pt"
        css["height"] = "${fmt(node.height)}pt"

        if (isDivider) {
            // Semantic <hr> element
            val bg = node.cssProperties["background-color"]
            return HtmlNode(
                tag = "hr",
                css = mutableMapOf<String, String>().apply {
                    put("position", "absolute")
                    put("left", "${fmt(node.x)}pt")
                    put("top", "${fmt(node.y)}pt")
                    put("width", "${fmt(node.width)}pt")
                    put("border", "none")
                    put("margin", "0")
                    put("padding", "0")
                    if (bg != null) {
                        put("border-top", "${fmt(node.height)}pt solid $bg")
                    } else {
                        put("height", "${fmt(node.height)}pt")
                        put("background-color", node.cssProperties["background-color"] ?: "#000")
                    }
                },
                selfClosing = true,
            )
        }

        // Regular rect → div with background
        css.putAll(node.cssProperties)
        return HtmlNode(tag = "div", css = css)
    }

    private fun buildImage(node: SvgNode): HtmlNode {
        return HtmlNode(
            tag = "img",
            css = mutableMapOf(
                "position" to "absolute",
                "left" to "${fmt(node.x)}pt",
                "top" to "${fmt(node.y)}pt",
                "width" to "${fmt(node.width)}pt",
                "height" to "${fmt(node.height)}pt",
            ),
            attributes = mutableMapOf(
                "src" to (node.attributes["src"] ?: ""),
                "alt" to "",
            ),
            selfClosing = true,
        )
    }

    private fun buildPath(node: SvgNode): HtmlNode {
        val hasBackground = node.cssProperties["background-color"] != null
        val hasBorder = node.cssProperties["border"] != null
        val hasBorderRadius = node.cssProperties["border-radius"] != null
        val hasEvenOddFill = node.attributes["fillRule"] == "evenodd"

        // Force inline SVG for evenodd fill-rule (CSS can't express it) and complex paths.
        // Only render as div if it's a simple rounded rect without special fill rules.
        if ((hasBackground || hasBorder) && hasBorderRadius && !hasEvenOddFill) {
            val css = mutableMapOf<String, String>()
            css["position"] = "absolute"
            css["left"] = "${fmt(node.x)}pt"
            css["top"] = "${fmt(node.y)}pt"
            css["width"] = "${fmt(node.width)}pt"
            css["height"] = "${fmt(node.height)}pt"
            css.putAll(node.cssProperties)
            return HtmlNode(tag = "div", css = css)
        }

        // All other paths → inline SVG for accurate rendering
        return buildInlineSvg(node)
    }

    private fun buildShape(node: SvgNode): HtmlNode {
        val css = mutableMapOf<String, String>()
        css["position"] = "absolute"
        css["left"] = "${fmt(node.x)}pt"
        css["top"] = "${fmt(node.y)}pt"
        css["width"] = "${fmt(node.width)}pt"
        css["height"] = "${fmt(node.height)}pt"
        css.putAll(node.cssProperties)
        return HtmlNode(tag = "div", css = css)
    }

    private fun buildLine(node: SvgNode): HtmlNode {
        // Thin horizontal line → <hr>
        if (node.height <= DIVIDER_MAX_HEIGHT && node.width > node.height * 3) {
            val strokeColor = node.cssProperties["border"]?.let {
                Regex("""solid\s+(.+)""").find(it)?.groupValues?.get(1)
            } ?: "#000"
            return HtmlNode(
                tag = "hr",
                css = mutableMapOf(
                    "position" to "absolute",
                    "left" to "${fmt(node.x)}pt",
                    "top" to "${fmt(node.y)}pt",
                    "width" to "${fmt(node.width)}pt",
                    "border" to "none",
                    "border-top" to "${fmt(maxOf(node.height, 1f))}pt solid $strokeColor",
                    "margin" to "0",
                ),
                selfClosing = true,
            )
        }
        return buildInlineSvg(node)
    }

    private fun buildInlineSvg(node: SvgNode): HtmlNode {
        val w = fmt(node.width)
        val h = fmt(node.height)

        // Use raw SVG element string if available (preserves original SVG px coordinates)
        val rawSvgElem = node.attributes["rawSvg"]
        val bboxX = node.attributes["bboxX"] ?: "0"
        val bboxY = node.attributes["bboxY"] ?: "0"
        val bboxW = node.attributes["bboxW"] ?: w
        val bboxH = node.attributes["bboxH"] ?: h

        val svgContent = if (rawSvgElem != null) {
            // ViewBox uses original SVG px coordinates to match the path data
            "<svg style=\"position:absolute;left:${fmt(node.x)}pt;top:${fmt(node.y)}pt;width:${w}pt;height:${h}pt;overflow:visible\" viewBox=\"$bboxX $bboxY $bboxW $bboxH\" xmlns=\"http://www.w3.org/2000/svg\">$rawSvgElem</svg>"
        } else {
            "<svg style=\"position:absolute;left:${fmt(node.x)}pt;top:${fmt(node.y)}pt;width:${w}pt;height:${h}pt\" xmlns=\"http://www.w3.org/2000/svg\"></svg>"
        }

        return HtmlNode(
            tag = "div",
            rawSvg = svgContent,
        )
    }

    /**
     * Detects appropriate HTML tag for text based on font properties.
     */
    private fun detectTextTag(node: SvgNode): String {
        val fontSize = node.cssProperties["font-size"]?.removeSuffix("pt")?.toFloatOrNull() ?: 0f
        val fontWeight = node.cssProperties["font-weight"]?.toIntOrNull() ?: 400
        val isBold = fontWeight >= 600

        return when {
            isBold && fontSize >= 18 -> "h1"
            isBold && fontSize >= 15 -> "h2"
            isBold && fontSize >= 12 -> "h3"
            isBold -> "strong"
            else -> "span"
        }
    }

    private fun fmt(v: Float): String {
        if (v == 0f) return "0"
        val s = "%.4f".format(v)
        return s.trimEnd('0').trimEnd('.')
    }
}

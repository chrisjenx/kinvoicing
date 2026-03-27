package com.chrisjenx.kinvoicing.composehtml.internal

import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Parses Skia SVGCanvas output into a flat list of [SvgNode] elements
 * with absolute positions resolved from transform chains.
 *
 * Skia's SVGCanvas produces flat SVG — layout containers (Column, Row, Box)
 * are NOT preserved as `<g>` groups. All elements are siblings with absolute
 * `translate()` transforms. This parser extracts each element with its resolved
 * position and visual properties.
 */
internal object SvgParser {

    /**
     * Parses an SVG string into a list of positioned [SvgNode] elements.
     *
     * @param svg SVG string from Skia SVGCanvas.
     * @param density Render density (SVG px / density = CSS pt).
     * @return Flat list of elements with absolute positions in pt.
     */
    fun parse(svg: String, density: Float): List<SvgNode> {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val xmlDoc = factory.newDocumentBuilder().parse(svg.byteInputStream())
        val svgRoot = xmlDoc.documentElement

        // Collect defs for <use> resolution
        val defs = mutableMapOf<String, Element>()
        collectDefs(svgRoot, defs)

        val nodes = mutableListOf<SvgNode>()
        parseChildren(svgRoot, defs, density, 0f, 0f, nodes)
        return nodes
    }

    private fun parseChildren(
        parent: Element,
        defs: Map<String, Element>,
        density: Float,
        parentTx: Float,
        parentTy: Float,
        out: MutableList<SvgNode>,
        parentScale: Float = 1f,
    ) {
        for (i in 0 until parent.childNodes.length) {
            val node = parent.childNodes.item(i)
            if (node.nodeType != Node.ELEMENT_NODE) continue
            parseElement(node as Element, defs, density, parentTx, parentTy, out, parentScale)
        }
    }

    private fun parseElement(
        elem: Element,
        defs: Map<String, Element>,
        density: Float,
        parentTx: Float,
        parentTy: Float,
        out: MutableList<SvgNode>,
        parentScale: Float = 1f,
    ) {
        // Resolve transform (translate + scale + matrix)
        val transform = resolveTransform(elem)
        val absTx = parentTx + transform.tx
        val absTy = parentTy + transform.ty
        val scale = parentScale * transform.sx

        when (elem.localName) {
            "defs", "clipPath" -> {} // Skip definitions
            "g" -> {
                val clipRef = elem.getAttribute("clip-path")
                if (clipRef.isNotEmpty()) {
                    // Group with clip-path — resolve clip shape and create container
                    val clipId = Regex("""url\(#([^)]+)\)""").find(clipRef)?.groupValues?.get(1)
                    val clipElem = clipId?.let { defs[it] }
                    val clipRect = clipElem?.let { resolveClipRect(it) }

                    if (clipRect != null) {
                        // Parse children into a temporary list
                        val childNodes = mutableListOf<SvgNode>()
                        parseChildren(elem, defs, density, absTx, absTy, childNodes, scale)

                        // Create a clip container SvgNode
                        val cx = clipRect[0] / density
                        val cy = clipRect[1] / density
                        val cw = clipRect[2] / density
                        val ch = clipRect[3] / density
                        val crx = clipRect[4] / density
                        val cry = clipRect[5] / density

                        val css = mutableMapOf<String, String>()
                        css["overflow"] = "hidden"
                        if (crx > 0 || cry > 0) {
                            css["border-radius"] = if (crx == cw / 2f && cry == ch / 2f) "50%"
                            else "${fmt(crx)}pt"
                        }

                        out.add(SvgNode(
                            type = SvgNodeType.GROUP,
                            x = cx, y = cy, width = cw, height = ch,
                            cssProperties = css,
                            children = childNodes,
                        ))
                    } else {
                        // Can't resolve clip — render children normally
                        parseChildren(elem, defs, density, absTx, absTy, out, scale)
                    }
                } else {
                    // Regular group — recurse, accumulating transform
                    parseChildren(elem, defs, density, absTx, absTy, out, scale)
                }
            }
            "use" -> {
                val href = elem.getAttribute("href").ifEmpty {
                    elem.getAttributeNS("http://www.w3.org/1999/xlink", "href")
                } ?: return
                val def = defs[href.removePrefix("#")] ?: return
                // Pass accumulated scale from the <use> transform to the referenced element
                parseElement(def, defs, density, absTx, absTy, out, scale)
            }
            "text" -> out.add(parseText(elem, density, absTx, absTy, defs))
            "rect" -> out.add(parseRect(elem, density, absTx, absTy))
            "path" -> out.add(parsePath(elem, density, absTx, absTy))
            "image" -> out.add(parseImage(elem, density, absTx, absTy, scale))
            "line" -> out.add(parseLine(elem, density, absTx, absTy))
            "circle" -> out.add(parseCircle(elem, density, absTx, absTy))
            "ellipse" -> out.add(parseEllipse(elem, density, absTx, absTy))
            "polyline", "polygon" -> out.add(parsePoly(elem, density, absTx, absTy))
        }
    }

    // --- Element parsers ---

    private fun parseText(elem: Element, density: Float, tx: Float, ty: Float, defs: Map<String, Element> = emptyMap()): SvgNode {
        val text = elem.textContent.trim()
        val fontSize = attrF(elem, "font-size").let { if (it == 0f) 12f else it }
        // SVG y attribute is the text baseline position (relative to group origin)
        val yBaseline = (attr(elem, "y") ?: "").split(",")
            .firstOrNull()?.trim()?.toFloatOrNull() ?: (fontSize)
        // Convert baseline to top using ascent ratio.
        // Empirically tuned: binary search between 0.84-0.87 for best RMSE.
        val ascentRatio = 0.855f
        val textTop = ty + yBaseline - fontSize * ascentRatio

        val css = mutableMapOf<String, String>()
        val fill = attr(elem, "fill")
        if (fill != null && fill != "none" && fill != "#000" && fill != "#000000" && fill != "black") {
            css["color"] = parseCssColor(fill) ?: fill
        }
        css["font-size"] = "${fmt(fontSize / density)}pt"
        attr(elem, "font-weight")?.let { w ->
            if (w != "400" && w != "normal") css["font-weight"] = w
        }
        attr(elem, "font-style")?.let { s ->
            if (s != "normal") css["font-style"] = s
        }
        // Font family: Skia emits system font fallback list like ".SF NS, System Font, ..."
        // When bundled Inter font is used, map to 'Inter'. Otherwise, use the first non-system entry.
        attr(elem, "font-family")?.let { ff ->
            val families = ff.split(",").map { it.trim().trim('\'', '"') }
            val firstNonSystem = families.firstOrNull { !it.startsWith(".SF") && !it.contains("System") }
            css["font-family"] = if (firstNonSystem != null) "'$firstNonSystem'" else "'Inter',sans-serif"
        }

        // Estimate text width from x-position array or content length
        val xPositions = attr(elem, "x")?.split(",")?.mapNotNull { it.trim().toFloatOrNull() } ?: emptyList()
        val textWidth = if (xPositions.size > 1) {
            (xPositions.last() - xPositions.first()) + fontSize * 0.6f // approximate last char width
        } else {
            text.length * fontSize * 0.5f // rough estimate
        }

        // Store raw SVG text element for inline SVG rendering (preserves exact character positions)
        val rawSvgText = buildRawSvgText(elem)

        return SvgNode(
            type = SvgNodeType.TEXT,
            x = (tx + (xPositions.firstOrNull() ?: 0f)) / density,
            y = textTop / density,
            width = textWidth / density,
            height = fontSize / density,
            textContent = text,
            cssProperties = css,
            attributes = mapOf(
                "rawSvg" to rawSvgText,
                "svgY" to fmt(yBaseline),
                "svgFontSize" to fmt(fontSize),
            ),
        )
    }

    /**
     * Builds a raw SVG `<text>` element string preserving all attributes
     * (font, fill, x positions, y baseline) for inline SVG rendering.
     */
    private fun buildRawSvgText(elem: Element): String = buildString {
        append("<text")
        val attrs = elem.attributes
        for (j in 0 until attrs.length) {
            val a = attrs.item(j)
            if (a.localName == "transform") continue // Position handled by container
            // Simplify font-family to just the first meaningful entry
            if (a.localName == "font-family") {
                val families = a.nodeValue.split(",").map { it.trim().trim('\'', '"') }
                val first = families.firstOrNull { !it.startsWith(".SF") && !it.contains("System") } ?: "Inter"
                append(" font-family=\"$first\"")
            } else {
                append(" ${a.localName}=\"${a.nodeValue}\"")
            }
        }
        append(">${elem.textContent.trim().replace("&", "&amp;").replace("<", "&lt;")}</text>")
    }

    private fun parseRect(elem: Element, density: Float, tx: Float, ty: Float): SvgNode {
        var x = attrF(elem, "x") + tx
        var y = attrF(elem, "y") + ty
        var w = attrF(elem, "width")
        var h = attrF(elem, "height")
        val rx = attrF(elem, "rx")
        val ry = attrF(elem, "ry").let { if (it == 0f) rx else it }

        val fill = attr(elem, "fill")
        val stroke = attr(elem, "stroke")
        val strokeWidth = attrF(elem, "stroke-width")

        // Adjust for SVG stroke centering: SVG stroke is centered on the edge,
        // but the rect may have been inset (x/y offset, reduced w/h) by half stroke-width.
        // Expand back to the full visual bounds for CSS border rendering.
        val isStrokeOnly = (fill == null || fill == "none") && stroke != null && stroke != "none"
        if (isStrokeOnly && strokeWidth > 0) {
            val halfStroke = strokeWidth / 2f
            x -= halfStroke
            y -= halfStroke
            w += strokeWidth
            h += strokeWidth
        }

        val css = mutableMapOf<String, String>()
        extractFillStroke(elem, css)

        // Convert border width from SVG px to pt
        if (stroke != null && stroke != "none" && strokeWidth > 0) {
            val color = parseCssColor(stroke) ?: stroke
            css["border"] = "${fmt(strokeWidth / density)}pt solid $color"
            css["box-sizing"] = "border-box"
        }
        if (rx > 0 || ry > 0) {
            css["border-radius"] = "${fmt(rx / density)}pt"
        }

        return SvgNode(
            type = SvgNodeType.RECT,
            x = x / density,
            y = y / density,
            width = w / density,
            height = h / density,
            cssProperties = css,
        )
    }

    private fun parsePath(elem: Element, density: Float, tx: Float, ty: Float): SvgNode {
        val d = attr(elem, "d") ?: ""
        val bbox = estimatePathBBox(d)
        val css = mutableMapOf<String, String>()
        extractFillStroke(elem, css)

        val fill = attr(elem, "fill")
        val stroke = attr(elem, "stroke")
        val strokeWidth = attrF(elem, "stroke-width")
        val bboxPx = bbox ?: floatArrayOf(0f, 0f, 0f, 0f)

        var px = tx + bboxPx[0]
        var py = ty + bboxPx[1]
        var pw = bboxPx[2]
        var ph = bboxPx[3]

        // Adjust stroke centering for stroke-only paths (same as rects)
        val isStrokeOnly = (fill == null || fill == "none") && stroke != null && stroke != "none"
        if (isStrokeOnly && strokeWidth > 0) {
            val halfStroke = strokeWidth / 2f
            px -= halfStroke
            py -= halfStroke
            pw += strokeWidth
            ph += strokeWidth
        }

        // Convert border width to pt
        if (stroke != null && stroke != "none" && strokeWidth > 0) {
            val color = parseCssColor(stroke) ?: stroke
            css["border"] = "${fmt(strokeWidth / density)}pt solid $color"
            css["box-sizing"] = "border-box"
        }

        // Detect rounded rect paths from Skia's SVGCanvas.
        // Skia serializes RoundedCornerShape as: M L Q*N L Q*N L Q*N L Q*N Z
        // with many Q segments per corner arc. The radius = arc span distance.
        val roundedRectRadius = detectRoundedRectRadius(d)
        if (roundedRectRadius > 0) {
            css["border-radius"] = "${fmt(roundedRectRadius / density)}pt"
        }

        // Capture fill-rule (evenodd paths must use inline SVG, CSS can't express them)
        val fillRule = attr(elem, "fill-rule")
        if (fillRule != null) {
            css["--fill-rule"] = fillRule  // Internal marker for LayoutAnalyzer
        }

        // Store raw SVG for fallback
        val rawSvg = elementToSvgString(elem)

        return SvgNode(
            type = SvgNodeType.PATH,
            x = px / density,
            y = py / density,
            width = pw / density,
            height = ph / density,
            cssProperties = css,
            attributes = mapOf(
                "d" to d,
                "rawSvg" to rawSvg,
                "fillRule" to (fillRule ?: ""),
                "bboxX" to fmt(bboxPx[0]),
                "bboxY" to fmt(bboxPx[1]),
                "bboxW" to fmt(bboxPx[2]),
                "bboxH" to fmt(bboxPx[3]),
            ),
        )
    }

    private fun parseImage(elem: Element, density: Float, tx: Float, ty: Float, scale: Float = 1f): SvgNode {
        val x = attrF(elem, "x") * scale + tx
        val y = attrF(elem, "y") * scale + ty
        val w = attrF(elem, "width") * scale
        val h = attrF(elem, "height") * scale
        val href = elem.getAttribute("href").ifEmpty {
            elem.getAttributeNS("http://www.w3.org/1999/xlink", "href")
        } ?: ""

        return SvgNode(
            type = SvgNodeType.IMAGE,
            x = x / density,
            y = y / density,
            width = w / density,
            height = h / density,
            attributes = mapOf("src" to href),
        )
    }

    private fun parseLine(elem: Element, density: Float, tx: Float, ty: Float): SvgNode {
        val x1 = attrF(elem, "x1") + tx
        val y1 = attrF(elem, "y1") + ty
        val x2 = attrF(elem, "x2") + tx
        val y2 = attrF(elem, "y2") + ty
        val css = mutableMapOf<String, String>()
        extractFillStroke(elem, css)

        return SvgNode(
            type = SvgNodeType.LINE,
            x = minOf(x1, x2) / density,
            y = minOf(y1, y2) / density,
            width = kotlin.math.abs(x2 - x1) / density,
            height = kotlin.math.abs(y2 - y1) / density,
            cssProperties = css,
        )
    }

    private fun parseCircle(elem: Element, density: Float, tx: Float, ty: Float): SvgNode {
        val cx = attrF(elem, "cx") + tx
        val cy = attrF(elem, "cy") + ty
        val r = attrF(elem, "r")
        val css = mutableMapOf<String, String>()
        extractFillStroke(elem, css)
        css["border-radius"] = "50%"

        return SvgNode(
            type = SvgNodeType.CIRCLE,
            x = (cx - r) / density,
            y = (cy - r) / density,
            width = (r * 2) / density,
            height = (r * 2) / density,
            cssProperties = css,
        )
    }

    private fun parseEllipse(elem: Element, density: Float, tx: Float, ty: Float): SvgNode {
        val cx = attrF(elem, "cx") + tx
        val cy = attrF(elem, "cy") + ty
        val rx = attrF(elem, "rx")
        val ry = attrF(elem, "ry")
        val css = mutableMapOf<String, String>()
        extractFillStroke(elem, css)
        css["border-radius"] = "50%"

        return SvgNode(
            type = SvgNodeType.ELLIPSE,
            x = (cx - rx) / density,
            y = (cy - ry) / density,
            width = (rx * 2) / density,
            height = (ry * 2) / density,
            cssProperties = css,
        )
    }

    private fun parsePoly(elem: Element, density: Float, tx: Float, ty: Float): SvgNode {
        val points = attr(elem, "points") ?: ""
        val coords = points.trim().split(Regex("[,\\s]+")).mapNotNull { it.toFloatOrNull() }
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE; var maxY = Float.MIN_VALUE
        for (i in coords.indices step 2) {
            val px = coords[i] + tx; val py = coords.getOrElse(i + 1) { 0f } + ty
            if (px < minX) minX = px; if (py < minY) minY = py
            if (px > maxX) maxX = px; if (py > maxY) maxY = py
        }
        val css = mutableMapOf<String, String>()
        extractFillStroke(elem, css)

        return SvgNode(
            type = SvgNodeType.POLY,
            x = minX / density,
            y = minY / density,
            width = (maxX - minX) / density,
            height = (maxY - minY) / density,
            cssProperties = css,
        )
    }

    // --- Helpers ---

    /**
     * Resolves a clipPath element to a rect: [x, y, w, h, rx, ry] in SVG px.
     * Returns null if the clip cannot be resolved to a simple rect.
     */
    private fun resolveClipRect(clipElem: Element): FloatArray? {
        for (i in 0 until clipElem.childNodes.length) {
            val node = clipElem.childNodes.item(i)
            if (node.nodeType != Node.ELEMENT_NODE) continue
            val child = node as Element
            when (child.localName) {
                "rect" -> {
                    val x = child.getAttribute("x").toFloatOrNull() ?: 0f
                    val y = child.getAttribute("y").toFloatOrNull() ?: 0f
                    val w = child.getAttribute("width").toFloatOrNull() ?: continue
                    val h = child.getAttribute("height").toFloatOrNull() ?: continue
                    val rx = child.getAttribute("rx").toFloatOrNull() ?: 0f
                    val ry = child.getAttribute("ry").toFloatOrNull() ?: rx
                    return floatArrayOf(x, y, w, h, rx, ry)
                }
                "circle" -> {
                    val cx = child.getAttribute("cx").toFloatOrNull() ?: 0f
                    val cy = child.getAttribute("cy").toFloatOrNull() ?: 0f
                    val r = child.getAttribute("r").toFloatOrNull() ?: continue
                    return floatArrayOf(cx - r, cy - r, r * 2, r * 2, r, r)
                }
                "path" -> {
                    // Try to estimate bbox from the path for rectangular clips
                    val d = child.getAttribute("d")
                    if (d.isNotEmpty()) {
                        val bbox = estimatePathBBox(d) ?: continue
                        val radius = detectRoundedRectRadius(d)
                        return floatArrayOf(bbox[0], bbox[1], bbox[2], bbox[3], radius, radius)
                    }
                }
            }
        }
        return null
    }

    /**
     * Extracts translation (tx, ty) and scale (sx, sy) from a transform attribute.
     * Handles both `translate(tx,ty)` and `matrix(a,b,c,d,tx,ty)`.
     */
    private fun resolveTransform(elem: Element): Transform {
        val transform = elem.getAttribute("transform")
        if (transform.isEmpty()) return Transform()

        var tx = 0f; var ty = 0f; var sx = 1f; var sy = 1f

        // Check for translate
        TRANSLATE_RE.find(transform)?.let { match ->
            val params = match.groupValues[1].split(Regex("[,\\s]+")).mapNotNull { it.trim().toFloatOrNull() }
            tx += params.getOrElse(0) { 0f }
            ty += params.getOrElse(1) { 0f }
        }

        // Check for matrix(a, b, c, d, e, f) — e=tx, f=ty, a=sx, d=sy
        MATRIX_RE.find(transform)?.let { match ->
            val params = match.groupValues[1].split(Regex("[,\\s]+")).mapNotNull { it.trim().toFloatOrNull() }
            if (params.size >= 6) {
                sx = params[0]
                sy = params[3]
                tx += params[4]
                ty += params[5]
            }
        }

        // Check for scale
        SCALE_RE.find(transform)?.let { match ->
            val params = match.groupValues[1].split(Regex("[,\\s]+")).mapNotNull { it.trim().toFloatOrNull() }
            sx *= params.getOrElse(0) { 1f }
            sy *= params.getOrElse(1) { sx }
        }

        return Transform(tx, ty, sx, sy)
    }

    private data class Transform(
        val tx: Float = 0f,
        val ty: Float = 0f,
        val sx: Float = 1f,
        val sy: Float = 1f,
    )

    private fun extractFillStroke(elem: Element, css: MutableMap<String, String>) {
        val fill = attr(elem, "fill")
        val fillOpacity = attr(elem, "fill-opacity")?.toFloatOrNull()
        val stroke = attr(elem, "stroke")
        val strokeWidth = attr(elem, "stroke-width")?.toFloatOrNull()
        val opacity = attr(elem, "opacity")?.toFloatOrNull()

        // SVG default fill is black when not specified (except for elements with stroke-only)
        val effectiveFill = fill ?: if (stroke != null && stroke != "none") "none" else "#000"
        if (effectiveFill != "none") {
            val color = parseCssColor(effectiveFill)
            if (color != null) {
                if (fillOpacity != null && fillOpacity < 1f) {
                    css["background-color"] = applyOpacity(color, fillOpacity)
                } else {
                    css["background-color"] = color
                }
            }
        }
        if (stroke != null && stroke != "none" && strokeWidth != null && strokeWidth > 0) {
            val color = parseCssColor(stroke) ?: stroke
            // strokeWidth is in SVG px — stored as-is. The LayoutAnalyzer will use it
            // in CSS where all dimensions are already converted to pt by the parseRect/etc callers.
            css["border"] = "${fmt(strokeWidth)}px solid $color"
        }
        if (opacity != null && opacity < 1f) {
            css["opacity"] = fmt(opacity)
        }
    }

    private fun applyOpacity(color: String, opacity: Float): String {
        // Convert hex color to rgba with opacity
        if (color.startsWith("#") && color.length == 7) {
            val r = color.substring(1, 3).toInt(16)
            val g = color.substring(3, 5).toInt(16)
            val b = color.substring(5, 7).toInt(16)
            return "rgba($r,$g,$b,${fmt(opacity)})"
        }
        return color
    }

    private fun parseCssColor(color: String): String? {
        val c = color.trim()
        return when {
            c.startsWith("#") -> c
            c.startsWith("rgba(") || c.startsWith("rgb(") -> c
            c.lowercase() in CSS_NAMED_COLORS -> CSS_NAMED_COLORS[c.lowercase()]
            else -> null
        }
    }

    /**
     * Detects if a path is a rounded rect from Skia's SVGCanvas and returns the radius.
     *
     * Skia serializes rounded rects as: M L Q*N L Q*N L Q*N L Q*N Z
     * where each corner has N quadratic bezier segments (typically 32).
     * The radius equals the total arc span of one corner's Q segments.
     *
     * Returns the radius in SVG px, or 0 if the path is not a rounded rect.
     */
    private fun detectRoundedRectRadius(d: String): Float {
        val tokens = PATH_TOKEN_RE.findAll(d).map { it.value }.toList()
        if (tokens.isEmpty()) return 0f

        // Count the command structure: M, L, Q groups, Z
        var i = 0
        var lineCount = 0
        var qGroupCount = 0
        var inQGroup = false
        var firstQGroupStartX = 0f
        var firstQGroupStartY = 0f
        var firstQGroupEndX = 0f
        var firstQGroupEndY = 0f
        var cx = 0f; var cy = 0f
        var lastCmd = ' '
        var hasOtherCurves = false

        fun nf(): Float = if (i < tokens.size) tokens[i++].toFloat() else 0f

        while (i < tokens.size) {
            val tok = tokens[i]
            val cmd: Char = if (tok.length == 1 && tok[0] in "MmLlHhVvCcSsQqTtAaZz") {
                i++; tok[0]
            } else {
                when (lastCmd) { 'M' -> 'L'; 'm' -> 'l'; 'Z', 'z', ' ' -> break; else -> lastCmd }
            }

            when (cmd) {
                'M' -> { cx = nf(); cy = nf(); inQGroup = false }
                'L' -> {
                    if (inQGroup) {
                        // End of a Q group
                        if (qGroupCount == 1) {
                            firstQGroupEndX = cx; firstQGroupEndY = cy
                        }
                        inQGroup = false
                    }
                    cx = nf(); cy = nf()
                    lineCount++
                }
                'Q' -> {
                    if (!inQGroup) {
                        // Start of a new Q group
                        qGroupCount++
                        inQGroup = true
                        if (qGroupCount == 1) {
                            firstQGroupStartX = cx; firstQGroupStartY = cy
                        }
                    }
                    nf(); nf() // control point
                    cx = nf(); cy = nf() // end point
                }
                'C', 'S', 'A' -> { hasOtherCurves = true; break }
                'H' -> { cx = nf(); lineCount++ }
                'V' -> { cy = nf(); lineCount++ }
                'Z', 'z' -> {
                    if (inQGroup) {
                        if (qGroupCount == 1) {
                            firstQGroupEndX = cx; firstQGroupEndY = cy
                        }
                        inQGroup = false
                    }
                    break
                }
                else -> break
            }
            lastCmd = cmd
        }

        if (hasOtherCurves) return 0f

        // A rounded rect has exactly 4 line segments and 4 Q-curve groups
        // (or 3 lines + 4 Q groups if one side is all curves)
        if (qGroupCount != 4 || lineCount < 3) return 0f

        // The radius is the arc span of the first corner's Q group
        val dx = kotlin.math.abs(firstQGroupEndX - firstQGroupStartX)
        val dy = kotlin.math.abs(firstQGroupEndY - firstQGroupStartY)
        return maxOf(dx, dy)
    }

    private fun estimatePathBBox(d: String): FloatArray? {
        val tokens = PATH_TOKEN_RE.findAll(d).map { it.value }.toList()
        if (tokens.isEmpty()) return null
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE; var maxY = Float.MIN_VALUE
        var cx = 0f; var cy = 0f; var i = 0; var lastCmd = ' '

        fun track(x: Float, y: Float) {
            if (x < minX) minX = x; if (y < minY) minY = y
            if (x > maxX) maxX = x; if (y > maxY) maxY = y
        }
        fun nf(): Float = if (i < tokens.size) tokens[i++].toFloat() else 0f

        while (i < tokens.size) {
            val tok = tokens[i]
            val cmd: Char = if (tok.length == 1 && tok[0] in "MmLlHhVvCcSsQqTtAaZz") {
                i++; tok[0]
            } else {
                when (lastCmd) { 'M' -> 'L'; 'm' -> 'l'; 'Z', 'z', ' ' -> break; else -> lastCmd }
            }
            when (cmd) {
                'M' -> { cx = nf(); cy = nf(); track(cx, cy) }
                'm' -> { cx += nf(); cy += nf(); track(cx, cy) }
                'L' -> { cx = nf(); cy = nf(); track(cx, cy) }
                'l' -> { cx += nf(); cy += nf(); track(cx, cy) }
                'H' -> { cx = nf(); track(cx, cy) }
                'h' -> { cx += nf(); track(cx, cy) }
                'V' -> { cy = nf(); track(cx, cy) }
                'v' -> { cy += nf(); track(cx, cy) }
                'C' -> { track(nf(), nf()); track(nf(), nf()); cx = nf(); cy = nf(); track(cx, cy) }
                'c' -> { track(cx + nf(), cy + nf()); track(cx + nf(), cy + nf()); cx += nf(); cy += nf(); track(cx, cy) }
                'S' -> { track(nf(), nf()); cx = nf(); cy = nf(); track(cx, cy) }
                's' -> { track(cx + nf(), cy + nf()); cx += nf(); cy += nf(); track(cx, cy) }
                'Q' -> { track(nf(), nf()); cx = nf(); cy = nf(); track(cx, cy) }
                'q' -> { track(cx + nf(), cy + nf()); cx += nf(); cy += nf(); track(cx, cy) }
                'T' -> { cx = nf(); cy = nf(); track(cx, cy) }
                't' -> { cx += nf(); cy += nf(); track(cx, cy) }
                'A' -> { nf(); nf(); nf(); nf(); nf(); cx = nf(); cy = nf(); track(cx, cy) }
                'a' -> { nf(); nf(); nf(); nf(); nf(); cx += nf(); cy += nf(); track(cx, cy) }
                'Z', 'z' -> {}
            }
            lastCmd = cmd
        }
        if (minX > maxX) return null
        return floatArrayOf(minX, minY, maxX - minX, maxY - minY)
    }

    private fun elementToSvgString(elem: Element): String = buildString {
        append("<${elem.localName}")
        val attrs = elem.attributes
        for (j in 0 until attrs.length) {
            val a = attrs.item(j)
            if (a.localName == "transform") continue
            append(" ${a.localName}=\"${a.nodeValue}\"")
        }
        append("/>")
    }

    private fun collectDefs(parent: Element, defs: MutableMap<String, Element>) {
        for (i in 0 until parent.childNodes.length) {
            val node = parent.childNodes.item(i)
            if (node.nodeType != Node.ELEMENT_NODE) continue
            val elem = node as Element
            when (elem.localName) {
                "defs" -> {
                    for (j in 0 until elem.childNodes.length) {
                        val defNode = elem.childNodes.item(j)
                        if (defNode.nodeType != Node.ELEMENT_NODE) continue
                        val defElem = defNode as Element
                        val id = defElem.getAttribute("id")
                        if (id.isNotEmpty()) defs[id] = defElem
                    }
                }
                "clipPath" -> {
                    val id = elem.getAttribute("id")
                    if (id.isNotEmpty()) defs[id] = elem
                }
                "g" -> collectDefs(elem, defs)
            }
        }
    }

    /** Resolves an SVG attribute, checking `style` attribute first. */
    private fun attr(elem: Element, name: String): String? {
        val style = elem.getAttribute("style")
        if (style.isNotEmpty()) {
            for (prop in style.split(";")) {
                val colon = prop.indexOf(':')
                if (colon < 0) continue
                if (prop.substring(0, colon).trim() == name) {
                    return prop.substring(colon + 1).trim().takeIf { it.isNotEmpty() }
                }
            }
        }
        return elem.getAttribute(name).takeIf { it.isNotEmpty() }
    }

    private fun attrF(elem: Element, name: String): Float =
        attr(elem, name)?.toFloatOrNull() ?: 0f

    private fun fmt(v: Float): String {
        if (v == 0f) return "0"
        val s = "%.4f".format(v)
        return s.trimEnd('0').trimEnd('.')
    }

    private val TRANSLATE_RE = Regex("""translate\(([^)]+)\)""")
    private val MATRIX_RE = Regex("""matrix\(([^)]+)\)""")
    private val SCALE_RE = Regex("""scale\(([^)]+)\)""")
    private val PATH_TOKEN_RE = Regex("""[MmLlHhVvCcSsQqTtAaZz]|[+-]?(?:\d+\.?\d*|\.\d+)""")

    private val CSS_NAMED_COLORS = mapOf(
        "black" to "#000000", "white" to "#ffffff", "red" to "#ff0000",
        "green" to "#008000", "blue" to "#0000ff", "gray" to "#808080",
        "grey" to "#808080", "silver" to "#c0c0c0", "orange" to "#ffa500",
        "purple" to "#800080", "yellow" to "#ffff00", "cyan" to "#00ffff",
        "magenta" to "#ff00ff", "pink" to "#ffc0cb", "brown" to "#a52a2a",
        "navy" to "#000080", "teal" to "#008080", "olive" to "#808000",
        "lime" to "#00ff00", "maroon" to "#800000", "aqua" to "#00ffff",
        "fuchsia" to "#ff00ff",
    )
}

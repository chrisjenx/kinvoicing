package com.chrisjenx.composepdf.internal

import com.chrisjenx.composepdf.HoverStyles
import com.chrisjenx.composepdf.PdfButtonAnnotation
import com.chrisjenx.composepdf.PdfCheckboxAnnotation
import com.chrisjenx.composepdf.PdfElementAnnotation
import com.chrisjenx.composepdf.PdfHoverAnnotation
import com.chrisjenx.composepdf.PdfImageAnnotation
import com.chrisjenx.composepdf.PdfListAnnotation
import com.chrisjenx.composepdf.PdfLinkAnnotation
import com.chrisjenx.composepdf.PdfPageConfig
import com.chrisjenx.composepdf.PdfRadioButtonAnnotation
import com.chrisjenx.composepdf.PdfSelectAnnotation
import com.chrisjenx.composepdf.PdfSliderAnnotation
import com.chrisjenx.composepdf.PdfTableAnnotation
import com.chrisjenx.composepdf.PdfTextFieldAnnotation
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Converts Skia-generated SVG to self-contained HTML+CSS.
 *
 * Handles the same SVG elements as [SvgToPdfConverter]: shapes (rect, circle, ellipse,
 * line, polyline, polygon, path), text, images, groups, definitions, clipping,
 * transforms, fills, strokes, and opacity.
 *
 * Simple shapes are converted to CSS-styled `<div>` elements. Complex paths and
 * line-based elements fall back to inline `<svg>`. Text becomes `<span>` elements.
 */
internal object SvgToHtmlConverter {

    /**
     * Converts SVG pages to a self-contained HTML document.
     *
     * @param svgPages SVG string per page.
     * @param config Page configuration (dimensions, margins).
     * @param density Render density (SVG px / density = CSS px).
     * @param linksByPage Link annotations per page.
     * @param fontBase64 Map of font variant key (e.g. "400-normal") to base64-encoded TTF data.
     * @return Complete HTML document string.
     */
    fun convert(
        svgPages: List<String>,
        config: PdfPageConfig,
        density: Float,
        linksByPage: List<List<PdfLinkAnnotation>>,
        elementsByPage: List<List<PdfElementAnnotation>> = emptyList(),
        fontBase64: Map<String, String>,
    ): String {
        val pageWidthPt = config.width.value
        val pageHeightPt = config.height.value
        val marginLeft = config.margins.left.value
        val marginTop = config.margins.top.value
        val contentWidthPt = config.contentWidth.value
        val contentHeightPt = config.contentHeight.value

        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"en\">")
            appendLine("<head>")
            appendLine("<meta charset=\"utf-8\">")
            appendLine("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
            appendLine("<style>")

            // Emit @font-face declarations
            for ((variant, base64) in fontBase64) {
                val parts = variant.split("-")
                val weight = parts.getOrElse(0) { "400" }
                val style = parts.getOrElse(1) { "normal" }
                appendLine("@font-face { font-family: 'Inter'; font-weight: $weight; font-style: $style; src: url(data:font/ttf;base64,$base64) format('truetype'); }")
            }

            appendLine("* { margin: 0; padding: 0; box-sizing: border-box; }")
            appendLine("body { background: #f5f5f5; }")
            appendLine(".page { position: relative; width: ${fmt(pageWidthPt)}pt; height: ${fmt(pageHeightPt)}pt; background: #fff; overflow: hidden; margin: 0 auto; }")
            appendLine(".page + .page { margin-top: 20px; }")
            appendLine(".content { position: absolute; left: ${fmt(marginLeft)}pt; top: ${fmt(marginTop)}pt; width: ${fmt(contentWidthPt)}pt; height: ${fmt(contentHeightPt)}pt; }")
            appendLine("@media print { body { background: none; } .page { margin: 0; page-break-after: always; } .page + .page { margin-top: 0; } }")
            emitHoverCss(this, elementsByPage)
            appendLine("</style>")
            appendLine("</head>")
            appendLine("<body>")

            for ((pageIndex, svg) in svgPages.withIndex()) {
                val links = linksByPage.getOrElse(pageIndex) { emptyList() }
                val elements = elementsByPage.getOrElse(pageIndex) { emptyList() }
                appendLine("<div class=\"page\">")
                appendLine("<div class=\"content\">")
                renderSvgPage(this, svg, density, links, elements)
                appendLine("</div>")
                appendLine("</div>")
            }

            appendLine("</body>")
            appendLine("</html>")
        }
    }

    private fun renderSvgPage(
        sb: StringBuilder,
        svg: String,
        density: Float,
        links: List<PdfLinkAnnotation>,
        elements: List<PdfElementAnnotation>,
    ) {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val xmlDoc = factory.newDocumentBuilder().parse(svg.byteInputStream())
        val svgRoot = xmlDoc.documentElement

        val defs = mutableMapOf<String, Element>()
        collectDefs(svgRoot, defs)

        val renderer = PageRenderer(density, defs, elements)
        renderer.renderChildren(sb, svgRoot)

        // Emit native HTML elements for annotations (replaces SVG-derived content)
        emitNativeElements(sb, elements)

        // Add link annotations as positioned <a> overlays
        for (link in links) {
            val x = fmt(link.x)
            val y = fmt(link.y)
            val w = fmt(link.width)
            val h = fmt(link.height)
            sb.appendLine("<a href=\"${escapeAttr(link.href)}\" style=\"position:absolute;left:${x}pt;top:${y}pt;width:${w}pt;height:${h}pt;display:block\" target=\"_blank\"></a>")
        }
    }

    private fun collectDefs(parent: Element, defs: MutableMap<String, Element>) {
        val children = parent.childNodes
        for (i in 0 until children.length) {
            val node = children.item(i)
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

    /** Tolerance in pt for region matching (compensates floating-point rounding). */
    private const val REGION_TOLERANCE = 0.5f

    /** SVG elements that are structural and should not be suppressed directly. */
    private val STRUCTURAL_ELEMENTS = setOf("defs", "clipPath", "g")

    /**
     * Emits native HTML elements for annotations, replacing SVG-derived content.
     */
    private fun emitNativeElements(sb: StringBuilder, elements: List<PdfElementAnnotation>) {
        var hoverIndex = 0
        for (elem in elements) {
            when (elem) {
                is PdfTableAnnotation -> emitTable(sb, elem)
                is PdfListAnnotation -> emitList(sb, elem)
                is PdfButtonAnnotation -> emitButton(sb, elem)
                is PdfTextFieldAnnotation -> emitTextField(sb, elem)
                is PdfImageAnnotation -> emitImage(sb, elem)
                is PdfHoverAnnotation -> emitHover(sb, elem, hoverIndex++)
                is PdfCheckboxAnnotation,
                is PdfRadioButtonAnnotation,
                is PdfSelectAnnotation,
                is PdfSliderAnnotation -> {} // Handled by SvgToSemanticHtmlConverter
            }
        }
    }

    private fun emitTable(sb: StringBuilder, table: PdfTableAnnotation) {
        val x = fmt(table.x); val y = fmt(table.y)
        val w = fmt(table.width)
        sb.appendLine("<table style=\"position:absolute;left:${x}pt;top:${y}pt;width:${w}pt;border-collapse:collapse;\">")
        if (table.caption != null) {
            sb.appendLine("<caption>${escapeHtml(table.caption)}</caption>")
        }
        var inThead = false
        var inTbody = false
        for (row in table.rows) {
            if (row.isHeader && !inThead) {
                if (inTbody) { sb.appendLine("</tbody>"); inTbody = false }
                sb.appendLine("<thead>"); inThead = true
            } else if (!row.isHeader && !inTbody) {
                if (inThead) { sb.appendLine("</thead>"); inThead = false }
                sb.appendLine("<tbody>"); inTbody = true
            }
            sb.append("<tr>")
            val tag = if (row.isHeader) "th" else "td"
            for (cell in row.cells) {
                val attrs = buildString {
                    if (cell.colSpan > 1) append(" colspan=\"${cell.colSpan}\"")
                    if (cell.rowSpan > 1) append(" rowspan=\"${cell.rowSpan}\"")
                    if (cell.width > 0) append(" style=\"width:${fmt(cell.width)}pt\"")
                }
                sb.append("<$tag$attrs>${escapeHtml(cell.text)}</$tag>")
            }
            sb.appendLine("</tr>")
        }
        if (inThead) sb.appendLine("</thead>")
        if (inTbody) sb.appendLine("</tbody>")
        sb.appendLine("</table>")
    }

    private fun emitList(sb: StringBuilder, list: PdfListAnnotation) {
        val x = fmt(list.x); val y = fmt(list.y)
        val w = fmt(list.width)
        val tag = if (list.ordered) "ol" else "ul"
        sb.appendLine("<$tag style=\"position:absolute;left:${x}pt;top:${y}pt;width:${w}pt;\">")
        for (item in list.items) {
            sb.appendLine("<li>${escapeHtml(item.text)}</li>")
        }
        sb.appendLine("</$tag>")
    }

    private fun emitButton(sb: StringBuilder, button: PdfButtonAnnotation) {
        val x = fmt(button.x); val y = fmt(button.y)
        val w = fmt(button.width); val h = fmt(button.height)
        val onclick = if (button.onClick != null) " onclick=\"${escapeAttr(button.onClick)}\"" else ""
        sb.appendLine("<button name=\"${escapeAttr(button.name)}\"$onclick style=\"position:absolute;left:${x}pt;top:${y}pt;width:${w}pt;height:${h}pt;cursor:pointer;\">${escapeHtml(button.label)}</button>")
    }

    private fun emitTextField(sb: StringBuilder, field: PdfTextFieldAnnotation) {
        val x = fmt(field.x); val y = fmt(field.y)
        val w = fmt(field.width); val h = fmt(field.height)
        val commonStyle = "position:absolute;left:${x}pt;top:${y}pt;width:${w}pt;height:${h}pt;"
        val maxLen = if (field.maxLength > 0) " maxlength=\"${field.maxLength}\"" else ""
        if (field.multiline) {
            sb.appendLine("<textarea name=\"${escapeAttr(field.name)}\" placeholder=\"${escapeAttr(field.placeholder)}\"$maxLen style=\"$commonStyle\">${escapeHtml(field.value)}</textarea>")
        } else {
            sb.appendLine("<input type=\"text\" name=\"${escapeAttr(field.name)}\" value=\"${escapeAttr(field.value)}\" placeholder=\"${escapeAttr(field.placeholder)}\"$maxLen style=\"$commonStyle\">")
        }
    }

    private fun emitImage(sb: StringBuilder, image: PdfImageAnnotation) {
        val x = fmt(image.x); val y = fmt(image.y)
        val w = fmt(image.width); val h = fmt(image.height)
        sb.appendLine("<div role=\"img\" aria-label=\"${escapeAttr(image.altText)}\" style=\"position:absolute;left:${x}pt;top:${y}pt;width:${w}pt;height:${h}pt;\"></div>")
    }

    private fun emitHover(sb: StringBuilder, hover: PdfHoverAnnotation, index: Int) {
        val x = fmt(hover.x); val y = fmt(hover.y)
        val w = fmt(hover.width); val h = fmt(hover.height)
        sb.appendLine("<div class=\"pdf-hover-$index\" style=\"position:absolute;left:${x}pt;top:${y}pt;width:${w}pt;height:${h}pt;\"></div>")
    }

    /**
     * Generates CSS hover rules for hover annotations.
     * Called during the `<style>` block generation.
     */
    private fun emitHoverCss(sb: StringBuilder, elementsByPage: List<List<PdfElementAnnotation>>) {
        var hoverIndex = 0
        for (elements in elementsByPage) {
            for (elem in elements) {
                if (elem is PdfHoverAnnotation) {
                    sb.append(".pdf-hover-$hoverIndex:hover {")
                    val styles = elem.hoverStyles
                    if (styles.backgroundColor != null) sb.append(" background-color:${styles.backgroundColor};")
                    if (styles.opacity != null) sb.append(" opacity:${styles.opacity};")
                    if (styles.scale != null) sb.append(" transform:scale(${styles.scale});")
                    sb.append(" cursor:${styles.cursor};")
                    for ((k, v) in styles.customCss) {
                        sb.append(" $k:$v;")
                    }
                    sb.appendLine(" }")
                    hoverIndex++
                }
            }
        }
    }

    /**
     * Renders SVG elements to HTML strings for a single page.
     */
    private class PageRenderer(
        private val density: Float,
        private val defs: Map<String, Element>,
        private val elements: List<PdfElementAnnotation> = emptyList(),
    ) {
        // Track unique clip-path IDs for inline SVG clip definitions
        private var clipCounter = 0

        // Accumulated translation offset for region suppression.
        // Tracks translate transforms through group hierarchy to compute absolute positions.
        private var accTx = 0f
        private var accTy = 0f

        fun renderChildren(sb: StringBuilder, parent: Element) {
            for (i in 0 until parent.childNodes.length) {
                val node = parent.childNodes.item(i)
                if (node.nodeType == Node.ELEMENT_NODE) renderElement(sb, node as Element)
            }
        }

        private fun renderElement(sb: StringBuilder, elem: Element) {
            // Skip elements that fall inside an annotation region (native HTML replaces them)
            if (elements.isNotEmpty() && elem.localName !in STRUCTURAL_ELEMENTS) {
                if (isInsideAnnotationRegion(elem)) return
            }

            when (elem.localName) {
                "defs", "clipPath" -> {}
                "rect" -> renderRect(sb, elem)
                "circle" -> renderCircle(sb, elem)
                "ellipse" -> renderEllipse(sb, elem)
                "line" -> renderInlineSvg(sb, elem)
                "polyline" -> renderInlineSvg(sb, elem)
                "polygon" -> renderInlineSvg(sb, elem)
                "path" -> renderPath(sb, elem)
                "text" -> renderText(sb, elem)
                "g" -> renderGroup(sb, elem)
                "use" -> renderUse(sb, elem)
                "image" -> renderImage(sb, elem)
            }
        }

        /**
         * Checks if an SVG element falls entirely inside any annotation region.
         * Uses the accumulated translate offset to compute absolute coordinates.
         */
        private fun isInsideAnnotationRegion(elem: Element): Boolean {
            val bbox = estimateBBox(elem) ?: return false
            // Apply element's own translate (if any) + accumulated group translates
            var ex = bbox[0]; var ey = bbox[1]
            val ew = bbox[2]; val eh = bbox[3]

            // Parse element's own transform for translate offset
            val transform = elem.getAttribute("transform")
            if (transform.isNotEmpty()) {
                for (match in TRANSFORM_RE.findAll(transform)) {
                    if (match.groupValues[1] == "translate") {
                        val params = match.groupValues[2].split(Regex("[,\\s]+"))
                            .mapNotNull { it.trim().toFloatOrNull() }
                        ex += params.getOrElse(0) { 0f }
                        ey += params.getOrElse(1) { 0f }
                    }
                }
            }

            // Convert to absolute page coordinates (scaled to pt)
            val absX = scale(ex + accTx)
            val absY = scale(ey + accTy)
            val absW = scale(ew)
            val absH = scale(eh)

            for (ann in elements) {
                if (absX >= ann.x - REGION_TOLERANCE &&
                    absY >= ann.y - REGION_TOLERANCE &&
                    absX + absW <= ann.x + ann.width + REGION_TOLERANCE &&
                    absY + absH <= ann.y + ann.height + REGION_TOLERANCE
                ) {
                    return true
                }
            }
            return false
        }

        // ── Rect → <div> ──

        private fun renderRect(sb: StringBuilder, elem: Element) {
            val x = scale(attrF(elem, "x"))
            val y = scale(attrF(elem, "y"))
            val w = attrF(elem, "width").let { if (it == 0f) return else scale(it) }
            val h = attrF(elem, "height").let { if (it == 0f) return else scale(it) }
            val rx = scale(attrF(elem, "rx"))
            val ry = scale(attrF(elem, "ry").let { if (it == 0f) attrF(elem, "rx") else it })

            val fill = attr(elem, "fill")
            val stroke = attr(elem, "stroke")
            val hasFill = fill != "none"
            val hasStroke = stroke != null && stroke != "none"

            // If has both fill and stroke with border-radius, fall back to inline SVG
            // to avoid CSS border/background interaction issues
            if (hasFill && hasStroke && (rx > 0 || ry > 0)) {
                renderInlineSvg(sb, elem)
                return
            }

            val style = buildString {
                append("position:absolute;")
                append("left:${fmt(x)}pt;top:${fmt(y)}pt;")
                append("width:${fmt(w)}pt;height:${fmt(h)}pt;")
                if (rx > 0 || ry > 0) {
                    val brx = fmt(rx)
                    val bry = fmt(ry)
                    append("border-radius:${brx}pt/${bry}pt;")
                }
                if (hasFill) {
                    val color = fill?.let { parseCssColor(it) }
                    if (color != null) append("background:$color;")
                    else append("background:#000;")
                }
                if (hasStroke) {
                    val strokeWidth = scale(attrF(elem, "stroke-width").let { if (it == 0f) 1f else it })
                    val strokeColor = parseCssColor(stroke!!) ?: "#000"
                    append("border:${fmt(strokeWidth)}pt solid $strokeColor;")
                }
                appendOpacity(this, elem)
                appendTransform(this, elem)
            }

            sb.appendLine("<div style=\"$style\"></div>")
        }

        // ── Circle → <div> ──

        private fun renderCircle(sb: StringBuilder, elem: Element) {
            val cx = scale(attrF(elem, "cx"))
            val cy = scale(attrF(elem, "cy"))
            val r = attrF(elem, "r").let { if (it == 0f) return else scale(it) }

            val fill = attr(elem, "fill")
            val stroke = attr(elem, "stroke")
            val hasFill = fill != "none"
            val hasStroke = stroke != null && stroke != "none"

            if (hasFill && hasStroke) {
                renderInlineSvg(sb, elem)
                return
            }

            val d = r * 2
            val style = buildString {
                append("position:absolute;")
                append("left:${fmt(cx - r)}pt;top:${fmt(cy - r)}pt;")
                append("width:${fmt(d)}pt;height:${fmt(d)}pt;")
                append("border-radius:50%;")
                if (hasFill) {
                    val color = fill?.let { parseCssColor(it) }
                    if (color != null) append("background:$color;")
                    else append("background:#000;")
                }
                if (hasStroke) {
                    val strokeWidth = scale(attrF(elem, "stroke-width").let { if (it == 0f) 1f else it })
                    val strokeColor = parseCssColor(stroke!!) ?: "#000"
                    append("border:${fmt(strokeWidth)}pt solid $strokeColor;")
                }
                appendOpacity(this, elem)
                appendTransform(this, elem)
            }

            sb.appendLine("<div style=\"$style\"></div>")
        }

        // ── Ellipse → <div> ──

        private fun renderEllipse(sb: StringBuilder, elem: Element) {
            val cx = scale(attrF(elem, "cx"))
            val cy = scale(attrF(elem, "cy"))
            val rx = attrF(elem, "rx").let { if (it == 0f) return else scale(it) }
            val ry = attrF(elem, "ry").let { if (it == 0f) return else scale(it) }

            val fill = attr(elem, "fill")
            val stroke = attr(elem, "stroke")
            val hasFill = fill != "none"
            val hasStroke = stroke != null && stroke != "none"

            if (hasFill && hasStroke) {
                renderInlineSvg(sb, elem)
                return
            }

            val style = buildString {
                append("position:absolute;")
                append("left:${fmt(cx - rx)}pt;top:${fmt(cy - ry)}pt;")
                append("width:${fmt(rx * 2)}pt;height:${fmt(ry * 2)}pt;")
                append("border-radius:50%;")
                if (hasFill) {
                    val color = fill?.let { parseCssColor(it) }
                    if (color != null) append("background:$color;")
                    else append("background:#000;")
                }
                if (hasStroke) {
                    val strokeWidth = scale(attrF(elem, "stroke-width").let { if (it == 0f) 1f else it })
                    val strokeColor = parseCssColor(stroke!!) ?: "#000"
                    append("border:${fmt(strokeWidth)}pt solid $strokeColor;")
                }
                appendOpacity(this, elem)
                appendTransform(this, elem)
            }

            sb.appendLine("<div style=\"$style\"></div>")
        }

        // ── Text → <span> ──

        private fun renderText(sb: StringBuilder, elem: Element) {
            val text = elem.textContent.trim()
            if (text.isEmpty()) return

            val fontSize = scale(attrF(elem, "font-size").let { if (it == 0f) 12f else it })
            val xAttr = attr(elem, "x") ?: ""
            val xPositions = xAttr.split(",").mapNotNull { it.trim().toFloatOrNull() }
            val yVal = (attr(elem, "y") ?: "").split(",")
                .firstOrNull()?.trim()?.toFloatOrNull() ?: (fontSize * density)

            // SVG y is baseline. Convert to CSS top using ascent ratio.
            // Inter's ascent ratio is ~0.927. Use 0.8 as a safe default.
            val ascentRatio = 0.84f
            val cssTop = scale(yVal) - fontSize * ascentRatio
            val cssLeft = scale(xPositions.firstOrNull() ?: 0f)

            val fillColor = attr(elem, "fill")
                ?.takeIf { it != "none" }
                ?.let { parseCssColor(it) }
                ?: "#000"

            val fontFamily = attr(elem, "font-family")
                ?.split(",")
                ?.joinToString(",") { it.trim().trim('\'', '"').let { f -> "'$f'" } }
                ?: "'Inter'"
            val fontWeight = attr(elem, "font-weight") ?: "400"
            val fontStyle = attr(elem, "font-style") ?: "normal"

            val style = buildString {
                append("position:absolute;")
                append("left:${fmt(cssLeft)}pt;top:${fmt(cssTop)}pt;")
                append("font-family:$fontFamily;")
                append("font-size:${fmt(fontSize)}pt;")
                append("font-weight:$fontWeight;")
                if (fontStyle != "normal") append("font-style:$fontStyle;")
                append("color:$fillColor;")
                append("white-space:pre;")
                append("line-height:1;")
                appendOpacity(this, elem)
                appendTransform(this, elem)
            }

            sb.appendLine("<span style=\"$style\">${escapeHtml(text)}</span>")
        }

        // ── Image → <img> ──

        private fun renderImage(sb: StringBuilder, elem: Element) {
            val x = scale(attrF(elem, "x"))
            val y = scale(attrF(elem, "y"))
            val w = attrF(elem, "width").let { if (it == 0f) return else scale(it) }
            val h = attrF(elem, "height").let { if (it == 0f) return else scale(it) }

            val href = elem.getAttribute("href").ifEmpty {
                elem.getAttributeNS("http://www.w3.org/1999/xlink", "href")
            } ?: return

            val style = buildString {
                append("position:absolute;")
                append("left:${fmt(x)}pt;top:${fmt(y)}pt;")
                append("width:${fmt(w)}pt;height:${fmt(h)}pt;")
                appendOpacity(this, elem)
                appendTransform(this, elem)
            }

            sb.appendLine("<img style=\"$style\" src=\"${escapeAttr(href)}\" alt=\"\">")
        }

        // ── Path → inline <svg> or <div> ──

        private fun renderPath(sb: StringBuilder, elem: Element) {
            val d = attr(elem, "d")
            if (d.isNullOrEmpty()) return
            renderInlineSvg(sb, elem)
        }

        // ── Group → <div> ──

        private fun renderGroup(sb: StringBuilder, elem: Element) {
            val hasTransform = elem.getAttribute("transform").isNotEmpty()
            val hasOpacity = attr(elem, "opacity") != null
            val clipRect = if (attr(elem, "clip-path") != null) resolveClipRect(elem) else null

            // Track group translate transforms for region suppression
            var dtx = 0f; var dty = 0f
            if (hasTransform && elements.isNotEmpty()) {
                val transform = elem.getAttribute("transform")
                for (match in TRANSFORM_RE.findAll(transform)) {
                    if (match.groupValues[1] == "translate") {
                        val params = match.groupValues[2].split(Regex("[,\\s]+"))
                            .mapNotNull { it.trim().toFloatOrNull() }
                        dtx += params.getOrElse(0) { 0f }
                        dty += params.getOrElse(1) { 0f }
                    }
                }
                accTx += dtx; accTy += dty
            }

            try {
                renderGroupInner(sb, elem, hasTransform, hasOpacity, clipRect)
            } finally {
                if (hasTransform && elements.isNotEmpty()) {
                    accTx -= dtx; accTy -= dty
                }
            }
        }

        private fun renderGroupInner(
            sb: StringBuilder,
            elem: Element,
            hasTransform: Boolean,
            hasOpacity: Boolean,
            clipRect: FloatArray?,
        ) {
            if (!hasTransform && !hasOpacity && clipRect == null) {
                renderChildren(sb, elem)
                return
            }

            if (clipRect != null) {
                // Clip via overflow:hidden (+ border-radius) on a positioned container.
                // An inner offset div at (-x, -y) preserves the absolute coordinate system
                // so children remain positioned relative to the content origin.
                val clipStyle = buildString {
                    append("position:absolute;")
                    append("left:${fmt(clipRect[0])}pt;top:${fmt(clipRect[1])}pt;")
                    append("width:${fmt(clipRect[2])}pt;height:${fmt(clipRect[3])}pt;")
                    append("overflow:hidden;")
                    val rx = clipRect[4]
                    val ry = clipRect[5]
                    if (rx > 0 || ry > 0) {
                        append("border-radius:${fmt(rx)}pt/${fmt(ry)}pt;")
                    }
                    appendOpacity(this, elem)
                    appendTransform(this, elem)
                }
                sb.appendLine("<div style=\"$clipStyle\">")
                sb.appendLine("<div style=\"position:absolute;left:${fmt(-clipRect[0])}pt;top:${fmt(-clipRect[1])}pt;\">")
                renderChildren(sb, elem)
                sb.appendLine("</div>")
                sb.appendLine("</div>")
            } else {
                val style = buildString {
                    append("position:absolute;left:0;top:0;")
                    appendOpacity(this, elem)
                    appendTransform(this, elem)
                }
                sb.appendLine("<div style=\"$style\">")
                renderChildren(sb, elem)
                sb.appendLine("</div>")
            }
        }

        // ── Use → resolve def and render ──

        private fun renderUse(sb: StringBuilder, elem: Element) {
            val href = elem.getAttribute("href").ifEmpty {
                elem.getAttributeNS("http://www.w3.org/1999/xlink", "href")
            } ?: return
            if (href.isEmpty()) return
            val def = defs[href.removePrefix("#")] ?: return

            val x = attrF(elem, "x")
            val y = attrF(elem, "y")
            val hasOffset = x != 0f || y != 0f
            val hasTransform = elem.getAttribute("transform").isNotEmpty()

            if (hasOffset || hasTransform) {
                val style = buildString {
                    append("position:absolute;left:0;top:0;")
                    if (hasOffset) {
                        append("transform:translate(${fmt(scale(x))}pt,${fmt(scale(y))}pt);")
                    }
                    appendTransform(this, elem)
                }
                sb.appendLine("<div style=\"$style\">")
                renderElement(sb, def)
                sb.appendLine("</div>")
            } else {
                renderElement(sb, def)
            }
        }

        // ── Inline SVG fallback ──

        /**
         * Renders an SVG element as an inline <svg> positioned absolutely.
         * Used for paths, lines, polylines, polygons, and shapes with both fill+stroke.
         */
        private fun renderInlineSvg(sb: StringBuilder, elem: Element) {
            // Determine bounding box from the element's attributes
            val bbox = estimateBBox(elem) ?: return
            val (bx, by, bw, bh) = bbox

            var sx = scale(bx)
            var sy = scale(by)
            val sw = scale(bw)
            val sh = scale(bh)

            // Apply the element's transform to the position (since it's stripped from the SVG output).
            // Only simple translate transforms are folded into position; others use CSS transform.
            val transform = elem.getAttribute("transform")
            var cssTransformParts = mutableListOf<String>()
            if (transform.isNotEmpty()) {
                for (match in TRANSFORM_RE.findAll(transform)) {
                    val func = match.groupValues[1]
                    val params = match.groupValues[2]
                        .split(Regex("[,\\s]+"))
                        .mapNotNull { it.trim().toFloatOrNull() }
                    when (func) {
                        "translate" -> {
                            sx += scale(params.getOrElse(0) { 0f })
                            sy += scale(params.getOrElse(1) { 0f })
                        }
                        else -> {
                            // Pass non-translate transforms through as CSS
                            when (func) {
                                "scale" -> cssTransformParts.add("scale(${params.getOrElse(0) { 1f }},${params.getOrElse(1) { params.getOrElse(0) { 1f } }})")
                                "rotate" -> cssTransformParts.add("rotate(${params.getOrElse(0) { 0f }}deg)")
                                "matrix" -> if (params.size >= 6) {
                                    val me = scale(params[4]) * 96f / 72f
                                    val mf = scale(params[5]) * 96f / 72f
                                    cssTransformParts.add("matrix(${params[0]},${params[1]},${params[2]},${params[3]},${fmt(me)},${fmt(mf)})")
                                }
                                "skewX" -> cssTransformParts.add("skewX(${params.getOrElse(0) { 0f }}deg)")
                                "skewY" -> cssTransformParts.add("skewY(${params.getOrElse(0) { 0f }}deg)")
                            }
                        }
                    }
                }
            }

            // Build inline SVG with viewBox matching the element's coordinate space
            val cssTransform = if (cssTransformParts.isNotEmpty()) "transform:${cssTransformParts.joinToString(" ")};" else ""
            sb.append("<svg style=\"position:absolute;left:${fmt(sx)}pt;top:${fmt(sy)}pt;width:${fmt(sw)}pt;height:${fmt(sh)}pt;overflow:visible;$cssTransform\"")
            sb.append(" viewBox=\"${fmt(bx)} ${fmt(by)} ${fmt(bw)} ${fmt(bh)}\"")
            sb.append(" xmlns=\"http://www.w3.org/2000/svg\">")
            sb.append(elementToSvgString(elem))
            sb.appendLine("</svg>")
        }

        private fun estimateBBox(elem: Element): FloatArray? {
            return when (elem.localName) {
                "rect" -> {
                    val x = attrF(elem, "x")
                    val y = attrF(elem, "y")
                    val w = attrF(elem, "width")
                    val h = attrF(elem, "height")
                    if (w == 0f || h == 0f) null
                    else {
                        val sw = attrF(elem, "stroke-width") / 2f
                        floatArrayOf(x - sw, y - sw, w + sw * 2, h + sw * 2)
                    }
                }
                "circle" -> {
                    val cx = attrF(elem, "cx")
                    val cy = attrF(elem, "cy")
                    val r = attrF(elem, "r")
                    if (r == 0f) null
                    else {
                        val sw = attrF(elem, "stroke-width") / 2f
                        floatArrayOf(cx - r - sw, cy - r - sw, (r + sw) * 2, (r + sw) * 2)
                    }
                }
                "ellipse" -> {
                    val cx = attrF(elem, "cx")
                    val cy = attrF(elem, "cy")
                    val rx = attrF(elem, "rx")
                    val ry = attrF(elem, "ry")
                    if (rx == 0f || ry == 0f) null
                    else {
                        val sw = attrF(elem, "stroke-width") / 2f
                        floatArrayOf(cx - rx - sw, cy - ry - sw, (rx + sw) * 2, (ry + sw) * 2)
                    }
                }
                "line" -> {
                    val x1 = attrF(elem, "x1")
                    val y1 = attrF(elem, "y1")
                    val x2 = attrF(elem, "x2")
                    val y2 = attrF(elem, "y2")
                    val sw = attrF(elem, "stroke-width").let { if (it == 0f) 1f else it } / 2f
                    val minX = minOf(x1, x2) - sw
                    val minY = minOf(y1, y2) - sw
                    val maxX = maxOf(x1, x2) + sw
                    val maxY = maxOf(y1, y2) + sw
                    floatArrayOf(minX, minY, maxX - minX, maxY - minY)
                }
                "path" -> {
                    // Estimate bounding box from path data
                    val d = attr(elem, "d") ?: return null
                    estimatePathBBox(d, attrF(elem, "stroke-width"))
                }
                "polyline", "polygon" -> {
                    val points = attr(elem, "points") ?: return null
                    val coords = points.trim().split(Regex("[,\\s]+")).mapNotNull { it.toFloatOrNull() }
                    if (coords.size < 4) return null
                    var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
                    var maxX = Float.MIN_VALUE; var maxY = Float.MIN_VALUE
                    for (i in coords.indices step 2) {
                        val px = coords[i]; val py = coords.getOrElse(i + 1) { 0f }
                        if (px < minX) minX = px; if (py < minY) minY = py
                        if (px > maxX) maxX = px; if (py > maxY) maxY = py
                    }
                    val sw = attrF(elem, "stroke-width") / 2f
                    floatArrayOf(minX - sw, minY - sw, maxX - minX + sw * 2, maxY - minY + sw * 2)
                }
                else -> null
            }
        }

        /**
         * Estimates path bounding box from the path data string.
         * Tracks current point through M/L/H/V/C/S/Q/T/A/Z commands.
         */
        private fun estimatePathBBox(d: String, strokeWidth: Float): FloatArray? {
            val tokens = PATH_TOKEN_RE.findAll(d).map { it.value }.toList()
            if (tokens.isEmpty()) return null

            var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE; var maxY = Float.MIN_VALUE
            var cx = 0f; var cy = 0f
            var i = 0
            var lastCmd = ' '

            fun track(x: Float, y: Float) {
                if (x < minX) minX = x; if (y < minY) minY = y
                if (x > maxX) maxX = x; if (y > maxY) maxY = y
            }
            fun nf(): Float = if (i < tokens.size) tokens[i++].toFloat() else 0f

            while (i < tokens.size) {
                val tok = tokens[i]
                val cmd: Char
                if (tok.length == 1 && tok[0] in PATH_COMMANDS) {
                    cmd = tok[0]; i++
                } else {
                    cmd = when (lastCmd) {
                        'M' -> 'L'; 'm' -> 'l'
                        'Z', 'z', ' ' -> break
                        else -> lastCmd
                    }
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
                    'c' -> {
                        track(cx + nf(), cy + nf()); track(cx + nf(), cy + nf())
                        cx += nf(); cy += nf(); track(cx, cy)
                    }
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
            val sw = strokeWidth / 2f
            return floatArrayOf(minX - sw, minY - sw, maxX - minX + sw * 2, maxY - minY + sw * 2)
        }

        /**
         * Serializes an SVG element back to an SVG string for inline embedding.
         */
        private fun elementToSvgString(elem: Element): String = buildString {
            append("<${elem.localName}")
            val attrs = elem.attributes
            for (i in 0 until attrs.length) {
                val a = attrs.item(i)
                // Skip transform — the parent inline SVG handles positioning
                if (a.localName == "transform") continue
                append(" ${a.localName}=\"${escapeAttr(a.nodeValue)}\"")
            }
            val children = elem.childNodes
            val hasChildren = (0 until children.length).any { children.item(it).nodeType == Node.ELEMENT_NODE }
            val textContent = elem.textContent?.trim() ?: ""

            if (!hasChildren && textContent.isEmpty()) {
                append("/>")
            } else {
                append(">")
                if (textContent.isNotEmpty() && !hasChildren) {
                    append(escapeHtml(textContent))
                }
                for (i in 0 until children.length) {
                    val child = children.item(i)
                    if (child.nodeType == Node.ELEMENT_NODE) {
                        append(elementToSvgString(child as Element))
                    }
                }
                append("</${elem.localName}>")
            }
        }

        // ── CSS helpers ──

        private fun appendOpacity(sb: StringBuilder, elem: Element) {
            val opacity = attr(elem, "opacity")?.toFloatOrNull()
            val fillOp = attr(elem, "fill-opacity")?.toFloatOrNull()
            val strokeOp = attr(elem, "stroke-opacity")?.toFloatOrNull()
            val effective = (opacity ?: 1f) * (fillOp ?: 1f)
            // For HTML elements we combine fill-opacity with opacity
            // (stroke-opacity only matters for SVG stroke which we handle inline)
            if (effective < 1f || (strokeOp != null && strokeOp < 1f)) {
                val minOpacity = if (strokeOp != null) minOf(effective, (opacity ?: 1f) * strokeOp) else effective
                sb.append("opacity:${fmt(minOpacity)};")
            }
        }

        private fun appendTransform(sb: StringBuilder, elem: Element) {
            val transform = elem.getAttribute("transform")
            if (transform.isEmpty()) return

            val cssParts = mutableListOf<String>()
            for (match in TRANSFORM_RE.findAll(transform)) {
                val func = match.groupValues[1]
                val params = match.groupValues[2]
                    .split(Regex("[,\\s]+"))
                    .mapNotNull { it.trim().toFloatOrNull() }

                when (func) {
                    "translate" -> {
                        val tx = scale(params.getOrElse(0) { 0f })
                        val ty = scale(params.getOrElse(1) { 0f })
                        cssParts.add("translate(${fmt(tx)}pt,${fmt(ty)}pt)")
                    }
                    "scale" -> {
                        val sx = params.getOrElse(0) { 1f }
                        val sy = params.getOrElse(1) { sx }
                        cssParts.add("scale($sx,$sy)")
                    }
                    "rotate" -> {
                        val angle = params.getOrElse(0) { 0f }
                        if (params.size >= 3) {
                            val cx = scale(params[1])
                            val cy = scale(params[2])
                            cssParts.add("translate(${fmt(cx)}pt,${fmt(cy)}pt)")
                            cssParts.add("rotate(${angle}deg)")
                            cssParts.add("translate(${fmt(-cx)}pt,${fmt(-cy)}pt)")
                        } else {
                            cssParts.add("rotate(${angle}deg)")
                        }
                    }
                    "matrix" -> {
                        if (params.size >= 6) {
                            // SVG matrix(a,b,c,d,e,f) maps to CSS matrix(a,b,c,d,e',f')
                            // CSS matrix() tx/ty are always in CSS px (unitless), but our
                            // scaled values are in pt space. Convert pt→px: × 96/72.
                            val a = params[0]; val b = params[1]
                            val c = params[2]; val d = params[3]
                            val e = scale(params[4]) * 96f / 72f
                            val f = scale(params[5]) * 96f / 72f
                            cssParts.add("matrix($a,$b,$c,$d,${fmt(e)},${fmt(f)})")
                        }
                    }
                    "skewX" -> cssParts.add("skewX(${params.getOrElse(0) { 0f }}deg)")
                    "skewY" -> cssParts.add("skewY(${params.getOrElse(0) { 0f }}deg)")
                }
            }
            if (cssParts.isNotEmpty()) {
                sb.append("transform:${cssParts.joinToString(" ")};")
            }
        }

        /**
         * Resolves a clip-path reference to a scaled rect: [x, y, w, h, rx, ry].
         * Returns null if the clip cannot be resolved to a simple rect.
         */
        private fun resolveClipRect(elem: Element): FloatArray? {
            val clipRef = attr(elem, "clip-path") ?: return null
            val clipId = Regex("""url\(#([^)]+)\)""").find(clipRef)?.groupValues?.get(1) ?: return null
            val clipElem = defs[clipId] ?: return null

            for (i in 0 until clipElem.childNodes.length) {
                val node = clipElem.childNodes.item(i)
                if (node.nodeType != Node.ELEMENT_NODE) continue
                val child = node as Element
                if (child.localName == "rect") {
                    val x = scale(child.getAttribute("x").toFloatOrNull() ?: 0f)
                    val y = scale(child.getAttribute("y").toFloatOrNull() ?: 0f)
                    val w = child.getAttribute("width").toFloatOrNull()?.let { scale(it) } ?: continue
                    val h = child.getAttribute("height").toFloatOrNull()?.let { scale(it) } ?: continue
                    val rx = scale(child.getAttribute("rx").toFloatOrNull() ?: 0f)
                    val ry = scale(child.getAttribute("ry").toFloatOrNull() ?: rx)
                    return floatArrayOf(x, y, w, h, rx, ry)
                }
            }
            return null
        }

        // ── Scaling & formatting ──

        private fun scale(svgPx: Float): Float = svgPx / density

        private fun attrF(elem: Element, name: String): Float {
            return attr(elem, name)?.toFloatOrNull() ?: 0f
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

        private fun parseCssColor(color: String): String? {
            val c = color.trim()
            return when {
                c.startsWith("#") -> c // Pass through hex colors
                c.startsWith("rgba(") || c.startsWith("rgb(") -> c // Pass through rgb() functions
                c.lowercase() in CSS_NAMED_COLORS -> CSS_NAMED_COLORS[c.lowercase()]
                else -> null
            }
        }

        companion object {
            private val TRANSFORM_RE =
                Regex("""(translate|scale|rotate|matrix|skewX|skewY)\(([^)]+)\)""")
            private val PATH_TOKEN_RE =
                Regex("""[MmLlHhVvCcSsQqTtAaZz]|[+-]?(?:\d+\.?\d*|\.\d+)""")
            private const val PATH_COMMANDS = "MmLlHhVvCcSsQqTtAaZz"

            private val CSS_NAMED_COLORS = mapOf(
                "black" to "#000000",
                "white" to "#ffffff",
                "red" to "#ff0000",
                "green" to "#008000",
                "blue" to "#0000ff",
                "yellow" to "#ffff00",
                "cyan" to "#00ffff",
                "aqua" to "#00ffff",
                "magenta" to "#ff00ff",
                "fuchsia" to "#ff00ff",
                "gray" to "#808080",
                "grey" to "#808080",
                "silver" to "#c0c0c0",
                "maroon" to "#800000",
                "olive" to "#808000",
                "lime" to "#00ff00",
                "teal" to "#008080",
                "navy" to "#000080",
                "orange" to "#ffa500",
                "purple" to "#800080",
                "pink" to "#ffc0cb",
                "brown" to "#a52a2a",
            )
        }
    }

    // ── Shared utilities ──

    /** Format a float, trimming unnecessary trailing zeros. */
    private fun fmt(v: Float): String {
        if (v == 0f) return "0"
        val s = "%.2f".format(v)
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

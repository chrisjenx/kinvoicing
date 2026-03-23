package com.chrisjenx.composepdf.internal

import com.chrisjenx.composepdf.HoverStyles
import com.chrisjenx.composepdf.PdfButtonAnnotation
import com.chrisjenx.composepdf.PdfCheckboxAnnotation
import com.chrisjenx.composepdf.PdfElementAnnotation
import com.chrisjenx.composepdf.PdfHoverAnnotation
import com.chrisjenx.composepdf.PdfImageAnnotation
import com.chrisjenx.composepdf.PdfLinkAnnotation
import com.chrisjenx.composepdf.PdfListAnnotation
import com.chrisjenx.composepdf.PdfPageConfig
import com.chrisjenx.composepdf.PdfRadioButtonAnnotation
import com.chrisjenx.composepdf.PdfSelectAnnotation
import com.chrisjenx.composepdf.PdfSliderAnnotation
import com.chrisjenx.composepdf.PdfTableAnnotation
import com.chrisjenx.composepdf.PdfTextFieldAnnotation

/**
 * Converts Skia-generated SVG to semantic HTML5 with CSS flexbox layout.
 *
 * Replaces [SvgToHtmlConverter]. Instead of mapping each SVG element to an
 * absolute-positioned `<div>`, this converter:
 * 1. Parses SVG into flat positioned elements via [SvgParser]
 * 2. Reconstructs layout structure via [LayoutAnalyzer] (spatial clustering)
 * 3. Emits semantic HTML with CSS flexbox via [CssEmitter]
 *
 * The result is clean, semantic HTML suitable for emails, invoices, and static pages.
 */
internal object SvgToSemanticHtmlConverter {

    fun convert(
        svgPages: List<String>,
        config: PdfPageConfig,
        density: Float,
        linksByPage: List<List<PdfLinkAnnotation>>,
        elementsByPage: List<List<PdfElementAnnotation>> = emptyList(),
        fontBase64: Map<String, String>,
    ): String {
        val contentWidthPt = config.contentWidth.value
        val htmlPages = mutableListOf<HtmlNode>()

        for ((pageIndex, svg) in svgPages.withIndex()) {
            val links = linksByPage.getOrElse(pageIndex) { emptyList() }
            val elements = elementsByPage.getOrElse(pageIndex) { emptyList() }

            // Step 1: Parse SVG into flat positioned elements
            val allNodes = SvgParser.parse(svg, density)

            // Step 2: Filter out SVG elements within REPLACEMENT annotation regions.
            // Tables and lists replace the visual content entirely.
            // Buttons, text fields, images, and hover are overlays — don't suppress.
            val replacementElements = elements.filter {
                it is PdfTableAnnotation || it is PdfListAnnotation ||
                    it is PdfCheckboxAnnotation || it is PdfRadioButtonAnnotation ||
                    it is PdfSelectAnnotation || it is PdfSliderAnnotation ||
                    it is PdfTextFieldAnnotation
            }
            val svgNodes = if (replacementElements.isNotEmpty()) {
                allNodes.filter { node -> !isInsideAnyElement(node, replacementElements) }
            } else allNodes

            // Step 3: Build layout tree
            val tree = LayoutAnalyzer.buildTree(svgNodes, contentWidthPt)

            // Step 3: Add PdfElement annotations (tables, buttons, fields, etc.)
            addElements(tree, elements)

            // Step 4: Add link overlays
            addLinks(tree, links)

            htmlPages.add(tree)
        }

        // Step 5: Collect hover CSS
        val hoverCss = buildHoverCss(elementsByPage)

        // Step 6: Render to HTML document
        return CssEmitter.render(htmlPages, config, fontBase64, hoverCss)
    }

    private const val REGION_TOLERANCE = 2f

    private fun isInsideAnyElement(node: SvgNode, elements: List<PdfElementAnnotation>): Boolean {
        // Use center-point check: if the node's center falls within the annotation bounds,
        // suppress it. This is more robust than requiring full containment, since SVG text
        // nodes can extend slightly outside Compose layout bounds (font descenders, etc.).
        val cx = node.x + node.width / 2
        val cy = node.y + node.height / 2
        for (ann in elements) {
            if (cx >= ann.x - REGION_TOLERANCE &&
                cy >= ann.y - REGION_TOLERANCE &&
                cx <= ann.x + ann.width + REGION_TOLERANCE &&
                cy <= ann.y + ann.height + REGION_TOLERANCE
            ) {
                return true
            }
        }
        return false
    }

    private fun addElements(tree: HtmlNode, elements: List<PdfElementAnnotation>) {
        var hoverIndex = 0
        for (elem in elements) {
            when (elem) {
                is PdfTableAnnotation -> tree.children.add(buildTable(elem))
                is PdfListAnnotation -> tree.children.add(buildList(elem))
                is PdfButtonAnnotation -> tree.children.add(buildButton(elem))
                is PdfTextFieldAnnotation -> tree.children.add(buildTextField(elem))
                is PdfImageAnnotation -> tree.children.add(buildImage(elem))
                is PdfHoverAnnotation -> tree.children.add(buildHover(elem, hoverIndex++))
                is PdfCheckboxAnnotation -> tree.children.add(buildCheckbox(elem))
                is PdfRadioButtonAnnotation -> tree.children.add(buildRadioButton(elem))
                is PdfSelectAnnotation -> tree.children.add(buildSelect(elem))
                is PdfSliderAnnotation -> tree.children.add(buildSlider(elem))
            }
        }
    }

    private fun buildTable(table: PdfTableAnnotation): HtmlNode {
        val tableNode = HtmlNode(
            tag = "table",
            css = mutableMapOf(
                "position" to "absolute",
                "left" to "${fmt(table.x)}pt",
                "top" to "${fmt(table.y)}pt",
                "width" to "${fmt(table.width)}pt",
                "border-collapse" to "collapse",
            ),
        )
        if (table.caption != null) {
            tableNode.children.add(HtmlNode(tag = "caption", textContent = table.caption))
        }
        var inThead = false
        var inTbody = false
        var currentSection: HtmlNode? = null

        for (row in table.rows) {
            if (row.isHeader && !inThead) {
                if (inTbody) { currentSection = null; inTbody = false }
                currentSection = HtmlNode(tag = "thead")
                tableNode.children.add(currentSection)
                inThead = true
            } else if (!row.isHeader && !inTbody) {
                if (inThead) { currentSection = null; inThead = false }
                currentSection = HtmlNode(tag = "tbody")
                tableNode.children.add(currentSection)
                inTbody = true
            }

            val tr = HtmlNode(tag = "tr")
            val cellTag = if (row.isHeader) "th" else "td"
            for (cell in row.cells) {
                val td = HtmlNode(
                    tag = cellTag,
                    textContent = cell.text,
                    attributes = mutableMapOf<String, String>().apply {
                        if (cell.colSpan > 1) put("colspan", cell.colSpan.toString())
                        if (cell.rowSpan > 1) put("rowspan", cell.rowSpan.toString())
                    },
                    css = mutableMapOf<String, String>().apply {
                        if (cell.width > 0) put("width", "${fmt(cell.width)}pt")
                    },
                )
                tr.children.add(td)
            }
            (currentSection ?: tableNode).children.add(tr)
        }
        return tableNode
    }

    private fun buildList(list: PdfListAnnotation): HtmlNode {
        val tag = if (list.ordered) "ol" else "ul"
        val listNode = HtmlNode(
            tag = tag,
            css = mutableMapOf(
                "position" to "absolute",
                "left" to "${fmt(list.x)}pt",
                "top" to "${fmt(list.y)}pt",
                "width" to "${fmt(list.width)}pt",
            ),
        )
        for (item in list.items) {
            listNode.children.add(HtmlNode(tag = "li", textContent = item.text))
        }
        return listNode
    }

    private fun buildButton(button: PdfButtonAnnotation): HtmlNode {
        return HtmlNode(
            tag = "button",
            textContent = button.label,
            css = mutableMapOf(
                "position" to "absolute",
                "left" to "${fmt(button.x)}pt",
                "top" to "${fmt(button.y)}pt",
                "width" to "${fmt(button.width)}pt",
                "height" to "${fmt(button.height)}pt",
                "cursor" to "pointer",
                // Transparent overlay — the SVG visual provides the appearance
                "background" to "transparent",
                "border" to "none",
                "color" to "transparent",
            ),
            attributes = mutableMapOf<String, String>().apply {
                put("name", button.name)
                if (button.onClick != null) put("onclick", button.onClick)
            },
        )
    }

    private fun buildTextField(field: PdfTextFieldAnnotation): HtmlNode {
        val commonCss = mutableMapOf(
            "position" to "absolute",
            "left" to "${fmt(field.x)}pt",
            "top" to "${fmt(field.y)}pt",
            "width" to "${fmt(field.width)}pt",
            "height" to "${fmt(field.height)}pt",
            "box-sizing" to "border-box",
            "padding" to "4pt 8pt",
            "border" to "1pt solid #BDBDBD",
            "border-radius" to "4pt",
            "font-size" to "12pt",
            "font-family" to "inherit",
        )
        return if (field.multiline) {
            HtmlNode(
                tag = "textarea",
                textContent = field.value,
                css = commonCss,
                attributes = mutableMapOf<String, String>().apply {
                    put("name", field.name)
                    put("placeholder", field.placeholder)
                    if (field.maxLength > 0) put("maxlength", field.maxLength.toString())
                },
            )
        } else {
            HtmlNode(
                tag = "input",
                selfClosing = true,
                css = commonCss,
                attributes = mutableMapOf<String, String>().apply {
                    put("type", "text")
                    put("name", field.name)
                    put("value", field.value)
                    put("placeholder", field.placeholder)
                    if (field.maxLength > 0) put("maxlength", field.maxLength.toString())
                },
            )
        }
    }

    private fun buildImage(image: PdfImageAnnotation): HtmlNode {
        return HtmlNode(
            tag = "div",
            css = mutableMapOf(
                "position" to "absolute",
                "left" to "${fmt(image.x)}pt",
                "top" to "${fmt(image.y)}pt",
                "width" to "${fmt(image.width)}pt",
                "height" to "${fmt(image.height)}pt",
            ),
            attributes = mutableMapOf(
                "role" to "img",
                "aria-label" to image.altText,
            ),
        )
    }

    private fun buildHover(hover: PdfHoverAnnotation, index: Int): HtmlNode {
        return HtmlNode(
            tag = "div",
            css = mutableMapOf(
                "position" to "absolute",
                "left" to "${fmt(hover.x)}pt",
                "top" to "${fmt(hover.y)}pt",
                "width" to "${fmt(hover.width)}pt",
                "height" to "${fmt(hover.height)}pt",
            ),
            attributes = mutableMapOf("class" to "pdf-hover-$index"),
        )
    }

    private fun buildHoverCss(elementsByPage: List<List<PdfElementAnnotation>>): String {
        val sb = StringBuilder()
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
                    for ((k, v) in styles.customCss) sb.append(" $k:$v;")
                    sb.appendLine(" }")
                    hoverIndex++
                }
            }
        }
        return sb.toString()
    }

    private fun buildCheckbox(checkbox: PdfCheckboxAnnotation): HtmlNode {
        val label = HtmlNode(
            tag = "label",
            css = mutableMapOf(
                "position" to "absolute",
                "left" to "${fmt(checkbox.x)}pt",
                "top" to "${fmt(checkbox.y)}pt",
                "width" to "${fmt(checkbox.width)}pt",
                "height" to "${fmt(checkbox.height)}pt",
                "display" to "flex",
                "align-items" to "center",
                "gap" to "4pt",
                "cursor" to "pointer",
            ),
        )
        val input = HtmlNode(
            tag = "input",
            selfClosing = true,
            css = mutableMapOf(
                "width" to "14pt",
                "height" to "14pt",
                "accent-color" to "#1565C0",
            ),
            attributes = mutableMapOf<String, String>().apply {
                put("type", "checkbox")
                put("name", checkbox.name)
                if (checkbox.checked) put("checked", "checked")
            },
        )
        label.children.add(input)
        if (checkbox.label.isNotEmpty()) {
            label.children.add(HtmlNode(tag = "span", textContent = checkbox.label))
        }
        return label
    }

    private fun buildRadioButton(radio: PdfRadioButtonAnnotation): HtmlNode {
        val label = HtmlNode(
            tag = "label",
            css = mutableMapOf(
                "position" to "absolute",
                "left" to "${fmt(radio.x)}pt",
                "top" to "${fmt(radio.y)}pt",
                "width" to "${fmt(radio.width)}pt",
                "height" to "${fmt(radio.height)}pt",
                "display" to "flex",
                "align-items" to "center",
                "gap" to "4pt",
                "cursor" to "pointer",
            ),
        )
        val input = HtmlNode(
            tag = "input",
            selfClosing = true,
            css = mutableMapOf(
                "width" to "14pt",
                "height" to "14pt",
                "accent-color" to "#1565C0",
            ),
            attributes = mutableMapOf<String, String>().apply {
                put("type", "radio")
                put("name", radio.groupName)
                put("value", radio.value)
                if (radio.selected) put("checked", "checked")
            },
        )
        label.children.add(input)
        if (radio.label.isNotEmpty()) {
            label.children.add(HtmlNode(tag = "span", textContent = radio.label))
        }
        return label
    }

    private fun buildSelect(select: PdfSelectAnnotation): HtmlNode {
        val selectNode = HtmlNode(
            tag = "select",
            css = mutableMapOf(
                "position" to "absolute",
                "left" to "${fmt(select.x)}pt",
                "top" to "${fmt(select.y)}pt",
                "width" to "${fmt(select.width)}pt",
                "height" to "${fmt(select.height)}pt",
            ),
            attributes = mutableMapOf("name" to select.name),
        )
        for (option in select.options) {
            val optionNode = HtmlNode(
                tag = "option",
                textContent = option.label,
                attributes = mutableMapOf<String, String>().apply {
                    put("value", option.value)
                    if (option.value == select.selectedValue) put("selected", "selected")
                },
            )
            selectNode.children.add(optionNode)
        }
        return selectNode
    }

    private fun buildSlider(slider: PdfSliderAnnotation): HtmlNode {
        return HtmlNode(
            tag = "input",
            selfClosing = true,
            css = mutableMapOf(
                "position" to "absolute",
                "left" to "${fmt(slider.x)}pt",
                "top" to "${fmt(slider.y)}pt",
                "width" to "${fmt(slider.width)}pt",
                "height" to "${fmt(slider.height)}pt",
            ),
            attributes = mutableMapOf(
                "type" to "range",
                "name" to slider.name,
                "min" to fmt(slider.min),
                "max" to fmt(slider.max),
                "value" to fmt(slider.value),
                "step" to fmt(slider.step),
            ),
        )
    }

    private fun addLinks(tree: HtmlNode, links: List<PdfLinkAnnotation>) {
        for (link in links) {
            tree.children.add(
                HtmlNode(
                    tag = "a",
                    css = mutableMapOf(
                        "position" to "absolute",
                        "left" to "${fmt(link.x)}pt",
                        "top" to "${fmt(link.y)}pt",
                        "width" to "${fmt(link.width)}pt",
                        "height" to "${fmt(link.height)}pt",
                        "display" to "block",
                    ),
                    attributes = mutableMapOf(
                        "href" to link.href,
                        "target" to "_blank",
                    ),
                )
            )
        }
    }

    private fun fmt(v: Float): String {
        if (v == 0f) return "0"
        val s = "%.4f".format(v)
        return s.trimEnd('0').trimEnd('.')
    }
}

package com.chrisjenx.composepdf.internal

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.awt.geom.AffineTransform
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Converts Skia-generated SVG to vector PDF using PDFBox.
 *
 * Handles the limited subset of SVG elements produced by Skia's SVGCanvas:
 * <rect>, <text>, <path>, <g>, transforms, fills.
 */
internal object SvgToPdfConverter {

    fun addPage(pdfDoc: PDDocument, svg: String, pageWidthPt: Float, pageHeightPt: Float) {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val xmlDoc = factory.newDocumentBuilder().parse(svg.byteInputStream())
        val svgRoot = xmlDoc.documentElement

        val svgWidth = svgRoot.getAttribute("width").toFloatOrNull() ?: pageWidthPt
        val svgHeight = svgRoot.getAttribute("height").toFloatOrNull() ?: pageHeightPt

        // Scale factor from SVG units to PDF points
        val scaleX = pageWidthPt / svgWidth
        val scaleY = pageHeightPt / svgHeight

        val mediaBox = PDRectangle(pageWidthPt, pageHeightPt)
        val page = PDPage(mediaBox)
        pdfDoc.addPage(page)

        val cs = PDPageContentStream(pdfDoc, page)
        try {
            // PDF origin is bottom-left, SVG is top-left.
            // Transform: flip Y axis and scale from SVG units to PDF points.
            cs.transform(
                org.apache.pdfbox.util.Matrix(
                    scaleX, 0f, 0f, -scaleY, 0f, pageHeightPt
                )
            )

            renderChildren(svgRoot, cs, pdfDoc)
        } finally {
            cs.close()
        }
    }

    private fun renderChildren(parent: Element, cs: PDPageContentStream, doc: PDDocument) {
        val children = parent.childNodes
        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node.nodeType != Node.ELEMENT_NODE) continue
            val elem = node as Element
            when (elem.localName) {
                "rect" -> renderRect(elem, cs)
                "text" -> renderText(elem, cs, doc)
                "path" -> renderPath(elem, cs)
                "g" -> renderGroup(elem, cs, doc)
            }
        }
    }

    private fun renderRect(elem: Element, cs: PDPageContentStream) {
        cs.saveGraphicsState()
        applyTransform(elem, cs)
        applyFill(elem, cs)

        val x = elem.getAttribute("x").toFloatOrNull() ?: 0f
        val y = elem.getAttribute("y").toFloatOrNull() ?: 0f
        val w = elem.getAttribute("width").toFloatOrNull() ?: return
        val h = elem.getAttribute("height").toFloatOrNull() ?: return

        cs.addRect(x, y, w, h)
        cs.fill()
        cs.restoreGraphicsState()
    }

    private fun renderText(elem: Element, cs: PDPageContentStream, doc: PDDocument) {
        cs.saveGraphicsState()
        applyTransform(elem, cs)

        val fontSize = elem.getAttribute("font-size").toFloatOrNull() ?: 12f
        val text = elem.textContent.trim()
        if (text.isEmpty()) {
            cs.restoreGraphicsState()
            return
        }

        applyFill(elem, cs)

        // Use Helvetica as a reasonable default for system fonts
        val font = PDType1Font(Standard14Fonts.FontName.HELVETICA)

        // Get individual glyph positions if available
        val xPositions = elem.getAttribute("x")
            .split(",")
            .mapNotNull { it.trim().toFloatOrNull() }
        val yOffset = elem.getAttribute("y")
            .split(",")
            .firstOrNull()
            ?.trim()?.toFloatOrNull() ?: fontSize

        cs.beginText()
        cs.setFont(font, fontSize)

        if (xPositions.size > 1 && xPositions.size >= text.length) {
            // Position each glyph individually for precise placement
            for (i in text.indices) {
                cs.newLineAtOffset(
                    if (i == 0) xPositions[0] else xPositions[i] - xPositions[i - 1],
                    if (i == 0) yOffset else 0f,
                )
                cs.showText(text[i].toString())
            }
        } else {
            // Simple text positioning
            val x0 = xPositions.firstOrNull() ?: 0f
            cs.newLineAtOffset(x0, yOffset)
            cs.showText(text)
        }
        cs.endText()
        cs.restoreGraphicsState()
    }

    private fun renderPath(elem: Element, cs: PDPageContentStream) {
        cs.saveGraphicsState()
        applyTransform(elem, cs)
        applyFill(elem, cs)

        val d = elem.getAttribute("d")
        if (d.isNotEmpty()) {
            parseSvgPath(d, cs)
            val fill = elem.getAttribute("fill")
            val stroke = elem.getAttribute("stroke")
            when {
                fill.isNotEmpty() && stroke.isNotEmpty() -> cs.fillAndStroke()
                stroke.isNotEmpty() -> cs.stroke()
                else -> cs.fill()
            }
        }
        cs.restoreGraphicsState()
    }

    private fun renderGroup(elem: Element, cs: PDPageContentStream, doc: PDDocument) {
        cs.saveGraphicsState()
        applyTransform(elem, cs)
        renderChildren(elem, cs, doc)
        cs.restoreGraphicsState()
    }

    private fun applyTransform(elem: Element, cs: PDPageContentStream) {
        val transform = elem.getAttribute("transform")
        if (transform.isEmpty()) return

        // Parse translate(x y) and translate(x, y)
        val translateMatch = Regex("""translate\(\s*([+-]?[\d.]+)[,\s]+([+-]?[\d.]+)\s*\)""")
            .find(transform)
        if (translateMatch != null) {
            val tx = translateMatch.groupValues[1].toFloat()
            val ty = translateMatch.groupValues[2].toFloat()
            cs.transform(org.apache.pdfbox.util.Matrix(AffineTransform.getTranslateInstance(tx.toDouble(), ty.toDouble())))
        }

        // Parse scale(sx sy) and scale(s)
        val scaleMatch = Regex("""scale\(\s*([+-]?[\d.]+)(?:[,\s]+([+-]?[\d.]+))?\s*\)""")
            .find(transform)
        if (scaleMatch != null) {
            val sx = scaleMatch.groupValues[1].toFloat()
            val sy = scaleMatch.groupValues[2].toFloatOrNull() ?: sx
            cs.transform(org.apache.pdfbox.util.Matrix(AffineTransform.getScaleInstance(sx.toDouble(), sy.toDouble())))
        }

        // Parse matrix(a b c d e f)
        val matrixMatch = Regex("""matrix\(\s*([+-]?[\d.]+)[,\s]+([+-]?[\d.]+)[,\s]+([+-]?[\d.]+)[,\s]+([+-]?[\d.]+)[,\s]+([+-]?[\d.]+)[,\s]+([+-]?[\d.]+)\s*\)""")
            .find(transform)
        if (matrixMatch != null) {
            val values = matrixMatch.groupValues.drop(1).map { it.toFloat() }
            cs.transform(org.apache.pdfbox.util.Matrix(
                values[0], values[1], values[2], values[3], values[4], values[5]
            ))
        }
    }

    private fun applyFill(elem: Element, cs: PDPageContentStream) {
        val fill = elem.getAttribute("fill").ifEmpty { return }
        val (r, g, b) = parseCssColor(fill) ?: return
        cs.setNonStrokingColor(r, g, b)
    }

    private fun parseCssColor(color: String): Triple<Float, Float, Float>? {
        return when {
            color.startsWith("#") && color.length == 7 -> {
                val r = color.substring(1, 3).toInt(16) / 255f
                val g = color.substring(3, 5).toInt(16) / 255f
                val b = color.substring(5, 7).toInt(16) / 255f
                Triple(r, g, b)
            }
            color.startsWith("#") && color.length == 4 -> {
                val r = color.substring(1, 2).repeat(2).toInt(16) / 255f
                val g = color.substring(2, 3).repeat(2).toInt(16) / 255f
                val b = color.substring(3, 4).repeat(2).toInt(16) / 255f
                Triple(r, g, b)
            }
            color == "white" -> Triple(1f, 1f, 1f)
            color == "black" -> Triple(0f, 0f, 0f)
            color == "red" -> Triple(1f, 0f, 0f)
            color == "green" -> Triple(0f, 0.502f, 0f)
            color == "blue" -> Triple(0f, 0f, 1f)
            color.startsWith("rgb(") -> {
                val values = color.removePrefix("rgb(").removeSuffix(")")
                    .split(",").map { it.trim().toFloat() / 255f }
                if (values.size == 3) Triple(values[0], values[1], values[2]) else null
            }
            else -> null
        }
    }

    private fun parseSvgPath(d: String, cs: PDPageContentStream) {
        val tokens = Regex("""[MmLlHhVvCcSsQqTtAaZz]|[+-]?[\d.]+""")
            .findAll(d).map { it.value }.toList()

        var i = 0
        var cx = 0f
        var cy = 0f

        fun nextFloat(): Float = tokens[i++].toFloat()

        while (i < tokens.size) {
            when (tokens[i++]) {
                "M" -> { cx = nextFloat(); cy = nextFloat(); cs.moveTo(cx, cy) }
                "m" -> { cx += nextFloat(); cy += nextFloat(); cs.moveTo(cx, cy) }
                "L" -> { cx = nextFloat(); cy = nextFloat(); cs.lineTo(cx, cy) }
                "l" -> { cx += nextFloat(); cy += nextFloat(); cs.lineTo(cx, cy) }
                "H" -> { cx = nextFloat(); cs.lineTo(cx, cy) }
                "h" -> { cx += nextFloat(); cs.lineTo(cx, cy) }
                "V" -> { cy = nextFloat(); cs.lineTo(cx, cy) }
                "v" -> { cy += nextFloat(); cs.lineTo(cx, cy) }
                "C" -> {
                    val x1 = nextFloat(); val y1 = nextFloat()
                    val x2 = nextFloat(); val y2 = nextFloat()
                    cx = nextFloat(); cy = nextFloat()
                    cs.curveTo(x1, y1, x2, y2, cx, cy)
                }
                "Z", "z" -> cs.closePath()
            }
        }
    }
}

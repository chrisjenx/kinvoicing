package com.chrisjenx.kinvoicing.composehtml.internal

/**
 * Represents an HTML element in the reconstructed document tree.
 * Built by [LayoutAnalyzer] from flat [SvgNode] elements via spatial clustering.
 */
internal data class HtmlNode(
    /** HTML tag name (div, span, h1, h2, p, hr, button, input, table, img, a, etc.). */
    val tag: String,
    /** Inline CSS properties. */
    val css: MutableMap<String, String> = mutableMapOf(),
    /** HTML attributes (href, name, type, alt, src, etc.). */
    val attributes: MutableMap<String, String> = mutableMapOf(),
    /** Text content (for leaf text nodes). Null for container elements. */
    val textContent: String? = null,
    /** Child elements. */
    val children: MutableList<HtmlNode> = mutableListOf(),
    /** Whether this is a self-closing element (hr, img, input, br). */
    val selfClosing: Boolean = false,
    /** Raw SVG content for fallback rendering (Canvas, complex paths). */
    val rawSvg: String? = null,
)

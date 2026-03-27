package com.chrisjenx.kinvoicing.composehtml.internal

/**
 * Intermediate representation of an SVG element, parsed from Skia SVGCanvas output.
 * All coordinates are in SVG pixels (divide by density to get pt).
 */
internal data class SvgNode(
    val type: SvgNodeType,
    /** Absolute X position in SVG pixels (from transform). */
    val x: Float,
    /** Absolute Y position in SVG pixels (from transform). */
    val y: Float,
    /** Width in SVG pixels. */
    val width: Float,
    /** Height in SVG pixels. */
    val height: Float,
    /** Text content (for TEXT nodes). */
    val textContent: String? = null,
    /** CSS-mappable properties extracted from SVG attributes. */
    val cssProperties: Map<String, String> = emptyMap(),
    /** Additional SVG attributes (href for images, d for paths, etc.). */
    val attributes: Map<String, String> = emptyMap(),
    /** Children (for GROUP nodes, or container groupings). */
    val children: List<SvgNode> = emptyList(),
)

internal enum class SvgNodeType {
    /** Root SVG element. */
    ROOT,
    /** `<g>` group (with transform/clip). */
    GROUP,
    /** `<rect>` rectangle. */
    RECT,
    /** `<text>` element. */
    TEXT,
    /** `<image>` element (base64 embedded). */
    IMAGE,
    /** `<path>` element (bezier curves, rounded rects, etc.). */
    PATH,
    /** `<line>` element. */
    LINE,
    /** `<circle>` element. */
    CIRCLE,
    /** `<ellipse>` element. */
    ELLIPSE,
    /** `<polyline>` or `<polygon>`. */
    POLY,
    /** `<use>` reference to a def. */
    USE,
}

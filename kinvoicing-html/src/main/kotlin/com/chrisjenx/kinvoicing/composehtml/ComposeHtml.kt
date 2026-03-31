package com.chrisjenx.kinvoicing.composehtml

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Density
import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.kinvoicing.composehtml.internal.HtmlRenderer

/**
 * Renders a single page of Compose content to a self-contained HTML document.
 *
 * The HTML uses the same SVG intermediate as [renderToPdf] (vector mode), converting
 * SVG elements to HTML+CSS equivalents. Simple shapes become `<div>` elements,
 * text becomes `<span>` elements, and complex paths fall back to inline `<svg>`.
 *
 * The output is a complete HTML document with embedded fonts (when [useBundledFont] is true),
 * CSS reset, print styles, and absolute positioning matching the Compose layout.
 *
 * @param config Page size and margins. Defaults to A4.
 * @param density Render density. Higher values produce more detailed SVG intermediate.
 * @param useBundledFont Whether to use the bundled Inter font as the default text style
 *   and embed it as base64 @font-face declarations. Defaults to true.
 * @param content The composable content to render.
 * @return A self-contained HTML document string.
 */
public fun renderToHtml(
    config: PdfPageConfig = PdfPageConfig.A4,
    density: Density = Density(2f),
    useBundledFont: Boolean = true,
    content: @Composable () -> Unit,
): String {
    return HtmlRenderer.renderSinglePage(config, density, useBundledFont, content)
}

/**
 * Renders multiple pages of Compose content to a single HTML document.
 *
 * Each page is rendered as a separate `<div class="page">` element. Print styles
 * ensure proper page breaks when printing from a browser.
 *
 * @param pages Number of pages to render.
 * @param config Page size and margins (applied to all pages). Defaults to A4.
 * @param density Render density. Higher values produce more detailed SVG intermediate.
 * @param useBundledFont Whether to use the bundled Inter font as the default text style
 *   and embed it as base64 @font-face declarations. Defaults to true.
 * @param content The composable content for each page. Receives the zero-based page index.
 * @return A self-contained HTML document string.
 */
public fun renderToHtml(
    pages: Int,
    config: PdfPageConfig = PdfPageConfig.A4,
    density: Density = Density(2f),
    useBundledFont: Boolean = true,
    content: @Composable (pageIndex: Int) -> Unit,
): String {
    return HtmlRenderer.renderMultiPage(pages, config, density, useBundledFont, content)
}

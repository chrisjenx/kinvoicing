package com.chrisjenx.composepdf

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Density
import com.chrisjenx.composepdf.internal.PdfRenderer

/**
 * Renders a single page of Compose content to a PDF.
 *
 * @param config Page size and margins. Defaults to A4.
 * @param density Render density. Higher values produce sharper raster output.
 * @param content The composable content to render.
 * @return A valid PDF as a ByteArray.
 */
fun renderToPdf(
    config: PdfPageConfig = PdfPageConfig.A4,
    density: Density = Density(2f),
    content: @Composable () -> Unit,
): ByteArray {
    return PdfRenderer.renderSinglePage(config, density, content)
}

/**
 * Renders multiple pages of Compose content to a single PDF.
 *
 * @param pages Number of pages to render.
 * @param config Page size and margins (applied to all pages). Defaults to A4.
 * @param density Render density. Higher values produce sharper raster output.
 * @param content The composable content for each page. Receives the zero-based page index.
 * @return A valid PDF as a ByteArray.
 */
fun renderToPdf(
    pages: Int,
    config: PdfPageConfig = PdfPageConfig.A4,
    density: Density = Density(2f),
    content: @Composable (pageIndex: Int) -> Unit,
): ByteArray {
    return PdfRenderer.renderMultiPage(pages, config, density, content)
}

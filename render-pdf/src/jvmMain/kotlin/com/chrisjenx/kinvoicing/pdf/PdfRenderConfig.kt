package com.chrisjenx.kinvoicing.pdf

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.compose2pdf.PdfPagination
import com.chrisjenx.compose2pdf.RenderMode

/**
 * Configuration for PDF rendering.
 * Wraps compose2pdf's [PdfPageConfig], [RenderMode], [PdfPagination], and [Density].
 */
public data class PdfRenderConfig(
    val pageConfig: PdfPageConfig = PdfPageConfig.A4,
    val renderMode: RenderMode = RenderMode.VECTOR,
    val pagination: PdfPagination = PdfPagination.AUTO,
    val density: Density = Density(2f),
    val defaultFontFamily: FontFamily? = null,
) {
    public companion object {
        /** Default configuration: A4 page, vector rendering, 2x density. */
        public val Default: PdfRenderConfig = PdfRenderConfig()
        /** US Letter page size preset. */
        public val Letter: PdfRenderConfig = PdfRenderConfig(pageConfig = PdfPageConfig.Letter)
        /** ISO A4 page size preset. */
        public val A4: PdfRenderConfig = PdfRenderConfig(pageConfig = PdfPageConfig.A4)
    }
}

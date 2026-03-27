package com.chrisjenx.invoicekit.pdf

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.compose2pdf.RenderMode

/**
 * Configuration for PDF rendering.
 * Wraps compose2pdf's [PdfPageConfig], [RenderMode], and [Density].
 *
 * @property defaultFontFamily Optional Compose font family override for PDF text rendering.
 */
public data class PdfRenderConfig(
    val pageConfig: PdfPageConfig = PdfPageConfig.A4,
    val renderMode: RenderMode = RenderMode.VECTOR,
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

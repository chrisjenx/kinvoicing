package com.chrisjenx.kinvoicing.html

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

/**
 * Font family using the Inter fonts bundled in compose2pdf.
 * Used by the HTML renderer to match compose2pdf's PDF text rendering.
 */
public val PdfFontFamily: FontFamily = try {
    FontFamily(
        Font(resource = "fonts/Inter-Regular.ttf", weight = FontWeight.Normal, style = FontStyle.Normal),
        Font(resource = "fonts/Inter-Bold.ttf", weight = FontWeight.Bold, style = FontStyle.Normal),
        Font(resource = "fonts/Inter-Italic.ttf", weight = FontWeight.Normal, style = FontStyle.Italic),
        Font(resource = "fonts/Inter-BoldItalic.ttf", weight = FontWeight.Bold, style = FontStyle.Italic),
    )
} catch (_: Exception) {
    FontFamily.SansSerif
}

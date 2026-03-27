@file:OptIn(InternalComposeUiApi::class)

package com.chrisjenx.kinvoicing.html.internal

import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.kinvoicing.html.PdfFontFamily
import com.chrisjenx.kinvoicing.html.LocalPdfElementCollector
import com.chrisjenx.kinvoicing.html.LocalPdfLinkCollector
import com.chrisjenx.kinvoicing.html.PdfElementAnnotation
import com.chrisjenx.kinvoicing.html.PdfElementCollector
import com.chrisjenx.kinvoicing.html.PdfLinkAnnotation
import com.chrisjenx.kinvoicing.html.PdfLinkCollector
import java.util.Base64

/**
 * Orchestrates the Compose → SVG → HTML rendering pipeline.
 * Parallel to [PdfRenderer] which does Compose → SVG → PDF.
 */
internal object HtmlRenderer {

    fun renderSinglePage(
        config: PdfPageConfig,
        density: Density,
        useBundledFont: Boolean,
        content: @Composable () -> Unit,
    ): String {
        return renderMultiPage(
            pageCount = 1,
            config = config,
            density = density,
            useBundledFont = useBundledFont,
            content = { content() },
        )
    }

    fun renderMultiPage(
        pageCount: Int,
        config: PdfPageConfig,
        density: Density,
        useBundledFont: Boolean,
        content: @Composable (pageIndex: Int) -> Unit,
    ): String {
        require(pageCount > 0) { "pageCount must be positive, was $pageCount" }

        val pxW = (config.contentWidth.value * density.density).toInt()
        val pxH = (config.contentHeight.value * density.density).toInt()

        val svgPages = mutableListOf<String>()
        val linksByPage = mutableListOf<List<PdfLinkAnnotation>>()
        val elementsByPage = mutableListOf<List<PdfElementAnnotation>>()

        for (pageIndex in 0 until pageCount) {
            val linkCollector = PdfLinkCollector()
            val elementCollector = PdfElementCollector()
            val svg = ComposeToSvg.render(pxW, pxH, density) {
                CompositionLocalProvider(
                    LocalPdfLinkCollector provides linkCollector,
                    LocalPdfElementCollector provides elementCollector,
                ) {
                    if (useBundledFont) {
                        ProvideTextStyle(TextStyle(fontFamily = PdfFontFamily)) {
                            content(pageIndex)
                        }
                    } else {
                        content(pageIndex)
                    }
                }
            }
            svgPages.add(svg)
            linksByPage.add(linkCollector.links.toList())
            elementsByPage.add(elementCollector.elements.toList())
        }

        val fontBase64 = if (useBundledFont) bundledFontBase64 else emptyMap()

        return SvgToSemanticHtmlConverter.convert(
            svgPages = svgPages,
            config = config,
            density = density.density,
            linksByPage = linksByPage,
            elementsByPage = elementsByPage,
            fontBase64 = fontBase64,
        )
    }

    private val bundledFontBase64: Map<String, String> by lazy {
        val variants = mapOf(
            "400-normal" to "fonts/Inter-Regular.ttf",
            "700-normal" to "fonts/Inter-Bold.ttf",
            "400-italic" to "fonts/Inter-Italic.ttf",
            "700-italic" to "fonts/Inter-BoldItalic.ttf",
        )
        val result = mutableMapOf<String, String>()
        val classLoader = Thread.currentThread().contextClassLoader ?: return@lazy result
        for ((variant, path) in variants) {
            try {
                val bytes = classLoader.getResourceAsStream(path)?.readBytes() ?: continue
                result[variant] = Base64.getEncoder().encodeToString(bytes)
            } catch (_: Exception) {
                // Font not available on classpath
            }
        }
        result
    }
}

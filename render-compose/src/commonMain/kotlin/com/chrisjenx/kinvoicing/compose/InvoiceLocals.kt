package com.chrisjenx.kinvoicing.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.chrisjenx.kinvoicing.ImageSource

/**
 * Wraps content with a clickable link annotation.
 *
 * Defaults to a no-op (renders content only). In PDF rendering contexts,
 * render-pdf overrides this with compose2pdf's `PdfLink` to produce
 * clickable link annotations in the output.
 */
public val LocalLinkWrapper: ProvidableCompositionLocal<@Composable (href: String, content: @Composable () -> Unit) -> Unit> =
    compositionLocalOf {
        { _: String, content: @Composable () -> Unit -> content() }
    }

/**
 * Renders an inline image from an [ImageSource].
 *
 * On JVM, defaults to Skia-based image decoding for [ImageSource.Bytes]
 * and native `painterResource` for CMP `DrawableImageSource`.
 * On other platforms, defaults to a placeholder text label.
 */
public val LocalImageRenderer: ProvidableCompositionLocal<@Composable (source: ImageSource, width: Int?, height: Int?, altText: String?) -> Unit> =
    compositionLocalOf {
        { source: ImageSource, width: Int?, height: Int?, altText: String? ->
            InvoiceImageRenderer(source, width, height, altText)
        }
    }

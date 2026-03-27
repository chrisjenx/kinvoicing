package com.chrisjenx.kinvoicing.html

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

/**
 * Wraps image content with alt text for accessibility in PDF and HTML output.
 *
 * When rendered via [renderToHtml], adds an `aria-label` attribute to the image region.
 * On screen, the Compose content displays normally.
 *
 * @param altText Descriptive alt text for the image.
 * @param modifier Modifier applied to the image wrapper.
 * @param content Visual composable content (typically an Image composable).
 */
@Composable
public fun PdfImage(
    altText: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val collector = LocalPdfElementCollector.current
    val density = LocalDensity.current

    Box(
        modifier = modifier.then(
            if (collector != null) {
                Modifier.onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    val size = coords.size.toSize()
                    collector.add(
                        PdfImageAnnotation(
                            altText = altText,
                            x = pos.x / density.density,
                            y = pos.y / density.density,
                            width = size.width / density.density,
                            height = size.height / density.density,
                            id = collector.generateId(),
                        )
                    )
                }
            } else Modifier
        ),
    ) {
        content()
    }
}

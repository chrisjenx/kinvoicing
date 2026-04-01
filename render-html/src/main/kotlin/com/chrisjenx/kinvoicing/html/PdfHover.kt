package com.chrisjenx.kinvoicing.html

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

/**
 * Adds CSS `:hover` effects to content in HTML output.
 *
 * When rendered via [renderToHtml], generates a transparent overlay div with
 * CSS hover styles. Has no effect in PDF output.
 * On screen, the Compose content displays normally.
 *
 * @param hoverStyles CSS styles applied on hover.
 * @param modifier Modifier applied to the hover wrapper.
 * @param content Visual composable content.
 */
@Composable
public fun PdfHover(
    hoverStyles: HoverStyles,
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
                        PdfHoverAnnotation(
                            hoverStyles = hoverStyles,
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

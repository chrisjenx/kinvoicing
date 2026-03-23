package com.chrisjenx.composepdf

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

/**
 * Wraps content as a range slider for HTML output.
 *
 * When rendered via [renderToHtml], the Compose visual is replaced with a native
 * `<input type="range">` element (replacement strategy — SVG suppressed).
 * PDF has no native AcroForm range field, so this is visual-only in PDF output.
 * On screen, the Compose content displays normally.
 *
 * @param name HTML input name (must be unique within the document).
 * @param min Minimum value.
 * @param max Maximum value.
 * @param value Current value.
 * @param step Step increment.
 * @param modifier Modifier applied to the slider wrapper.
 * @param content Visual composable content (rendered on screen and in PDF raster mode).
 */
@Composable
fun PdfSlider(
    name: String,
    min: Float = 0f,
    max: Float = 100f,
    value: Float = 0f,
    step: Float = 1f,
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
                        PdfSliderAnnotation(
                            name = name,
                            min = min,
                            max = max,
                            value = value,
                            step = step,
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

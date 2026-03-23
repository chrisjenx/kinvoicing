package com.chrisjenx.composepdf

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

/**
 * Wraps content as a checkbox for PDF and HTML output.
 *
 * When rendered via [renderToHtml], the Compose visual is replaced with a native
 * `<input type="checkbox">` element (replacement strategy — SVG suppressed).
 * When rendered via [renderToPdf], adds an AcroForm checkbox field.
 * On screen, the Compose content displays normally.
 *
 * @param name AcroForm field name / HTML input name (must be unique within the document).
 * @param label Label text displayed next to the checkbox in HTML output.
 * @param checked Whether the checkbox is checked.
 * @param modifier Modifier applied to the checkbox wrapper.
 * @param content Visual composable content for the checkbox (rendered on screen and in PDF raster mode).
 */
@Composable
fun PdfCheckbox(
    name: String,
    label: String,
    checked: Boolean = false,
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
                        PdfCheckboxAnnotation(
                            name = name,
                            label = label,
                            checked = checked,
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

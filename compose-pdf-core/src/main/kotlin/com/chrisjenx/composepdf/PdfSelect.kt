package com.chrisjenx.composepdf

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

/**
 * Wraps content as a dropdown select for PDF and HTML output.
 *
 * When rendered via [renderToHtml], the Compose visual is replaced with a native
 * `<select>` element (replacement strategy — SVG suppressed).
 * When rendered via [renderToPdf], adds an AcroForm combo box field.
 * On screen, the Compose content displays normally.
 *
 * @param name AcroForm field name / HTML select name (must be unique within the document).
 * @param options The available options for the dropdown.
 * @param selectedValue The currently selected option value.
 * @param modifier Modifier applied to the select wrapper.
 * @param content Visual composable content (rendered on screen and in PDF raster mode).
 */
@Composable
fun PdfSelect(
    name: String,
    options: List<PdfSelectOption>,
    selectedValue: String = "",
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
                        PdfSelectAnnotation(
                            name = name,
                            options = options,
                            selectedValue = selectedValue,
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

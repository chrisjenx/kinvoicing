package com.chrisjenx.composepdf

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

/**
 * Wraps content as a radio button for PDF and HTML output.
 *
 * When rendered via [renderToHtml], the Compose visual is replaced with a native
 * `<input type="radio">` element (replacement strategy — SVG suppressed).
 * When rendered via [renderToPdf], adds an AcroForm radio button field.
 * On screen, the Compose content displays normally.
 *
 * Radio buttons sharing the same [groupName] are mutually exclusive in both
 * HTML (via `name` attribute) and PDF (grouped under one `PDRadioButton` field).
 *
 * @param name Unique identifier for this radio button within the group.
 * @param value The value submitted when this radio button is selected.
 * @param groupName Group name — radio buttons with the same groupName are mutually exclusive.
 * @param selected Whether this radio button is selected.
 * @param label Label text displayed next to the radio button in HTML output.
 * @param modifier Modifier applied to the radio button wrapper.
 * @param content Visual composable content (rendered on screen and in PDF raster mode).
 */
@Composable
fun PdfRadioButton(
    name: String,
    value: String,
    groupName: String,
    selected: Boolean = false,
    label: String = "",
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
                        PdfRadioButtonAnnotation(
                            name = name,
                            value = value,
                            groupName = groupName,
                            selected = selected,
                            label = label,
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

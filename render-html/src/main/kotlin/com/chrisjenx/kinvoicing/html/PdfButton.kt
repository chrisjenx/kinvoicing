package com.chrisjenx.kinvoicing.html

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

/**
 * Wraps content as a button for PDF and HTML output.
 *
 * When rendered via [renderToHtml], emits a native `<button>` element.
 * When rendered via [renderToPdf], adds an AcroForm push button field.
 * On screen, the Compose content displays normally.
 *
 * @param label Button label text (used in native HTML/PDF output).
 * @param name AcroForm field name (must be unique within the document).
 * @param onClick Optional JavaScript action (executes in PDF viewer or HTML onclick).
 * @param modifier Modifier applied to the button wrapper.
 * @param content Visual composable content for the button.
 */
@Composable
public fun PdfButton(
    label: String,
    name: String,
    onClick: String? = null,
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
                        PdfButtonAnnotation(
                            label = label,
                            name = name,
                            onClick = onClick,
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

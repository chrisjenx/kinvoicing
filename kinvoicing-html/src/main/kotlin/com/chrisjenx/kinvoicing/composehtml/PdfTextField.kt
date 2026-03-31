package com.chrisjenx.kinvoicing.composehtml

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

/**
 * Wraps content as a text input field for PDF and HTML output.
 *
 * When rendered via [renderToHtml], emits a native `<input>` or `<textarea>` element.
 * When rendered via [renderToPdf], adds an AcroForm text field.
 * On screen, the Compose content displays normally.
 *
 * @param name AcroForm field name (must be unique within the document).
 * @param modifier Modifier applied to the field wrapper.
 * @param placeholder Placeholder text shown when the field is empty.
 * @param value Initial/default value for the field.
 * @param multiline If true, renders as `<textarea>` in HTML and multiline text field in PDF.
 * @param maxLength Maximum character count (0 = unlimited).
 * @param content Visual composable content for the field appearance.
 */
@Composable
public fun PdfTextField(
    name: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    value: String = "",
    multiline: Boolean = false,
    maxLength: Int = 0,
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
                        PdfTextFieldAnnotation(
                            name = name,
                            placeholder = placeholder,
                            value = value,
                            multiline = multiline,
                            maxLength = maxLength,
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

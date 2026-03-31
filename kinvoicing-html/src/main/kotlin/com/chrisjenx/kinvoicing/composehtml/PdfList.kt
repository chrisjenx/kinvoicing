package com.chrisjenx.kinvoicing.composehtml

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

/**
 * Wraps content as an ordered list (`<ol>`) in HTML output.
 *
 * When rendered via [renderToHtml], the visual Compose content is replaced
 * by a native `<ol>` element. On screen, the Compose content displays normally.
 */
@Composable
public fun PdfOrderedList(
    modifier: Modifier = Modifier,
    content: @Composable PdfListScope.() -> Unit,
) {
    PdfListInternal(modifier = modifier, ordered = true, content = content)
}

/**
 * Wraps content as an unordered list (`<ul>`) in HTML output.
 *
 * When rendered via [renderToHtml], the visual Compose content is replaced
 * by a native `<ul>` element. On screen, the Compose content displays normally.
 */
@Composable
public fun PdfUnorderedList(
    modifier: Modifier = Modifier,
    content: @Composable PdfListScope.() -> Unit,
) {
    PdfListInternal(modifier = modifier, ordered = false, content = content)
}

public interface PdfListScope {
    @Composable
    public fun Item(
        semanticText: String? = null,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    )
}

@Composable
private fun PdfListInternal(
    modifier: Modifier,
    ordered: Boolean,
    content: @Composable PdfListScope.() -> Unit,
) {
    val collector = LocalPdfElementCollector.current
    val density = LocalDensity.current
    val scope = remember { PdfListScopeImpl() }
    scope.reset()

    Box(
        modifier = modifier.then(
            if (collector != null) {
                Modifier.onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    val size = coords.size.toSize()
                    collector.add(
                        PdfListAnnotation(
                            ordered = ordered,
                            items = scope.buildItems(),
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
        Column {
            scope.content()
        }
    }
}

internal class PdfListScopeImpl : PdfListScope {
    private val items = mutableListOf<PdfListItemAnnotation>()

    fun reset() {
        items.clear()
    }

    fun buildItems(): List<PdfListItemAnnotation> = items.toList()

    @Composable
    override fun Item(
        semanticText: String?,
        modifier: Modifier,
        content: @Composable () -> Unit,
    ) {
        val collector = LocalPdfElementCollector.current

        // Add item data during composition (runs before layout) so it's available
        // when the parent's onGloballyPositioned fires to build the annotation.
        if (collector != null) {
            items.add(
                PdfListItemAnnotation(text = semanticText ?: "")
            )
        }

        Box(modifier = modifier) {
            content()
        }
    }
}

package com.chrisjenx.kinvoicing.composehtml

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

data class PdfLinkAnnotation(
    val href: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
)

class PdfLinkCollector {
    private val _links = mutableListOf<PdfLinkAnnotation>()
    val links: List<PdfLinkAnnotation> get() = _links

    fun add(annotation: PdfLinkAnnotation) {
        _links.add(annotation)
    }

    fun clear() {
        _links.clear()
    }
}

val LocalPdfLinkCollector = compositionLocalOf<PdfLinkCollector?> { null }

@Composable
fun PdfLink(
    href: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val collector = LocalPdfLinkCollector.current
    val density = LocalDensity.current

    Box(
        modifier = modifier.then(
            if (collector != null) {
                Modifier.onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    val size = coords.size.toSize()
                    collector.add(
                        PdfLinkAnnotation(
                            href = href,
                            x = pos.x / density.density,
                            y = pos.y / density.density,
                            width = size.width / density.density,
                            height = size.height / density.density,
                        ),
                    )
                }
            } else Modifier,
        ),
    ) {
        content()
    }
}

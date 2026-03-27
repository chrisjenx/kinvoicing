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
 * Wraps table content for native HTML `<table>` output and PDF tagged structure.
 *
 * When rendered via [renderToHtml], the visual Compose content within this wrapper
 * is replaced by a semantic `<table>` element. When rendered to screen, the
 * Compose content is displayed normally.
 *
 * @param modifier Modifier applied to the table wrapper.
 * @param caption Optional table caption.
 * @param content Table content defined using [PdfTableScope] DSL.
 */
@Composable
fun PdfTable(
    modifier: Modifier = Modifier,
    caption: String? = null,
    content: @Composable PdfTableScope.() -> Unit,
) {
    val collector = LocalPdfElementCollector.current
    val density = LocalDensity.current
    val scope = remember { PdfTableScopeImpl() }
    scope.reset()

    Box(
        modifier = modifier.then(
            if (collector != null) {
                Modifier.onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    val size = coords.size.toSize()
                    collector.add(
                        PdfTableAnnotation(
                            rows = scope.buildRows(),
                            caption = caption,
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

interface PdfTableScope {
    @Composable
    fun HeaderRow(content: @Composable PdfTableRowScope.() -> Unit)

    @Composable
    fun Row(content: @Composable PdfTableRowScope.() -> Unit)
}

interface PdfTableRowScope {
    @Composable
    fun Cell(
        semanticText: String? = null,
        colSpan: Int = 1,
        rowSpan: Int = 1,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    )
}

internal class PdfTableScopeImpl : PdfTableScope {
    private val rows = mutableListOf<RowBuilder>()

    fun reset() {
        rows.clear()
    }

    fun buildRows(): List<PdfTableRowAnnotation> = rows.map { it.build() }

    @Composable
    override fun HeaderRow(content: @Composable PdfTableRowScope.() -> Unit) {
        val rowBuilder = remember { RowBuilder(isHeader = true) }
        rowBuilder.reset()
        rows.add(rowBuilder)
        androidx.compose.foundation.layout.Row {
            val rowScope = PdfTableRowScopeImpl(rowBuilder)
            rowScope.content()
        }
    }

    @Composable
    override fun Row(content: @Composable PdfTableRowScope.() -> Unit) {
        val rowBuilder = remember { RowBuilder(isHeader = false) }
        rowBuilder.reset()
        rows.add(rowBuilder)
        androidx.compose.foundation.layout.Row {
            val rowScope = PdfTableRowScopeImpl(rowBuilder)
            rowScope.content()
        }
    }
}

internal class RowBuilder(val isHeader: Boolean) {
    private val cells = mutableListOf<PdfTableCellAnnotation>()

    fun reset() {
        cells.clear()
    }

    fun addCell(cell: PdfTableCellAnnotation) {
        cells.add(cell)
    }

    fun build(): PdfTableRowAnnotation = PdfTableRowAnnotation(
        cells = cells.toList(),
        isHeader = isHeader,
    )
}

internal class PdfTableRowScopeImpl(
    private val rowBuilder: RowBuilder,
) : PdfTableRowScope {

    @Composable
    override fun Cell(
        semanticText: String?,
        colSpan: Int,
        rowSpan: Int,
        modifier: Modifier,
        content: @Composable () -> Unit,
    ) {
        val collector = LocalPdfElementCollector.current

        // Add cell data during composition (runs before layout) so it's available
        // when the parent's onGloballyPositioned fires to build the annotation.
        if (collector != null) {
            rowBuilder.addCell(
                PdfTableCellAnnotation(
                    text = semanticText ?: "",
                    colSpan = colSpan,
                    rowSpan = rowSpan,
                )
            )
        }

        Box(modifier = modifier) {
            content()
        }
    }
}

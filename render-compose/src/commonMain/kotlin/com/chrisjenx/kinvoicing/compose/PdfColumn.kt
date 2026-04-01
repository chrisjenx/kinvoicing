package com.chrisjenx.kinvoicing.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Layout provider for invoice sections.
 *
 * Defaults to [Column]. In PDF rendering contexts, render-pdf overrides this
 * with a pass-through so sections become direct children of compose2pdf's
 * paginated layout.
 */
public val LocalPdfColumn: ProvidableCompositionLocal<@Composable (Modifier, @Composable () -> Unit) -> Unit> =
    compositionLocalOf {
        { modifier: Modifier, content: @Composable () -> Unit -> Column(modifier) { content() } }
    }

@Composable
internal fun PdfColumn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    LocalPdfColumn.current.invoke(modifier, content)
}

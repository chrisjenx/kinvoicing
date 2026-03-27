package com.chrisjenx.kinvoicing.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.chrisjenx.kinvoicing.ArgbColor
import com.chrisjenx.kinvoicing.InvoiceStyle

/**
 * Derives an [InvoiceStyle] from the current [MaterialTheme] color scheme.
 *
 * This allows invoices to automatically match your app's theming. Call this
 * within a composable where [MaterialTheme] is provided:
 *
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val style = InvoiceStyle.fromMaterialTheme {
 *         copy(accentBorder = true) // optional overrides
 *     }
 *     InvoiceView(document.copy(style = style))
 * }
 * ```
 *
 * @param customize Optional transform to override individual style properties
 *   after the MaterialTheme colors have been applied.
 */
@Composable
public fun InvoiceStyle.Companion.fromMaterialTheme(
    customize: InvoiceStyle.() -> InvoiceStyle = { this },
): InvoiceStyle {
    val cs = MaterialTheme.colorScheme
    return InvoiceStyle(
        primaryColor = cs.primary.toArgbColor(),
        secondaryColor = cs.onSurfaceVariant.toArgbColor(),
        textColor = cs.onSurface.toArgbColor(),
        backgroundColor = cs.surface.toArgbColor(),
        negativeColor = cs.error.toArgbColor(),
        borderColor = cs.outlineVariant.toArgbColor(),
        dividerColor = cs.outlineVariant.copy(alpha = 0.5f).toArgbColor(),
        mutedBackgroundColor = cs.surfaceContainerLow.toArgbColor(),
        surfaceColor = cs.surfaceVariant.toArgbColor(),
    ).customize()
}

/** Convert a Compose [Color] to an [ArgbColor] for [InvoiceStyle] color properties. */
public fun Color.toArgbColor(): ArgbColor = ArgbColor(toArgb().toLong() and 0xFFFFFFFFL)

package com.chrisjenx.kinvoicing.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
        primaryColor = cs.primary.toArgbLong(),
        secondaryColor = cs.onSurfaceVariant.toArgbLong(),
        textColor = cs.onSurface.toArgbLong(),
        backgroundColor = cs.surface.toArgbLong(),
        negativeColor = cs.error.toArgbLong(),
        borderColor = cs.outlineVariant.toArgbLong(),
        dividerColor = cs.outlineVariant.copy(alpha = 0.5f).toArgbLong(),
        mutedBackgroundColor = cs.surfaceContainerLow.toArgbLong(),
        surfaceColor = cs.surfaceVariant.toArgbLong(),
    ).customize()
}

/** Convert a Compose [Color] to an ARGB [Long] for [InvoiceStyle] color properties. */
public fun Color.toArgbLong(): Long = toArgb().toLong() and 0xFFFFFFFFL

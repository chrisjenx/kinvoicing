package com.chrisjenx.kinvoicing.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.chrisjenx.kinvoicing.ArgbColor
import com.chrisjenx.kinvoicing.InvoiceStatus
import com.chrisjenx.kinvoicing.InvoiceStyle
import com.chrisjenx.kinvoicing.StatusDisplay

public val LocalInvoiceStyle: ProvidableCompositionLocal<InvoiceStyle> = compositionLocalOf { InvoiceStyle() }
public val LocalInvoiceStatus: ProvidableCompositionLocal<InvoiceStatus?> = compositionLocalOf { null }
public val LocalStatusDisplay: ProvidableCompositionLocal<StatusDisplay> = compositionLocalOf { StatusDisplay.Badge }

@Composable
public fun InvoiceStyleProvider(
    style: InvoiceStyle,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalInvoiceStyle provides style) {
        content()
    }
}

/** Convert an [ArgbColor] to a Compose [Color]. */
public fun ArgbColor.toComposeColor(): Color {
    val v = value
    return Color(
        red = ((v shr 16) and 0xFF).toInt(),
        green = ((v shr 8) and 0xFF).toInt(),
        blue = (v and 0xFF).toInt(),
        alpha = ((v shr 24) and 0xFF).toInt(),
    )
}

// Branding colors
internal val InvoiceStyle.primaryComposeColor: Color get() = primaryColor.toComposeColor()
internal val InvoiceStyle.secondaryComposeColor: Color get() = secondaryColor.toComposeColor()
internal val InvoiceStyle.textComposeColor: Color get() = textColor.toComposeColor()
internal val InvoiceStyle.backgroundComposeColor: Color get() = backgroundColor.toComposeColor()

// Semantic colors (theme-aware, replacing hardcoded InvoiceColors constants)
internal val InvoiceStyle.negativeComposeColor: Color get() = negativeColor.toComposeColor()
internal val InvoiceStyle.borderComposeColor: Color get() = borderColor.toComposeColor()
internal val InvoiceStyle.dividerComposeColor: Color get() = dividerColor.toComposeColor()
internal val InvoiceStyle.mutedBgComposeColor: Color get() = mutedBackgroundColor.toComposeColor()
internal val InvoiceStyle.surfaceComposeColor: Color get() = surfaceColor.toComposeColor()

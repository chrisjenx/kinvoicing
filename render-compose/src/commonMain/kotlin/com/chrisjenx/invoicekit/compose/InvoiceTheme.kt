package com.chrisjenx.invoicekit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.chrisjenx.invoicekit.InvoiceColors
import com.chrisjenx.invoicekit.InvoiceStyle

internal val LocalInvoiceStyle = compositionLocalOf { InvoiceStyle() }

// Semantic Compose colors from InvoiceColors
internal val NegativeColor = InvoiceColors.NEGATIVE.toComposeColor()
internal val BorderColor = InvoiceColors.BORDER.toComposeColor()
internal val DividerColor = InvoiceColors.DIVIDER.toComposeColor()
internal val BgMutedColor = InvoiceColors.BG_MUTED.toComposeColor()

@Composable
internal fun InvoiceStyleProvider(
    style: InvoiceStyle,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalInvoiceStyle provides style) {
        content()
    }
}

internal fun Long.toComposeColor(): Color {
    return Color(
        red = ((this shr 16) and 0xFF).toInt(),
        green = ((this shr 8) and 0xFF).toInt(),
        blue = (this and 0xFF).toInt(),
        alpha = ((this shr 24) and 0xFF).toInt(),
    )
}

internal val InvoiceStyle.primaryComposeColor: Color get() = primaryColor.toComposeColor()
internal val InvoiceStyle.secondaryComposeColor: Color get() = secondaryColor.toComposeColor()
internal val InvoiceStyle.textComposeColor: Color get() = textColor.toComposeColor()
internal val InvoiceStyle.backgroundComposeColor: Color get() = backgroundColor.toComposeColor()

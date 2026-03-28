package com.chrisjenx.kinvoicing.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.chrisjenx.kinvoicing.InvoiceColors
import com.chrisjenx.kinvoicing.InvoiceStyle

public val LocalInvoiceStyle: ProvidableCompositionLocal<InvoiceStyle> = compositionLocalOf { InvoiceStyle() }

// Semantic Compose colors from InvoiceColors
internal val NegativeColor = InvoiceColors.NEGATIVE.toComposeColor()
internal val BorderColor = InvoiceColors.BORDER.toComposeColor()
internal val DividerColor = InvoiceColors.DIVIDER.toComposeColor()
internal val BgMutedColor = InvoiceColors.BG_MUTED.toComposeColor()

@Composable
public fun InvoiceStyleProvider(
    style: InvoiceStyle,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalInvoiceStyle provides style) {
        content()
    }
}

public fun Long.toComposeColor(): Color {
    return Color(
        red = ((this shr 16) and 0xFF).toInt(),
        green = ((this shr 8) and 0xFF).toInt(),
        blue = (this and 0xFF).toInt(),
        alpha = ((this shr 24) and 0xFF).toInt(),
    )
}

public val InvoiceStyle.primaryComposeColor: Color get() = primaryColor.toComposeColor()
public val InvoiceStyle.secondaryComposeColor: Color get() = secondaryColor.toComposeColor()
public val InvoiceStyle.textComposeColor: Color get() = textColor.toComposeColor()
public val InvoiceStyle.backgroundComposeColor: Color get() = backgroundColor.toComposeColor()

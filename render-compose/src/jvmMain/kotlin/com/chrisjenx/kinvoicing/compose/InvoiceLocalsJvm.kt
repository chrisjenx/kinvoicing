package com.chrisjenx.kinvoicing.compose

import androidx.compose.runtime.Composable
import com.chrisjenx.kinvoicing.ImageSource

internal actual fun defaultImageRenderer(): @Composable (ImageSource, Int?, Int?, String?) -> Unit =
    { source, width, height, altText ->
        InvoiceImageRenderer(source, width, height, altText)
    }

package com.chrisjenx.kinvoicing.compose

import androidx.compose.ui.graphics.ImageBitmap

internal expect fun decodeImageBytes(bytes: ByteArray): DecodedImage

internal class DecodedImage(
    val bitmap: ImageBitmap,
    val intrinsicWidth: Int,
    val intrinsicHeight: Int,
)

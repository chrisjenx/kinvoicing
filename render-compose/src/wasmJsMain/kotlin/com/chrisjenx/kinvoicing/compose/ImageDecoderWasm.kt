package com.chrisjenx.kinvoicing.compose

import androidx.compose.ui.graphics.toComposeImageBitmap

internal actual fun decodeImageBytes(bytes: ByteArray): DecodedImage {
    val skiaImage = org.jetbrains.skia.Image.makeFromEncoded(bytes)
    return DecodedImage(skiaImage.toComposeImageBitmap(), skiaImage.width, skiaImage.height)
}

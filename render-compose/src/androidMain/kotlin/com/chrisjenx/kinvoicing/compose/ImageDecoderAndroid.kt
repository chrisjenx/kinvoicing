package com.chrisjenx.kinvoicing.compose

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap

internal actual fun decodeImageBytes(bytes: ByteArray): DecodedImage {
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return DecodedImage(bitmap.asImageBitmap(), bitmap.width, bitmap.height)
}

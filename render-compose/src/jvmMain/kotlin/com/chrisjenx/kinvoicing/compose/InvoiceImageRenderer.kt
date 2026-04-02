package com.chrisjenx.kinvoicing.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import com.chrisjenx.kinvoicing.ImageSource
import org.jetbrains.compose.resources.painterResource

private const val DEFAULT_MAX_WIDTH = 200
private const val DEFAULT_MAX_HEIGHT = 60

/**
 * JVM image renderer that handles both [ImageSource.Bytes] (Skia decode)
 * and [DrawableImageSource] (native `painterResource`).
 *
 * When [width] and [height] are both specified, renders at that exact size.
 * Otherwise, uses sensible defaults (200×60 dp max) to match the HTML email logo constraint.
 */
@Composable
public fun InvoiceImageRenderer(
    source: ImageSource,
    width: Int?,
    height: Int?,
    altText: String?,
) {
    when (source) {
        is DrawableImageSource -> {
            val painter = painterResource(source.resource)
            Image(
                painter = painter,
                contentDescription = altText,
                modifier = imageModifier(width, height),
            )
        }
        else -> {
            val decoded = remember(source) {
                val skiaImage = org.jetbrains.skia.Image.makeFromEncoded(source.bytes)
                DecodedImage(skiaImage.toComposeImageBitmap(), skiaImage.width, skiaImage.height)
            }
            Image(
                bitmap = decoded.bitmap,
                contentDescription = altText,
                modifier = imageModifier(width, height, decoded.intrinsicWidth, decoded.intrinsicHeight),
            )
        }
    }
}

private class DecodedImage(
    val bitmap: androidx.compose.ui.graphics.ImageBitmap,
    val intrinsicWidth: Int,
    val intrinsicHeight: Int,
)

private fun imageModifier(
    width: Int?,
    height: Int?,
    intrinsicWidth: Int? = null,
    intrinsicHeight: Int? = null,
): Modifier = when {
    width != null && height != null -> Modifier.size(width.dp, height.dp)
    intrinsicWidth != null && intrinsicHeight != null &&
        intrinsicWidth <= DEFAULT_MAX_WIDTH && intrinsicHeight <= DEFAULT_MAX_HEIGHT ->
        Modifier.size(intrinsicWidth.dp, intrinsicHeight.dp)
    else -> Modifier.widthIn(max = DEFAULT_MAX_WIDTH.dp).heightIn(max = DEFAULT_MAX_HEIGHT.dp)
}

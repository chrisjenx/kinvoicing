package com.chrisjenx.kinvoicing.fidelity

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import com.chrisjenx.kinvoicing.InvoiceDocument
import com.chrisjenx.kinvoicing.compose.InvoiceContent
import org.jetbrains.skia.Surface
import java.awt.image.BufferedImage

/**
 * Renders an InvoiceDocument to a BufferedImage using Compose's headless rendering.
 */
internal object RasterizeCompose {

    fun rasterize(
        document: InvoiceDocument,
        width: Int = 1190,
        height: Int = 1684,
        density: Float = 2f,
    ): BufferedImage {
        val scene = ImageComposeScene(
            width = width,
            height = height,
            density = Density(density),
        ) {
            InvoiceContent(document)
        }

        val image = scene.render()
        scene.close()

        // Convert Skia Image to BufferedImage
        val buffered = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val pixels = image.peekPixels()!!
        for (y in 0 until height) {
            for (x in 0 until width) {
                buffered.setRGB(x, y, pixels.getColor(x, y))
            }
        }

        return buffered
    }
}

package com.chrisjenx.kinvoicing.fidelity

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import com.chrisjenx.kinvoicing.InvoiceDocument
import com.chrisjenx.kinvoicing.compose.InvoiceContent
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

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
        // Encode the rendered Skia Image to PNG bytes BEFORE closing the scene.
        // peekPixels() aliases the surface memory; reading from it after
        // scene.close() is a use-after-free that SIGSEGVs in libskiko on Linux
        // (issue #6). Skia's encodeToData snapshots the pixels into an owned
        // Data buffer, which is safe to use post-close. ImageIO.read then
        // produces a BufferedImage independent of any Skia state.
        val scene = ImageComposeScene(
            width = width,
            height = height,
            density = Density(density),
        ) {
            InvoiceContent(document)
        }
        return try {
            val image = scene.render()
            val data = image.encodeToData() ?: error("Failed to encode rasterized invoice")
            ImageIO.read(ByteArrayInputStream(data.bytes))
                ?: error("Failed to decode rasterized invoice PNG")
        } finally {
            scene.close()
        }
    }
}

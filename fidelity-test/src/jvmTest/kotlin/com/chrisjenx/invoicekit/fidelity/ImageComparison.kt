package com.chrisjenx.invoicekit.fidelity

import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Image comparison utilities: RMSE, SSIM, and diff image generation.
 * Uses bitwise RGB extraction instead of Color objects to avoid per-pixel allocations.
 */
internal object ImageComparison {

    private inline fun Int.r() = (this shr 16) and 0xFF
    private inline fun Int.g() = (this shr 8) and 0xFF
    private inline fun Int.b() = this and 0xFF

    /**
     * Compute Root Mean Square Error between two images, normalized to 0.0-1.0.
     */
    fun rmse(img1: BufferedImage, img2: BufferedImage): Double {
        val w = min(img1.width, img2.width)
        val h = min(img1.height, img2.height)
        if (w == 0 || h == 0) return 1.0

        var sumSqDiff = 0.0
        var count = 0

        for (y in 0 until h) {
            for (x in 0 until w) {
                val p1 = img1.getRGB(x, y)
                val p2 = img2.getRGB(x, y)

                val dr = (p1.r() - p2.r()) / 255.0
                val dg = (p1.g() - p2.g()) / 255.0
                val db = (p1.b() - p2.b()) / 255.0

                sumSqDiff += dr * dr + dg * dg + db * db
                count += 3
            }
        }

        return sqrt(sumSqDiff / count)
    }

    /**
     * Compute Structural Similarity Index (simplified) between two images.
     * Returns value in 0.0-1.0 range where 1.0 = identical.
     * Single-pass: computes luminance and statistics simultaneously.
     */
    fun ssim(img1: BufferedImage, img2: BufferedImage): Double {
        val w = min(img1.width, img2.width)
        val h = min(img1.height, img2.height)
        if (w == 0 || h == 0) return 0.0

        val n = (w * h).toDouble()
        var sum1 = 0.0
        var sum2 = 0.0
        var sum1sq = 0.0
        var sum2sq = 0.0
        var sum12 = 0.0

        for (y in 0 until h) {
            for (x in 0 until w) {
                val p1 = img1.getRGB(x, y)
                val p2 = img2.getRGB(x, y)
                val lum1 = (0.2126 * p1.r() + 0.7152 * p1.g() + 0.0722 * p1.b()) / 255.0
                val lum2 = (0.2126 * p2.r() + 0.7152 * p2.g() + 0.0722 * p2.b()) / 255.0

                sum1 += lum1
                sum2 += lum2
                sum1sq += lum1 * lum1
                sum2sq += lum2 * lum2
                sum12 += lum1 * lum2
            }
        }

        val mu1 = sum1 / n
        val mu2 = sum2 / n
        val sigma1sq = sum1sq / n - mu1 * mu1
        val sigma2sq = sum2sq / n - mu2 * mu2
        val sigma12 = sum12 / n - mu1 * mu2

        val c1 = 0.01 * 0.01 // (K1*L)^2
        val c2 = 0.03 * 0.03 // (K2*L)^2

        return ((2 * mu1 * mu2 + c1) * (2 * sigma12 + c2)) /
            ((mu1 * mu1 + mu2 * mu2 + c1) * (sigma1sq + sigma2sq + c2))
    }

    /**
     * Generate a diff image showing pixel differences amplified 10x.
     */
    fun diffImage(img1: BufferedImage, img2: BufferedImage): BufferedImage {
        val w = min(img1.width, img2.width)
        val h = min(img1.height, img2.height)
        val diff = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val p1 = img1.getRGB(x, y)
                val p2 = img2.getRGB(x, y)

                val dr = min(255, abs(p1.r() - p2.r()) * 10)
                val dg = min(255, abs(p1.g() - p2.g()) * 10)
                val db = min(255, abs(p1.b() - p2.b()) * 10)

                diff.setRGB(x, y, (0xFF shl 24) or (dr shl 16) or (dg shl 8) or db)
            }
        }

        return diff
    }
}

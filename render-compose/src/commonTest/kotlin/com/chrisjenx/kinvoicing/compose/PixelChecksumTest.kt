package com.chrisjenx.kinvoicing.compose

import com.chrisjenx.kinvoicing.InvoiceFixtures
import kotlin.test.Test
import kotlin.test.assertEquals

// Native Skiko (JVM, iOS, macOS, Linux, Mingw) and Skia wasm produce
// different default byte layouts for the same decoded image, so each
// test source set declares its own baseline value. The wasmJs baseline
// is observed to be identical between `nodejs()` and `browser()` —
// that equivalence is the safety net behind running tests on Node.
internal expect val expectedPixelChecksum: Long

class PixelChecksumTest {

    @Test
    fun decodedLogoDimensionsMatch() {
        val decoded = decodeImageBytes(InvoiceFixtures.TEST_LOGO_PNG)
        assertEquals(200, decoded.intrinsicWidth)
        assertEquals(60, decoded.intrinsicHeight)
        assertEquals(200, decoded.bitmap.width)
        assertEquals(60, decoded.bitmap.height)
    }

    @Test
    fun decodedLogoPixelChecksumMatchesPlatformBaseline() {
        val decoded = decodeImageBytes(InvoiceFixtures.TEST_LOGO_PNG)
        val pixels = IntArray(decoded.bitmap.width * decoded.bitmap.height)
        decoded.bitmap.readPixels(pixels)

        val (checksum, nonTransparent) = hashAndCountOpaque(pixels)

        // Cross-platform invariant: every Skia-backed decoder we tested
        // (JVM, iOS, wasmJs Node, wasmJs Browser) produces 11988 opaque
        // pixels. Drift here means the fixture or decoder changed.
        assertEquals(11988, nonTransparent)

        println("[PixelChecksumTest] checksum=$checksum nonTransparent=$nonTransparent")

        // 0L sentinel means this platform has no recorded baseline yet
        // (e.g. Android needs Robolectric to exercise BitmapFactory).
        if (expectedPixelChecksum == 0L) return
        assertEquals(expectedPixelChecksum, checksum)
    }
}

private fun hashAndCountOpaque(pixels: IntArray): Pair<Long, Int> {
    var hash = -3750763034362895579L // FNV-1a 64-bit offset basis
    var opaque = 0
    for (i in pixels.indices) {
        val p = pixels[i]
        val a = (p ushr 24) and 0xff
        if (a != 0) opaque++
        hash = (hash xor a.toLong()) * 1099511628211L
        hash = (hash xor ((p ushr 16) and 0xff).toLong()) * 1099511628211L
        hash = (hash xor ((p ushr 8) and 0xff).toLong()) * 1099511628211L
        hash = (hash xor (p and 0xff).toLong()) * 1099511628211L
    }
    return hash to opaque
}

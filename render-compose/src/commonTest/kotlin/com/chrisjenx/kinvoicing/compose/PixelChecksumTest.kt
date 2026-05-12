package com.chrisjenx.kinvoicing.compose

import com.chrisjenx.kinvoicing.InvoiceFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Cross-platform validation that Skia (or per-platform decoder) produces
 * deterministic pixel data for [InvoiceFixtures.TEST_LOGO_PNG]. Each
 * target ships its own [expectedPixelChecksum] in its test source set:
 *
 *   - wasmJsTest    -> Skia wasm value (identical in Node and Browser)
 *   - jvmTest       -> Skiko-native value
 *   - nativeTest    -> Skiko-native value (iOS, macOS, Linux, Mingw)
 *   - androidUnitTest -> 0L sentinel (BitmapFactory needs Robolectric;
 *                         test logs and skips when value is 0)
 *
 * Native Skiko and Skia-wasm produce different byte layouts because of
 * differing default ColorType / alpha-premultiplication, but each is
 * internally deterministic. The migration claim that prompted this test
 * is "wasmJs/Node == wasmJs/Browser"; this file's wasmJsTest actual
 * (-5336315741981768721) is what both produce, so the equivalence is
 * locked in.
 */
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

        val checksum = pixelChecksum(pixels)
        val nonTransparentCount = pixels.count { (it ushr 24) != 0 }

        // Sanity: same number of non-transparent pixels on every platform.
        // This held for JVM / iOS / wasmJs Node / wasmJs Browser when we
        // collected the baselines, so it's a meaningful invariant even
        // across decoders with different byte layouts.
        assertEquals(
            11988,
            nonTransparentCount,
            "Logo non-transparent pixel count drifted from the cross-platform invariant.",
        )

        println("[PixelChecksumTest] checksum=$checksum nonTransparent=$nonTransparentCount")

        // Sentinel: 0L means this platform has no recorded baseline yet
        // (e.g. Android needs Robolectric to run BitmapFactory in a unit
        // test). Skip the assertion but still surface the value.
        if (expectedPixelChecksum == 0L) {
            println("[PixelChecksumTest] no baseline recorded for this platform; skipping equality check")
            return
        }
        assertEquals(
            expectedPixelChecksum,
            checksum,
            "Decoded logo pixel checksum drifted from this platform's baseline.",
        )
    }
}

/**
 * Per-platform expected value, declared in each test source set. Native
 * Skiko targets and the Skia wasm build use different default pixel
 * formats, so the baselines differ between equivalence classes.
 */
internal expect val expectedPixelChecksum: Long

/**
 * Deterministic pixel hash. Defined in common code so the hash itself
 * cannot diverge per platform — only its input (decoded pixels) can.
 * 64-bit FNV-1a folded over each ARGB int.
 */
internal fun pixelChecksum(pixels: IntArray): Long {
    var h = -3750763034362895579L // FNV-1a 64-bit offset basis
    for (i in pixels.indices) {
        val p = pixels[i]
        h = (h xor ((p ushr 24) and 0xff).toLong()) * 1099511628211L
        h = (h xor ((p ushr 16) and 0xff).toLong()) * 1099511628211L
        h = (h xor ((p ushr 8) and 0xff).toLong()) * 1099511628211L
        h = (h xor (p and 0xff).toLong()) * 1099511628211L
    }
    return h
}

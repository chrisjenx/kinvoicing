package com.chrisjenx.kinvoicing.compose

import com.chrisjenx.kinvoicing.InvoiceFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageDecoderTest {

    @Test
    fun decodesTestLogoPng() {
        val decoded = decodeImageBytes(InvoiceFixtures.TEST_LOGO_PNG)
        assertEquals(200, decoded.intrinsicWidth, "Expected 200px width for test logo")
        assertEquals(60, decoded.intrinsicHeight, "Expected 60px height for test logo")
    }

    @Test
    fun decodedBitmapIsNonEmpty() {
        val decoded = decodeImageBytes(InvoiceFixtures.TEST_LOGO_PNG)
        assertTrue(decoded.bitmap.width > 0, "Bitmap width should be positive")
        assertTrue(decoded.bitmap.height > 0, "Bitmap height should be positive")
    }
}

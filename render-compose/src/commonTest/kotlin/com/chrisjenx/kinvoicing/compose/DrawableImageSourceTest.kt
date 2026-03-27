package com.chrisjenx.kinvoicing.compose

import com.chrisjenx.kinvoicing.ImageSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DrawableImageSourceTest {

    @Test
    fun bytesImageSourceDefaultContentType() {
        val source = ImageSource.Bytes(byteArrayOf(1, 2, 3), "image/png")
        assertEquals("image/png", source.contentType)
    }

    @Test
    fun bytesImageSourceEquality() {
        val bytes = byteArrayOf(1, 2, 3)
        val a = ImageSource.Bytes(bytes, "image/png")
        val b = ImageSource.Bytes(bytes, "image/png")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun bytesImageSourceInequalityOnContentType() {
        val bytes = byteArrayOf(1, 2, 3)
        val a = ImageSource.Bytes(bytes, "image/png")
        val b = ImageSource.Bytes(bytes, "image/jpeg")
        assertNotEquals(a, b)
    }

    @Test
    fun bytesImageSourceInequalityOnBytes() {
        val a = ImageSource.Bytes(byteArrayOf(1, 2, 3), "image/png")
        val b = ImageSource.Bytes(byteArrayOf(4, 5, 6), "image/png")
        assertNotEquals(a, b)
    }
}

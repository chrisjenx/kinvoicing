package com.chrisjenx.kinvoicing

/**
 * Abstraction for image data used in invoice elements and branding.
 *
 * Core provides [Bytes] for raw byte array images. The render-compose module
 * provides a [org.jetbrains.compose.resources.DrawableResource]-based implementation
 * for CMP resource images.
 */
public abstract class ImageSource {

    /** Raw image bytes. All implementations must provide bytes for cross-renderer compatibility. */
    public abstract val bytes: ByteArray

    /** MIME content type (e.g., "image/png", "image/jpeg"). */
    public abstract val contentType: String

    /** Image source backed by raw bytes. */
    public class Bytes(
        override val bytes: ByteArray,
        override val contentType: String,
    ) : ImageSource() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Bytes) return false
            return bytes.contentEquals(other.bytes) && contentType == other.contentType
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + contentType.hashCode()
            return result
        }

        override fun toString(): String = "ImageSource.Bytes(${bytes.size} bytes, $contentType)"
    }
}

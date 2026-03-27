package com.chrisjenx.kinvoicing.compose

import com.chrisjenx.kinvoicing.ImageSource
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getDrawableResourceBytes
import org.jetbrains.compose.resources.getSystemResourceEnvironment

/**
 * Image source backed by a CMP [DrawableResource].
 *
 * At Compose render time, the renderer uses `painterResource()` for efficient
 * native rendering. For non-Compose renderers (HTML email, PDF), [bytes] provides
 * the raw image data eagerly loaded from the resource.
 */
@OptIn(ExperimentalResourceApi::class)
public class DrawableImageSource(
    public val resource: DrawableResource,
    override val contentType: String = "image/png",
) : ImageSource() {

    override val bytes: ByteArray by lazy {
        runBlockingCompat {
            val environment = getSystemResourceEnvironment()
            getDrawableResourceBytes(environment, resource)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DrawableImageSource) return false
        return resource == other.resource && contentType == other.contentType
    }

    override fun hashCode(): Int {
        var result = resource.hashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }

    override fun toString(): String = "DrawableImageSource(resource=$resource, $contentType)"
}

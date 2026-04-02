package com.chrisjenx.kinvoicing.compose

import com.chrisjenx.kinvoicing.builders.BrandIdentityBuilder
import com.chrisjenx.kinvoicing.builders.CustomBuilder
import org.jetbrains.compose.resources.DrawableResource

/** Set the brand logo from a CMP drawable resource. */
public fun BrandIdentityBuilder.logo(
    resource: DrawableResource,
    contentType: String = "image/png",
) {
    logo(DrawableImageSource(resource, contentType))
}

/** Add an inline image from a CMP drawable resource. */
public fun CustomBuilder.image(
    resource: DrawableResource,
    contentType: String = "image/png",
    width: Int? = null,
    height: Int? = null,
) {
    image(DrawableImageSource(resource, contentType), width, height)
}

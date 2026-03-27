package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.util.requireSafeUrl

/** DSL builder for [Branding]. Requires at least a [primary] identity. */
@InvoiceDsl
public class BrandingBuilder {
    private var primary: BrandIdentity? = null
    private var poweredBy: BrandIdentity? = null
    /** Controls where primary and powered-by brands are placed. Defaults to [BrandLayout.POWERED_BY_FOOTER]. */
    public var layout: BrandLayout = BrandLayout.POWERED_BY_FOOTER

    /** Configure the primary brand identity (required). */
    public fun primary(init: BrandIdentityBuilder.() -> Unit) {
        primary = BrandIdentityBuilder().apply(init).build()
    }

    /** Configure an optional secondary "powered by" brand identity. */
    public fun poweredBy(init: BrandIdentityBuilder.() -> Unit) {
        poweredBy = BrandIdentityBuilder().apply(init).build()
    }

    internal fun build(): Branding = Branding(
        primary = requireNotNull(primary) { "Branding requires a primary identity" },
        poweredBy = poweredBy,
        layout = layout,
    )
}

/** DSL builder for a [BrandIdentity]. Requires at least a [name]. */
@InvoiceDsl
public class BrandIdentityBuilder {
    private var name: String? = null
    private var logo: ImageSource? = null
    private var address: List<String> = emptyList()
    private var email: String? = null
    private var phone: String? = null
    private var website: String? = null
    private var tagline: String? = null

    /** Set the brand name (required). */
    public fun name(value: String) { name = value }
    /** Set the brand logo from raw image bytes with the given MIME [contentType]. */
    public fun logo(data: ByteArray, contentType: String = "image/png") {
        logo = ImageSource.Bytes(data, contentType)
    }
    /** Set the brand logo from an [ImageSource]. */
    public fun logo(source: ImageSource) {
        logo = source
    }
    /** Set the brand address as one or more lines. */
    public fun address(vararg lines: String) { address = lines.toList() }
    /** Set the brand contact email. */
    public fun email(value: String) { email = value }
    /** Set the brand contact phone number. */
    public fun phone(value: String) { phone = value }
    /** Set the brand website URL. */
    public fun website(value: String) { website = requireSafeUrl(value, "website") }
    /** Set a tagline or subtitle (e.g., "Powered by Acme Payments"). */
    public fun tagline(value: String) { tagline = value }

    internal fun build(): BrandIdentity = BrandIdentity(
        name = requireNotNull(name) { "BrandIdentity requires a name" },
        logo = logo,
        address = address,
        email = email,
        phone = phone,
        website = website,
        tagline = tagline,
    )
}

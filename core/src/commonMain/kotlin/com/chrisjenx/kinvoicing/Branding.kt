package com.chrisjenx.kinvoicing

/**
 * Branding configuration for the invoice header.
 *
 * @property primary The main brand shown on the invoice.
 * @property poweredBy Optional secondary brand (e.g., a payment processor).
 * @property layout Controls where the primary and powered-by brands appear.
 */
public data class Branding(
    val primary: BrandIdentity,
    val poweredBy: BrandIdentity? = null,
    val layout: BrandLayout = BrandLayout.POWERED_BY_FOOTER,
)

/**
 * Visual and contact identity for a brand.
 *
 * @property logo Image source for the brand logo, or null if none.
 */
public data class BrandIdentity(
    val name: String,
    val logo: ImageSource? = null,
    val address: List<String> = emptyList(),
    val email: String? = null,
    val phone: String? = null,
    val website: String? = null,
    val tagline: String? = null,
)

/** Controls the placement of primary and secondary brand identities in the header. */
public enum class BrandLayout {
    /** Primary brand in header; powered-by brand in footer. */
    POWERED_BY_FOOTER,
    /** Primary brand in header; powered-by brand in header subtitle. */
    POWERED_BY_HEADER,
    /** Both brands displayed side-by-side in the header. */
    DUAL_HEADER,
}

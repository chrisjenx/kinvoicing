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
 * @property logo Raw image bytes for the brand logo.
 * @property logoContentType MIME type of [logo] (e.g., "image/png").
 */
public data class BrandIdentity(
    val name: String,
    val logo: ByteArray? = null,
    val logoContentType: String? = null,
    val address: List<String> = emptyList(),
    val email: String? = null,
    val phone: String? = null,
    val website: String? = null,
    val tagline: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BrandIdentity) return false
        return name == other.name &&
            logo.contentEquals(other.logo) &&
            logoContentType == other.logoContentType &&
            address == other.address &&
            email == other.email &&
            phone == other.phone &&
            website == other.website &&
            tagline == other.tagline
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (logo?.contentHashCode() ?: 0)
        result = 31 * result + (logoContentType?.hashCode() ?: 0)
        result = 31 * result + address.hashCode()
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (phone?.hashCode() ?: 0)
        result = 31 * result + (website?.hashCode() ?: 0)
        result = 31 * result + (tagline?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = buildString {
        append("BrandIdentity(name=").append(name)
        append(", logo=").append(logo?.let { "[${it.size} bytes]" } ?: "null")
        append(", logoContentType=").append(logoContentType)
        append(", address=").append(address)
        append(", email=").append(email)
        append(", phone=").append(phone)
        append(", website=").append(website)
        append(", tagline=").append(tagline)
        append(")")
    }
}

/** Controls the placement of primary and secondary brand identities in the header. */
public enum class BrandLayout {
    /** Primary brand in header; powered-by brand in footer. */
    POWERED_BY_FOOTER,
    /** Primary brand in header; powered-by brand in header subtitle. */
    POWERED_BY_HEADER,
    /** Both brands displayed side-by-side in the header. */
    DUAL_HEADER,
}

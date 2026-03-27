package com.chrisjenx.kinvoicing.util

private val SAFE_URL_SCHEMES = setOf("http", "https", "mailto", "tel")
private val FORBIDDEN_FONT_CHARS = setOf(';', '{', '}', '<', '>', '"', '\'', '(', ')', '\\')

/**
 * Validates that [url] uses a safe scheme (http, https, mailto, tel) or is a relative URL.
 * Throws [IllegalArgumentException] for dangerous schemes like javascript: or data:.
 */
public fun requireSafeUrl(url: String, fieldName: String): String {
    val colonIdx = url.indexOf(':')
    if (colonIdx < 0) return url // relative URL, safe
    val scheme = url.substring(0, colonIdx).lowercase().trim()
    require(scheme in SAFE_URL_SCHEMES) {
        "$fieldName contains unsafe URL scheme '$scheme'. Only http, https, mailto, and tel are allowed."
    }
    return url
}

/**
 * Validates that [value] is a safe CSS font-family name with no injection characters.
 * Throws [IllegalArgumentException] if the value contains characters that could break out of CSS.
 */
internal fun sanitizeFontFamily(value: String): String {
    require(FORBIDDEN_FONT_CHARS.none { it in value }) {
        "fontFamily contains forbidden character. Only alphanumeric, space, and hyphen are allowed."
    }
    return value
}

/**
 * Validates that [value] is a finite number (not NaN or Infinity).
 * Throws [IllegalArgumentException] for non-finite values.
 */
internal fun requireFinite(value: Double, fieldName: String): Double {
    require(!value.isNaN() && !value.isInfinite()) {
        "$fieldName must be a finite number, got $value"
    }
    return value
}

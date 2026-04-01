package com.chrisjenx.kinvoicing

/**
 * Contact information for an invoice party (sender or recipient).
 *
 * @property name The party's display name (required).
 * @property address Mailing address as individual lines.
 * @property email Contact email address.
 * @property phone Contact phone number.
 */
public data class ContactInfo(
    val name: String,
    val address: List<String> = emptyList(),
    val email: String? = null,
    val phone: String? = null,
)

package com.chrisjenx.kinvoicing.builders

import com.chrisjenx.kinvoicing.InvoiceDsl
import com.chrisjenx.kinvoicing.InvoiceSection

/**
 * DSL builder for [InvoiceSection.Custom] sections composed of inline content elements.
 *
 * Inherits the full [ContentBuilder] surface — `text()`, `link()`, `button()`, `spacer()`,
 * `divider()`, `image()`, `row()`.
 *
 * ```kotlin
 * custom("terms-banner") {
 *     text("Special terms apply", styleRef = "bold")
 *     divider()
 *     link("Read more", "https://example.com/terms")
 * }
 * ```
 */
@InvoiceDsl
public class CustomBuilder(private val key: String) : ContentBuilder() {
    internal fun buildSection(): InvoiceSection.Custom = InvoiceSection.Custom(
        key = key,
        content = build(),
    )
}

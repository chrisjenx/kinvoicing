package com.chrisjenx.kinvoicing.html.email

/**
 * Configuration for [HtmlRenderer] output.
 *
 * @property embedImages When true, images are base64-encoded inline; when false, omitted.
 * @property includeDoctype When true, prepend `<!DOCTYPE html>` to the output.
 * @property wrapInBody When true, wrap the invoice in full `<html><body>` structure.
 */
public data class HtmlRenderConfig(
    val embedImages: Boolean = true,
    val includeDoctype: Boolean = true,
    val wrapInBody: Boolean = true,
)

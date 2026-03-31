package com.chrisjenx.kinvoicing.composehtml

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Collector for element annotations during PDF/HTML rendering.
 * Set via [LocalPdfElementCollector] during renderToPdf/renderToHtml calls.
 *
 * Generalizes [PdfLinkCollector] to support tables, lists, buttons, text fields,
 * images, and hover annotations.
 */
public class PdfElementCollector {
    private val _elements = mutableListOf<PdfElementAnnotation>()
    public val elements: List<PdfElementAnnotation> get() = _elements

    private var nextId = 0

    public fun add(annotation: PdfElementAnnotation) {
        _elements.add(annotation)
    }

    public fun clear() {
        _elements.clear()
        nextId = 0
    }

    public fun generateId(): String = "pdf-elem-${nextId++}"
}

/**
 * CompositionLocal providing access to the element collector during PDF/HTML rendering.
 * When rendering to screen (not PDF/HTML), this is null and element annotations are ignored.
 */
public val LocalPdfElementCollector: ProvidableCompositionLocal<PdfElementCollector?> = compositionLocalOf { null }

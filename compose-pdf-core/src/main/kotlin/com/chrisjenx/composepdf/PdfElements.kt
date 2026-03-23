package com.chrisjenx.composepdf

/**
 * Base class for all PDF/HTML element annotations.
 *
 * Each annotation carries the bounds (in PDF points, Compose Y-down coordinate space)
 * of the annotated region, plus structured content specific to the element type.
 * Collected via [PdfElementCollector] during rendering.
 */
sealed class PdfElementAnnotation {
    /** Left edge in PDF points. */
    abstract val x: Float
    /** Top edge in PDF points (Y-down). */
    abstract val y: Float
    /** Width in PDF points. */
    abstract val width: Float
    /** Height in PDF points. */
    abstract val height: Float
    /** Unique ID for region matching. */
    abstract val id: String
}

// --- Table ---

data class PdfTableAnnotation(
    val rows: List<PdfTableRowAnnotation>,
    val caption: String? = null,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String = "",
) : PdfElementAnnotation()

data class PdfTableRowAnnotation(
    val cells: List<PdfTableCellAnnotation>,
    val isHeader: Boolean = false,
)

data class PdfTableCellAnnotation(
    val text: String,
    val colSpan: Int = 1,
    val rowSpan: Int = 1,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
)

// --- List ---

data class PdfListAnnotation(
    val ordered: Boolean,
    val items: List<PdfListItemAnnotation>,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String = "",
) : PdfElementAnnotation()

data class PdfListItemAnnotation(
    val text: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
)

// --- Button ---

data class PdfButtonAnnotation(
    val label: String,
    /** AcroForm field name. */
    val name: String,
    /** JavaScript action (for PDF) or onclick handler (for HTML). */
    val onClick: String? = null,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String = "",
) : PdfElementAnnotation()

// --- TextField ---

data class PdfTextFieldAnnotation(
    val name: String,
    val placeholder: String = "",
    val value: String = "",
    val multiline: Boolean = false,
    val maxLength: Int = 0,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String = "",
) : PdfElementAnnotation()

// --- Image with alt text ---

data class PdfImageAnnotation(
    val altText: String,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String = "",
) : PdfElementAnnotation()

// --- Hover (HTML-only, no-op in PDF) ---

data class PdfHoverAnnotation(
    val hoverStyles: HoverStyles,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String = "",
) : PdfElementAnnotation()

data class HoverStyles(
    val backgroundColor: String? = null,
    val opacity: Float? = null,
    val scale: Float? = null,
    val cursor: String = "pointer",
    val customCss: Map<String, String> = emptyMap(),
)

// --- Checkbox ---

data class PdfCheckboxAnnotation(
    val name: String,
    val label: String,
    val checked: Boolean = false,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String = "",
) : PdfElementAnnotation()

// --- RadioButton ---

data class PdfRadioButtonAnnotation(
    val name: String,
    val value: String,
    val groupName: String,
    val selected: Boolean = false,
    val label: String = "",
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String = "",
) : PdfElementAnnotation()

// --- Select (dropdown) ---

data class PdfSelectAnnotation(
    val name: String,
    val options: List<PdfSelectOption>,
    val selectedValue: String = "",
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String = "",
) : PdfElementAnnotation()

data class PdfSelectOption(val value: String, val label: String)

// --- Slider (range) ---

data class PdfSliderAnnotation(
    val name: String,
    val min: Float = 0f,
    val max: Float = 100f,
    val value: Float = 0f,
    val step: Float = 1f,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String = "",
) : PdfElementAnnotation()

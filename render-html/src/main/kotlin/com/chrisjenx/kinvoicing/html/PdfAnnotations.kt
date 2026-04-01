package com.chrisjenx.kinvoicing.html

internal sealed interface PdfElementAnnotation {
    val x: Float
    val y: Float
    val width: Float
    val height: Float
    val id: String
}

internal data class PdfButtonAnnotation(
    val label: String,
    val name: String,
    val onClick: String?,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

internal data class PdfTextFieldAnnotation(
    val name: String,
    val placeholder: String,
    val value: String,
    val multiline: Boolean,
    val maxLength: Int,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

internal data class PdfImageAnnotation(
    val altText: String,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

internal data class PdfHoverAnnotation(
    val hoverStyles: HoverStyles,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

internal data class PdfTableAnnotation(
    val rows: List<PdfTableRowAnnotation>,
    val caption: String?,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

internal data class PdfTableRowAnnotation(
    val cells: List<PdfTableCellAnnotation>,
    val isHeader: Boolean,
)

internal data class PdfTableCellAnnotation(
    val text: String,
    val colSpan: Int = 1,
    val rowSpan: Int = 1,
)

internal data class PdfListAnnotation(
    val ordered: Boolean,
    val items: List<PdfListItemAnnotation>,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

internal data class PdfListItemAnnotation(
    val text: String,
)

public data class HoverStyles(
    val backgroundColor: String? = null,
    val opacity: Float? = null,
    val transform: String? = null,
    val boxShadow: String? = null,
    val cursor: String? = null,
)

package com.chrisjenx.kinvoicing.composehtml

public sealed interface PdfElementAnnotation {
    public val x: Float
    public val y: Float
    public val width: Float
    public val height: Float
    public val id: String
}

public data class PdfButtonAnnotation(
    val label: String,
    val name: String,
    val onClick: String?,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

public data class PdfTextFieldAnnotation(
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

public data class PdfImageAnnotation(
    val altText: String,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

public data class PdfHoverAnnotation(
    val hoverStyles: HoverStyles,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

public data class PdfTableAnnotation(
    val rows: List<PdfTableRowAnnotation>,
    val caption: String?,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

public data class PdfTableRowAnnotation(
    val cells: List<PdfTableCellAnnotation>,
    val isHeader: Boolean,
)

public data class PdfTableCellAnnotation(
    val text: String,
    val colSpan: Int = 1,
    val rowSpan: Int = 1,
)

public data class PdfListAnnotation(
    val ordered: Boolean,
    val items: List<PdfListItemAnnotation>,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

public data class PdfListItemAnnotation(
    val text: String,
)

public data class HoverStyles(
    val backgroundColor: String? = null,
    val opacity: Float? = null,
    val transform: String? = null,
    val boxShadow: String? = null,
    val cursor: String? = null,
)

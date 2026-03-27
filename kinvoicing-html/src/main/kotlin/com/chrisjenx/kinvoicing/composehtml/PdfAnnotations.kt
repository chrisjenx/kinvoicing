package com.chrisjenx.kinvoicing.composehtml

sealed interface PdfElementAnnotation {
    val x: Float
    val y: Float
    val width: Float
    val height: Float
    val id: String
}

data class PdfButtonAnnotation(
    val label: String,
    val name: String,
    val onClick: String?,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

data class PdfTextFieldAnnotation(
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

data class PdfImageAnnotation(
    val altText: String,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

data class PdfHoverAnnotation(
    val hoverStyles: HoverStyles,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

data class PdfTableAnnotation(
    val rows: List<PdfTableRowAnnotation>,
    val caption: String?,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

data class PdfTableRowAnnotation(
    val cells: List<PdfTableCellAnnotation>,
    val isHeader: Boolean,
)

data class PdfTableCellAnnotation(
    val text: String,
    val colSpan: Int = 1,
    val rowSpan: Int = 1,
)

data class PdfListAnnotation(
    val ordered: Boolean,
    val items: List<PdfListItemAnnotation>,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

data class PdfListItemAnnotation(
    val text: String,
)

data class PdfCheckboxAnnotation(
    val name: String,
    val checked: Boolean,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

data class PdfRadioButtonAnnotation(
    val name: String,
    val group: String,
    val selected: Boolean,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

data class PdfSelectAnnotation(
    val name: String,
    val options: List<PdfSelectOption>,
    val selectedIndex: Int,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

data class PdfSelectOption(
    val label: String,
    val value: String,
)

data class PdfSliderAnnotation(
    val name: String,
    val min: Float,
    val max: Float,
    val value: Float,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val id: String,
) : PdfElementAnnotation

data class HoverStyles(
    val backgroundColor: String? = null,
    val opacity: Float? = null,
    val transform: String? = null,
    val boxShadow: String? = null,
    val cursor: String? = null,
)

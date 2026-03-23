@file:OptIn(InternalComposeUiApi::class)

package com.chrisjenx.composepdf.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.material.ProvideTextStyle
import androidx.compose.ui.text.TextStyle
import com.chrisjenx.composepdf.LocalPdfElementCollector
import com.chrisjenx.composepdf.LocalPdfLinkCollector
import com.chrisjenx.composepdf.PdfButtonAnnotation
import com.chrisjenx.composepdf.PdfCheckboxAnnotation
import com.chrisjenx.composepdf.PdfElementAnnotation
import com.chrisjenx.composepdf.PdfElementCollector
import com.chrisjenx.composepdf.PdfFontFamily
import com.chrisjenx.composepdf.PdfLinkAnnotation
import com.chrisjenx.composepdf.PdfLinkCollector
import com.chrisjenx.composepdf.PdfPageConfig
import com.chrisjenx.composepdf.PdfRadioButtonAnnotation
import com.chrisjenx.composepdf.PdfSelectAnnotation
import com.chrisjenx.composepdf.PdfTextFieldAnnotation
import com.chrisjenx.composepdf.RenderMode
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox
import org.apache.pdfbox.pdmodel.interactive.form.PDComboBox
import org.apache.pdfbox.pdmodel.interactive.form.PDPushButton
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

internal object PdfRenderer {

    fun renderSinglePage(
        config: PdfPageConfig,
        density: Density,
        mode: RenderMode,
        useBundledFont: Boolean,
        content: @Composable () -> Unit,
    ): ByteArray {
        return renderMultiPage(
            pageCount = 1,
            config = config,
            density = density,
            mode = mode,
            useBundledFont = useBundledFont,
            content = { content() },
        )
    }

    fun renderMultiPage(
        pageCount: Int,
        config: PdfPageConfig,
        density: Density,
        mode: RenderMode,
        useBundledFont: Boolean,
        content: @Composable (pageIndex: Int) -> Unit,
    ): ByteArray {
        require(pageCount > 0) { "pageCount must be positive, was $pageCount" }
        return when (mode) {
            RenderMode.VECTOR -> renderVector(pageCount, config, density, useBundledFont, content)
            RenderMode.RASTER -> renderRaster(pageCount, config, density, useBundledFont, content)
        }
    }

    // --- Vector path: Compose → PictureRecorder → SVGCanvas → SVG → PDF ---

    private fun renderVector(
        pageCount: Int,
        config: PdfPageConfig,
        density: Density,
        useBundledFont: Boolean,
        content: @Composable (pageIndex: Int) -> Unit,
    ): ByteArray {
        val pxW = (config.contentWidth.value * density.density).toInt()
        val pxH = (config.contentHeight.value * density.density).toInt()

        val pdfDoc = PDDocument()
        val fontCache = mutableMapOf<String, PDFont>()
        try {
            for (pageIndex in 0 until pageCount) {
                val linkCollector = PdfLinkCollector()
                val elementCollector = PdfElementCollector()
                val svg = ComposeToSvg.render(pxW, pxH, density) {
                    CompositionLocalProvider(
                        LocalPdfLinkCollector provides linkCollector,
                        LocalPdfElementCollector provides elementCollector,
                    ) {
                        if (useBundledFont) {
                            ProvideTextStyle(TextStyle(fontFamily = PdfFontFamily)) {
                                content(pageIndex)
                            }
                        } else {
                            content(pageIndex)
                        }
                    }
                }
                SvgToPdfConverter.addPage(pdfDoc, svg, config.width.value, config.height.value, fontCache)
                addLinkAnnotations(pdfDoc.getPage(pageIndex), config, linkCollector.links)
                addFormFields(pdfDoc, pdfDoc.getPage(pageIndex), config, elementCollector.elements)
            }
            val baos = ByteArrayOutputStream()
            pdfDoc.save(baos)
            return baos.toByteArray()
        } finally {
            pdfDoc.close()
        }
    }

    // --- Raster path: Compose → ImageComposeScene → bitmap → PDF ---

    private fun renderRaster(
        pageCount: Int,
        config: PdfPageConfig,
        density: Density,
        useBundledFont: Boolean,
        content: @Composable (pageIndex: Int) -> Unit,
    ): ByteArray {
        val contentWidthPx = (config.contentWidth.value * density.density).toInt()
        val contentHeightPx = (config.contentHeight.value * density.density).toInt()

        val doc = PDDocument()
        try {
            for (pageIndex in 0 until pageCount) {
                val linkCollector = PdfLinkCollector()
                val elementCollector = PdfElementCollector()
                val bitmap = renderComposeToBitmap(
                    width = contentWidthPx,
                    height = contentHeightPx,
                    density = density,
                    content = {
                        CompositionLocalProvider(
                            LocalPdfLinkCollector provides linkCollector,
                            LocalPdfElementCollector provides elementCollector,
                        ) {
                            if (useBundledFont) {
                                ProvideTextStyle(TextStyle(fontFamily = PdfFontFamily)) {
                                    content(pageIndex)
                                }
                            } else {
                                content(pageIndex)
                            }
                        }
                    },
                )
                addBitmapPage(doc, config, bitmap)
                addLinkAnnotations(doc.getPage(pageIndex), config, linkCollector.links)
                addFormFields(doc, doc.getPage(pageIndex), config, elementCollector.elements)
            }
            val baos = ByteArrayOutputStream()
            doc.save(baos)
            return baos.toByteArray()
        } finally {
            doc.close()
        }
    }

    private fun renderComposeToBitmap(
        width: Int,
        height: Int,
        density: Density,
        content: @Composable () -> Unit,
    ): BufferedImage {
        val scene = ImageComposeScene(
            width = width,
            height = height,
            density = density,
            content = content,
        )
        try {
            val image = scene.render()
            return skiaImageToBufferedImage(image)
        } finally {
            scene.close()
        }
    }

    private fun skiaImageToBufferedImage(image: org.jetbrains.skia.Image): BufferedImage {
        val data = image.encodeToData() ?: error("Failed to encode Skia Image to PNG")
        return ImageIO.read(ByteArrayInputStream(data.bytes))
    }

    private fun addBitmapPage(
        doc: PDDocument,
        config: PdfPageConfig,
        bitmap: BufferedImage,
    ) {
        val mediaBox = PDRectangle(config.width.value, config.height.value)
        val page = PDPage(mediaBox)
        doc.addPage(page)

        val pdImage = LosslessFactory.createFromImage(doc, bitmap)
        val contentStream = PDPageContentStream(doc, page)
        try {
            contentStream.drawImage(
                pdImage,
                config.margins.left.value,
                config.margins.bottom.value,
                config.contentWidth.value,
                config.contentHeight.value,
            )
        } finally {
            contentStream.close()
        }
    }

    // --- Link annotations ---

    private fun addLinkAnnotations(
        page: PDPage,
        config: PdfPageConfig,
        links: List<PdfLinkAnnotation>,
    ) {
        if (links.isEmpty()) return
        val pageHeight = config.height.value
        val marginLeft = config.margins.left.value
        val marginTop = config.margins.top.value

        for (link in links) {
            val annotation = PDAnnotationLink()
            val action = PDActionURI()
            action.uri = link.href
            annotation.action = action

            // Convert from Compose coordinates (Y-down from content origin)
            // to PDF coordinates (Y-up from page origin)
            val llx = marginLeft + link.x
            val lly = pageHeight - marginTop - link.y - link.height
            annotation.rectangle = PDRectangle(llx, lly, link.width, link.height)

            // Invisible border (the content provides visual styling)
            val borderStyle = PDBorderStyleDictionary()
            borderStyle.width = 0f
            annotation.borderStyle = borderStyle

            page.annotations.add(annotation)
        }
    }

    // --- Form fields (buttons, text fields) ---

    private fun addFormFields(
        doc: PDDocument,
        page: PDPage,
        config: PdfPageConfig,
        elements: List<PdfElementAnnotation>,
    ) {
        val formElements = elements.filter {
            it is PdfButtonAnnotation || it is PdfTextFieldAnnotation ||
                it is PdfCheckboxAnnotation || it is PdfRadioButtonAnnotation ||
                it is PdfSelectAnnotation
        }
        if (formElements.isEmpty()) return

        val acroForm = doc.documentCatalog.acroForm ?: PDAcroForm(doc).also {
            val resources = org.apache.pdfbox.pdmodel.PDResources()
            val helvetica = org.apache.pdfbox.pdmodel.font.PDType1Font(
                org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA
            )
            resources.put(org.apache.pdfbox.cos.COSName.getPDFName("Helv"), helvetica)
            it.defaultResources = resources
            it.defaultAppearance = "/Helv 12 Tf 0 g"
            doc.documentCatalog.acroForm = it
        }

        val pageHeight = config.height.value
        val marginLeft = config.margins.left.value
        val marginTop = config.margins.top.value

        for (elem in formElements) {
            val llx = marginLeft + elem.x
            val lly = pageHeight - marginTop - elem.y - elem.height
            val rect = PDRectangle(llx, lly, elem.width, elem.height)

            when (elem) {
                is PdfButtonAnnotation -> {
                    val widget = PDAnnotationWidget()
                    widget.rectangle = rect
                    widget.page = page

                    val pushButton = PDPushButton(acroForm)
                    pushButton.partialName = elem.name
                    pushButton.widgets = listOf(widget)

                    if (elem.onClick != null) {
                        widget.action = PDActionJavaScript(elem.onClick)
                    }

                    acroForm.fields.add(pushButton)
                    page.annotations.add(widget)
                }

                is PdfTextFieldAnnotation -> {
                    val widget = PDAnnotationWidget()
                    widget.rectangle = rect
                    widget.page = page

                    val textField = PDTextField(acroForm)
                    textField.partialName = elem.name
                    if (elem.value.isNotEmpty()) textField.value = elem.value
                    if (elem.maxLength > 0) textField.maxLen = elem.maxLength
                    textField.isMultiline = elem.multiline
                    textField.widgets = listOf(widget)

                    acroForm.fields.add(textField)
                    page.annotations.add(widget)
                }

                is PdfCheckboxAnnotation -> {
                    val widget = PDAnnotationWidget()
                    widget.rectangle = rect
                    widget.page = page

                    val checkbox = PDCheckBox(acroForm)
                    checkbox.partialName = elem.name
                    checkbox.widgets = listOf(widget)
                    if (elem.checked) checkbox.check()

                    acroForm.fields.add(checkbox)
                    page.annotations.add(widget)
                }

                is PdfRadioButtonAnnotation -> {
                    // Group radio buttons by groupName — find or create the PDRadioButton field
                    val existingField = acroForm.fields.find {
                        it is PDRadioButton && it.partialName == elem.groupName
                    } as? PDRadioButton

                    val widget = PDAnnotationWidget()
                    widget.rectangle = rect
                    widget.page = page
                    // Set up widget appearance dict with the value as the "on" state
                    val apNDict = org.apache.pdfbox.cos.COSDictionary()
                    apNDict.setItem(
                        org.apache.pdfbox.cos.COSName.getPDFName(elem.value),
                        org.apache.pdfbox.cos.COSStream(),
                    )
                    apNDict.setItem(org.apache.pdfbox.cos.COSName.OFF, org.apache.pdfbox.cos.COSStream())
                    val apDict = org.apache.pdfbox.cos.COSDictionary()
                    apDict.setItem(org.apache.pdfbox.cos.COSName.N, apNDict)
                    widget.cosObject.setItem(org.apache.pdfbox.cos.COSName.AP, apDict)

                    if (existingField != null) {
                        val widgets = existingField.widgets.toMutableList()
                        widgets.add(widget)
                        existingField.widgets = widgets
                        val exportVals = existingField.exportValues.toMutableList()
                        exportVals.add(elem.value)
                        existingField.exportValues = exportVals
                    } else {
                        val radioButton = PDRadioButton(acroForm)
                        radioButton.partialName = elem.groupName
                        radioButton.widgets = listOf(widget)
                        radioButton.exportValues = listOf(elem.value)
                        acroForm.fields.add(radioButton)
                    }

                    if (elem.selected) {
                        val radioField = (acroForm.fields.find {
                            it is PDRadioButton && it.partialName == elem.groupName
                        } as? PDRadioButton)
                        radioField?.setValue(elem.value)
                    }

                    page.annotations.add(widget)
                }

                is PdfSelectAnnotation -> {
                    val widget = PDAnnotationWidget()
                    widget.rectangle = rect
                    widget.page = page

                    val comboBox = PDComboBox(acroForm)
                    comboBox.partialName = elem.name
                    comboBox.setOptions(
                        elem.options.map { it.value },
                        elem.options.map { it.label },
                    )
                    if (elem.selectedValue.isNotEmpty()) comboBox.setValue(elem.selectedValue)
                    comboBox.widgets = listOf(widget)

                    acroForm.fields.add(comboBox)
                    page.annotations.add(widget)
                }

                else -> {}
            }
        }
    }
}

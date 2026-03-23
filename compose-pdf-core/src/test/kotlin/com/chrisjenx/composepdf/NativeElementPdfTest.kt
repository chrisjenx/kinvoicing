@file:OptIn(InternalComposeUiApi::class)

package com.chrisjenx.composepdf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox
import org.apache.pdfbox.pdmodel.interactive.form.PDComboBox
import org.apache.pdfbox.pdmodel.interactive.form.PDPushButton
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NativeElementPdfTest {

    @Test
    fun `PdfButton creates AcroForm push button in PDF`() {
        val pdfBytes = renderToPdf {
            Column(Modifier.padding(24.dp)) {
                PdfButton(label = "Submit", name = "submit-btn") {
                    Box(
                        Modifier.width(120.dp).height(40.dp)
                            .background(Color.Blue)
                    ) {
                        Text("Submit")
                    }
                }
            }
        }

        Loader.loadPDF(pdfBytes).use { doc ->
            val acroForm = doc.documentCatalog.acroForm
            assertNotNull(acroForm, "PDF should have an AcroForm")
            val fields = acroForm.fields
            assertTrue(fields.isNotEmpty(), "AcroForm should have fields")
            val button = fields.filterIsInstance<PDPushButton>().firstOrNull()
            assertNotNull(button, "Should have a push button field")
            assertEquals("submit-btn", button.partialName)
        }
    }

    @Test
    fun `PdfTextField creates AcroForm text field in PDF`() {
        val pdfBytes = renderToPdf {
            Column(Modifier.padding(24.dp)) {
                PdfTextField(
                    name = "email",
                    placeholder = "Enter email",
                    value = "test@example.com",
                ) {
                    Box(Modifier.width(200.dp).height(30.dp).background(Color.White))
                }
            }
        }

        Loader.loadPDF(pdfBytes).use { doc ->
            val acroForm = doc.documentCatalog.acroForm
            assertNotNull(acroForm, "PDF should have an AcroForm")
            val fields = acroForm.fields
            assertTrue(fields.isNotEmpty(), "AcroForm should have fields")
            val textField = fields.filterIsInstance<PDTextField>().firstOrNull()
            assertNotNull(textField, "Should have a text field")
            assertEquals("email", textField.partialName)
            assertEquals("test@example.com", textField.value)
        }
    }

    @Test
    fun `PdfTextField multiline creates multiline field in PDF`() {
        val pdfBytes = renderToPdf {
            Column(Modifier.padding(24.dp)) {
                PdfTextField(
                    name = "notes",
                    multiline = true,
                    maxLength = 500,
                ) {
                    Box(Modifier.width(200.dp).height(100.dp).background(Color.White))
                }
            }
        }

        Loader.loadPDF(pdfBytes).use { doc ->
            val acroForm = doc.documentCatalog.acroForm
            assertNotNull(acroForm)
            val textField = acroForm.fields.filterIsInstance<PDTextField>().firstOrNull()
            assertNotNull(textField)
            assertEquals("notes", textField.partialName)
            assertTrue(textField.isMultiline, "Field should be multiline")
            assertEquals(500, textField.maxLen)
        }
    }

    @Test
    fun `multiple form fields in single PDF`() {
        val pdfBytes = renderToPdf {
            Column(Modifier.padding(24.dp)) {
                PdfTextField(name = "name") {
                    Box(Modifier.width(200.dp).height(30.dp))
                }
                PdfTextField(name = "email") {
                    Box(Modifier.width(200.dp).height(30.dp))
                }
                PdfButton(label = "Send", name = "send-btn") {
                    Box(Modifier.width(100.dp).height(36.dp))
                }
            }
        }

        Loader.loadPDF(pdfBytes).use { doc ->
            val acroForm = doc.documentCatalog.acroForm
            assertNotNull(acroForm)
            assertEquals(3, acroForm.fields.size, "Should have 3 form fields")
            assertEquals(2, acroForm.fields.filterIsInstance<PDTextField>().size)
            assertEquals(1, acroForm.fields.filterIsInstance<PDPushButton>().size)
        }
    }

    @Test
    fun `PdfCheckbox creates AcroForm checkbox in PDF`() {
        val pdfBytes = renderToPdf {
            Column(Modifier.padding(24.dp)) {
                PdfCheckbox(name = "agree", label = "I agree", checked = true) {
                    Box(Modifier.width(200.dp).height(24.dp).background(Color.White))
                }
            }
        }

        Loader.loadPDF(pdfBytes).use { doc ->
            val acroForm = doc.documentCatalog.acroForm
            assertNotNull(acroForm, "PDF should have an AcroForm")
            val checkbox = acroForm.fields.filterIsInstance<PDCheckBox>().firstOrNull()
            assertNotNull(checkbox, "Should have a checkbox field")
            assertEquals("agree", checkbox.partialName)
            assertTrue(checkbox.isChecked, "Checkbox should be checked")
        }
    }

    @Test
    fun `PdfCheckbox unchecked creates unchecked AcroForm field`() {
        val pdfBytes = renderToPdf {
            Column(Modifier.padding(24.dp)) {
                PdfCheckbox(name = "opt-in", label = "Optional", checked = false) {
                    Box(Modifier.width(200.dp).height(24.dp))
                }
            }
        }

        Loader.loadPDF(pdfBytes).use { doc ->
            val acroForm = doc.documentCatalog.acroForm
            assertNotNull(acroForm)
            val checkbox = acroForm.fields.filterIsInstance<PDCheckBox>().firstOrNull()
            assertNotNull(checkbox)
            assertTrue(!checkbox.isChecked, "Checkbox should not be checked")
        }
    }

    @Test
    fun `PdfRadioButton creates AcroForm radio button in PDF`() {
        val pdfBytes = renderToPdf {
            Column(Modifier.padding(24.dp)) {
                PdfRadioButton(name = "opt-a", value = "a", groupName = "choice", selected = true, label = "A") {
                    Box(Modifier.width(200.dp).height(24.dp))
                }
                PdfRadioButton(name = "opt-b", value = "b", groupName = "choice", selected = false, label = "B") {
                    Box(Modifier.width(200.dp).height(24.dp))
                }
            }
        }

        Loader.loadPDF(pdfBytes).use { doc ->
            val acroForm = doc.documentCatalog.acroForm
            assertNotNull(acroForm, "PDF should have an AcroForm")
            val radioField = acroForm.fields.filterIsInstance<PDRadioButton>().firstOrNull()
            assertNotNull(radioField, "Should have a radio button field")
            assertEquals("choice", radioField.partialName)
        }
    }

    @Test
    fun `PdfSelect creates AcroForm combo box in PDF`() {
        val pdfBytes = renderToPdf {
            Column(Modifier.padding(24.dp)) {
                PdfSelect(
                    name = "dept",
                    options = listOf(
                        PdfSelectOption("cardio", "Cardiology"),
                        PdfSelectOption("derma", "Dermatology"),
                    ),
                    selectedValue = "derma",
                ) {
                    Box(Modifier.width(200.dp).height(30.dp).background(Color.White))
                }
            }
        }

        Loader.loadPDF(pdfBytes).use { doc ->
            val acroForm = doc.documentCatalog.acroForm
            assertNotNull(acroForm, "PDF should have an AcroForm")
            val comboBox = acroForm.fields.filterIsInstance<PDComboBox>().firstOrNull()
            assertNotNull(comboBox, "Should have a combo box field")
            assertEquals("dept", comboBox.partialName)
            assertEquals("derma", comboBox.value.firstOrNull())
        }
    }

    @Test
    fun `PDF without form elements has no AcroForm`() {
        val pdfBytes = renderToPdf {
            Text("No form elements here")
        }

        Loader.loadPDF(pdfBytes).use { doc ->
            val acroForm = doc.documentCatalog.acroForm
            assertTrue(acroForm == null || acroForm.fields.isEmpty())
        }
    }
}

@file:OptIn(InternalComposeUiApi::class)

package com.chrisjenx.composepdf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class NativeElementHtmlTest {

    @Test
    fun `PdfTable renders native table element in HTML`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfTable {
                    HeaderRow {
                        Cell(semanticText = "Name") { Text("Name") }
                        Cell(semanticText = "Price") { Text("Price") }
                    }
                    Row {
                        Cell(semanticText = "Widget") { Text("Widget") }
                        Cell(semanticText = "$10") { Text("$10") }
                    }
                }
            }
        }
        assertContains(html, "<table")
        assertContains(html, "<thead>")
        assertContains(html, "<th>Name</th>")
        assertContains(html, "<th>Price</th>")
        assertContains(html, "</thead>")
        assertContains(html, "<tbody>")
        assertContains(html, "<td>Widget</td>")
        assertContains(html, "<td>\$10</td>")
        assertContains(html, "</tbody>")
        assertContains(html, "</table>")
    }

    @Test
    fun `PdfTable with caption renders caption element`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfTable(caption = "Products") {
                    Row {
                        Cell(semanticText = "Item") { Text("Item") }
                    }
                }
            }
        }
        assertContains(html, "<caption>Products</caption>")
    }

    @Test
    fun `PdfOrderedList renders native ol element in HTML`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfOrderedList {
                    Item(semanticText = "First") { Text("First") }
                    Item(semanticText = "Second") { Text("Second") }
                    Item(semanticText = "Third") { Text("Third") }
                }
            }
        }
        assertContains(html, "<ol")
        assertContains(html, "<li>First</li>")
        assertContains(html, "<li>Second</li>")
        assertContains(html, "<li>Third</li>")
        assertContains(html, "</ol>")
    }

    @Test
    fun `PdfUnorderedList renders native ul element in HTML`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfUnorderedList {
                    Item(semanticText = "Apple") { Text("Apple") }
                    Item(semanticText = "Banana") { Text("Banana") }
                }
            }
        }
        assertContains(html, "<ul")
        assertContains(html, "<li>Apple</li>")
        assertContains(html, "<li>Banana</li>")
        assertContains(html, "</ul>")
    }

    @Test
    fun `PdfButton renders native button element in HTML`() {
        val html = renderToHtml {
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
        assertContains(html, "<button")
        assertContains(html, "name=\"submit-btn\"")
        assertContains(html, ">Submit</button>")
    }

    @Test
    fun `PdfButton with onClick renders onclick attribute`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfButton(label = "Alert", name = "alert-btn", onClick = "alert('hello')") {
                    Text("Alert")
                }
            }
        }
        assertContains(html, "onclick=\"alert(")
    }

    @Test
    fun `PdfTextField renders native input element in HTML`() {
        val html = renderToHtml {
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
        assertContains(html, "<input")
        assertContains(html, "type=\"text\"")
        assertContains(html, "name=\"email\"")
        assertContains(html, "placeholder=\"Enter email\"")
        assertContains(html, "value=\"test@example.com\"")
    }

    @Test
    fun `PdfTextField multiline renders textarea in HTML`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfTextField(
                    name = "notes",
                    placeholder = "Enter notes",
                    multiline = true,
                ) {
                    Box(Modifier.width(200.dp).height(100.dp).background(Color.White))
                }
            }
        }
        assertContains(html, "<textarea")
        assertContains(html, "name=\"notes\"")
        assertContains(html, "placeholder=\"Enter notes\"")
    }

    @Test
    fun `PdfImage renders div with aria-label in HTML`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfImage(altText = "Company logo") {
                    Box(Modifier.width(100.dp).height(50.dp).background(Color.Gray))
                }
            }
        }
        assertContains(html, "role=\"img\"")
        assertContains(html, "aria-label=\"Company logo\"")
    }

    @Test
    fun `PdfHover renders hover CSS class in HTML`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfHover(
                    hoverStyles = HoverStyles(
                        backgroundColor = "rgba(0,0,0,0.1)",
                        cursor = "pointer",
                    )
                ) {
                    Box(Modifier.width(200.dp).height(40.dp).background(Color.LightGray))
                }
            }
        }
        assertContains(html, "pdf-hover-0")
        assertContains(html, ".pdf-hover-0:hover")
        assertContains(html, "background-color:rgba(0,0,0,0.1)")
        assertContains(html, "cursor:pointer")
    }

    @Test
    fun `PdfCheckbox renders native checkbox in HTML`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfCheckbox(name = "agree", label = "I agree", checked = true) {
                    Box(Modifier.width(200.dp).height(24.dp).background(Color.White))
                }
            }
        }
        assertContains(html, "<label")
        assertContains(html, "<input")
        assertContains(html, "type=\"checkbox\"")
        assertContains(html, "name=\"agree\"")
        assertContains(html, "checked=\"checked\"")
        assertContains(html, ">I agree</span>")
    }

    @Test
    fun `PdfCheckbox unchecked does not render checked attribute`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfCheckbox(name = "opt-in", label = "Optional", checked = false) {
                    Box(Modifier.width(200.dp).height(24.dp))
                }
            }
        }
        assertContains(html, "type=\"checkbox\"")
        assertContains(html, "name=\"opt-in\"")
        assertTrue(!html.contains("checked=\"checked\""), "Unchecked checkbox should not have checked attribute")
    }

    @Test
    fun `PdfRadioButton renders native radio in HTML`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfRadioButton(
                    name = "option-a",
                    value = "a",
                    groupName = "options",
                    selected = true,
                    label = "Option A",
                ) {
                    Box(Modifier.width(200.dp).height(24.dp))
                }
                PdfRadioButton(
                    name = "option-b",
                    value = "b",
                    groupName = "options",
                    selected = false,
                    label = "Option B",
                ) {
                    Box(Modifier.width(200.dp).height(24.dp))
                }
            }
        }
        assertContains(html, "type=\"radio\"")
        assertContains(html, "name=\"options\"")
        assertContains(html, "value=\"a\"")
        assertContains(html, "value=\"b\"")
        assertContains(html, ">Option A</span>")
        assertContains(html, ">Option B</span>")
    }

    @Test
    fun `PdfSelect renders native select with options in HTML`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfSelect(
                    name = "department",
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
        assertContains(html, "<select")
        assertContains(html, "name=\"department\"")
        assertContains(html, "<option")
        assertContains(html, "value=\"cardio\"")
        assertContains(html, ">Cardiology</option>")
        assertContains(html, "value=\"derma\"")
        assertContains(html, "selected=\"selected\"")
        assertContains(html, ">Dermatology</option>")
        assertContains(html, "</select>")
    }

    @Test
    fun `PdfSlider renders native range input in HTML`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfSlider(
                    name = "pain-level",
                    min = 0f,
                    max = 10f,
                    value = 3f,
                    step = 1f,
                ) {
                    Box(Modifier.width(200.dp).height(24.dp))
                }
            }
        }
        assertContains(html, "<input")
        assertContains(html, "type=\"range\"")
        assertContains(html, "name=\"pain-level\"")
        assertContains(html, "min=\"0\"")
        assertContains(html, "max=\"10\"")
        assertContains(html, "value=\"3\"")
        assertContains(html, "step=\"1\"")
    }

    @Test
    fun `multiple native elements on same page`() {
        val html = renderToHtml {
            Column(Modifier.padding(24.dp)) {
                PdfTable {
                    Row {
                        Cell(semanticText = "A") { Text("A") }
                    }
                }
                PdfButton(label = "Go", name = "go") {
                    Text("Go")
                }
                PdfUnorderedList {
                    Item(semanticText = "X") { Text("X") }
                }
            }
        }
        assertContains(html, "<table")
        assertContains(html, "<button")
        assertContains(html, "<ul")
    }
}

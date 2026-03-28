package com.chrisjenx.kinvoicing.composehtml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PdfElementCollectorTest {

    @Test
    fun `empty collector has no elements`() {
        val collector = PdfElementCollector()
        assertTrue(collector.elements.isEmpty())
    }

    @Test
    fun `add stores annotation`() {
        val collector = PdfElementCollector()
        collector.add(PdfButtonAnnotation("Submit", "btn1", onClick = null, x = 10f, y = 20f, width = 100f, height = 30f, id = "e0"))
        assertEquals(1, collector.elements.size)
        assertTrue(collector.elements[0] is PdfButtonAnnotation)
    }

    @Test
    fun `add multiple types preserves order`() {
        val collector = PdfElementCollector()
        collector.add(PdfButtonAnnotation("Submit", "btn1", onClick = null, x = 0f, y = 0f, width = 50f, height = 20f, id = "e0"))
        collector.add(PdfTextFieldAnnotation("email", placeholder = "", value = "", multiline = false, maxLength = 0, x = 0f, y = 30f, width = 200f, height = 30f, id = "e1"))
        collector.add(PdfTableAnnotation(rows = emptyList(), caption = null, x = 0f, y = 70f, width = 300f, height = 100f, id = "e2"))
        assertEquals(3, collector.elements.size)
        assertTrue(collector.elements[0] is PdfButtonAnnotation)
        assertTrue(collector.elements[1] is PdfTextFieldAnnotation)
        assertTrue(collector.elements[2] is PdfTableAnnotation)
    }

    @Test
    fun `clear removes all elements and resets ID counter`() {
        val collector = PdfElementCollector()
        collector.add(PdfButtonAnnotation("A", "a", onClick = null, x = 0f, y = 0f, width = 50f, height = 20f, id = "e0"))
        collector.generateId()
        collector.clear()
        assertTrue(collector.elements.isEmpty())
        assertEquals("pdf-elem-0", collector.generateId(), "ID counter should reset after clear")
    }

    @Test
    fun `generateId produces sequential unique IDs`() {
        val collector = PdfElementCollector()
        assertEquals("pdf-elem-0", collector.generateId())
        assertEquals("pdf-elem-1", collector.generateId())
        assertEquals("pdf-elem-2", collector.generateId())
    }

    @Test
    fun `table annotation stores structured content`() {
        val table = PdfTableAnnotation(
            rows = listOf(
                PdfTableRowAnnotation(
                    cells = listOf(PdfTableCellAnnotation("Name"), PdfTableCellAnnotation("Price")),
                    isHeader = true,
                ),
                PdfTableRowAnnotation(
                    cells = listOf(PdfTableCellAnnotation("Widget"), PdfTableCellAnnotation("$10")),
                    isHeader = false,
                ),
            ),
            caption = "Products",
            x = 10f, y = 20f, width = 400f, height = 100f, id = "t0",
        )
        assertEquals(2, table.rows.size)
        assertTrue(table.rows[0].isHeader)
        assertEquals("Name", table.rows[0].cells[0].text)
        assertEquals("Products", table.caption)
    }

    @Test
    fun `list annotation stores items`() {
        val list = PdfListAnnotation(
            ordered = true,
            items = listOf(
                PdfListItemAnnotation("First"),
                PdfListItemAnnotation("Second"),
                PdfListItemAnnotation("Third"),
            ),
            x = 0f, y = 0f, width = 200f, height = 80f, id = "l0",
        )
        assertTrue(list.ordered)
        assertEquals(3, list.items.size)
        assertEquals("Second", list.items[1].text)
    }

    @Test
    fun `hover annotation stores styles`() {
        val hover = PdfHoverAnnotation(
            hoverStyles = HoverStyles(
                backgroundColor = "rgba(0,0,0,0.1)",
                opacity = 0.9f,
                cursor = "pointer",
                boxShadow = "0 2px 4px rgba(0,0,0,0.2)",
            ),
            x = 0f, y = 0f, width = 100f, height = 40f, id = "h0",
        )
        assertEquals("rgba(0,0,0,0.1)", hover.hoverStyles.backgroundColor)
        assertEquals(0.9f, hover.hoverStyles.opacity)
        assertEquals("pointer", hover.hoverStyles.cursor)
        assertEquals("0 2px 4px rgba(0,0,0,0.2)", hover.hoverStyles.boxShadow)
    }
}

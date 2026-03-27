package com.chrisjenx.kinvoicing.fidelity.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrisjenx.compose2pdf.PdfLink
import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.compose2pdf.RenderMode
import com.chrisjenx.compose2pdf.renderToPdf
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink
import kotlin.test.Test
import kotlin.test.assertTrue

class LinkAnnotationTest {

    private val config = PdfPageConfig.A4
    private val density = Density(2f)

    @Test
    fun `PdfLink annotations are present in vector PDF`() {
        val pdfBytes = renderToPdf(config = config, density = density, mode = RenderMode.VECTOR) {
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                PdfLink(href = "https://example.com") {
                    Text("Visit Example", fontSize = 14.sp)
                }
                Spacer(Modifier.height(8.dp))
                PdfLink(href = "https://pay.acme.com/inv-001") {
                    Text("Pay Invoice Online", fontSize = 14.sp)
                }
            }
        }

        Loader.loadPDF(pdfBytes).use { doc ->
            val page = doc.getPage(0)
            val annotations = page.annotations.filterIsInstance<PDAnnotationLink>()
            val uris = annotations.mapNotNull { link ->
                val action = link.action
                if (action is org.apache.pdfbox.pdmodel.interactive.action.PDActionURI) {
                    action.uri
                } else null
            }

            for (expected in listOf("https://example.com", "https://pay.acme.com/inv-001")) {
                assertTrue(
                    uris.contains(expected),
                    "Expected link annotation for $expected, found: $uris",
                )
            }

            for (link in annotations) {
                val rect = link.rectangle
                assertTrue(rect.width > 0, "Link rectangle width must be positive: $rect")
                assertTrue(rect.height > 0, "Link rectangle height must be positive: $rect")
            }
        }
    }
}

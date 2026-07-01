package com.chrisjenx.compat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chrisjenx.kinvoicing.InvoiceFixtures
import com.chrisjenx.kinvoicing.html.renderToHtml
import com.chrisjenx.kinvoicing.pdf.PdfRenderer

/**
 * Exercises BOTH reflective scene-driver paths of the PUBLISHED kinvoicing render jars (built
 * at kinvoicing's base CMP 1.11.1) against whatever Compose runtime this build resolved:
 *
 *  1. render-html — drives kinvoicing's OWN ComposeSceneRenderer (renderToHtml).
 *  2. render-pdf  — drives compose2pdf's reflective renderer (PdfRenderer().render(doc)).
 *
 * A wrong reflective dispatch on the reshaped CMP 1.12 API throws → non-zero exit → red CI.
 */
fun main() {
    // --- render-html: kinvoicing's own reflective ComposeSceneRenderer ---
    val html = renderToHtml {
        Column {
            Text("kinvoicing compat smoke")
            Box(Modifier.size(48.dp).background(Color.Red))
        }
    }
    check(html.isNotEmpty()) { "render-html produced empty output" }
    check(html.contains("<svg")) { "render-html produced no SVG" }

    // --- render-pdf: compose2pdf's reflective renderer over the kinvoicing IR ---
    val doc = InvoiceFixtures.basic
    val pdf = PdfRenderer().render(doc)
    check(pdf.size > 100) { "PDF suspiciously small: ${pdf.size} bytes" }
    val header = pdf.copyOfRange(0, 5).toString(Charsets.US_ASCII)
    check(header == "%PDF-") { "render-pdf did not produce a PDF — header was '$header'" }

    println("kinvoicing compat-consumer OK: ${html.length}-char HTML, ${pdf.size}-byte PDF")
}

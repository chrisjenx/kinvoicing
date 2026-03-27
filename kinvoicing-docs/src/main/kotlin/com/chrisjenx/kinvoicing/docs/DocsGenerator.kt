package com.chrisjenx.kinvoicing.docs

import com.chrisjenx.kinvoicing.InvoiceDocument
import com.chrisjenx.kinvoicing.examples.InvoiceExamples
import com.chrisjenx.kinvoicing.examples.InvoiceShowcases
import com.chrisjenx.kinvoicing.html.email.toHtml
import java.io.File

fun main() {
    val projectRoot = File(
        System.getProperty("project.root") ?: System.getProperty("user.dir")
    )
    val outputDir = File(projectRoot, "docs/_includes/examples")
    outputDir.mkdirs()

    val allFixtures: List<Pair<String, InvoiceDocument>> =
        InvoiceShowcases.all + InvoiceExamples.all

    for ((name, document) in allFixtures) {
        File(outputDir, "$name.html").writeText(document.toHtml())
    }

    println("Generated ${allFixtures.size} example HTML files in ${outputDir.absolutePath}")
}

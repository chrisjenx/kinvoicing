package com.chrisjenx.invoicekit.compose.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrisjenx.invoicekit.InvoiceElement
import com.chrisjenx.invoicekit.InvoiceSection
import com.chrisjenx.invoicekit.compose.*

@Composable
internal fun CustomSection(custom: InvoiceSection.Custom) {
    Column(modifier = Modifier.fillMaxWidth()) {
        custom.content.forEach { element ->
            ElementContent(element)
        }
    }
}

@Composable
private fun ElementContent(element: InvoiceElement) {
    val style = LocalInvoiceStyle.current

    when (element) {
        is InvoiceElement.Text -> {
            Text(
                text = element.value,
                fontSize = 14.sp,
                color = style.textComposeColor,
            )
        }
        is InvoiceElement.Spacer -> {
            Spacer(modifier = Modifier.height(element.height.dp))
        }
        is InvoiceElement.Divider -> {
            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))
        }
        is InvoiceElement.Row -> {
            Row(modifier = Modifier.fillMaxWidth()) {
                element.children.forEachIndexed { i, child ->
                    val weight = if (element.weights.isNotEmpty() && i < element.weights.size) {
                        element.weights[i]
                    } else {
                        1f
                    }
                    Box(modifier = Modifier.weight(weight)) {
                        ElementContent(child)
                    }
                }
            }
        }
        is InvoiceElement.Image -> {
            // Image rendering would require platform-specific bitmap decoding.
            // In compose2pdf/PDF context, this would be handled by the PDF renderer.
            // For preview, we show a placeholder.
            Text(
                text = "[Image: ${element.contentType}]",
                fontSize = 12.sp,
                color = style.secondaryComposeColor,
            )
        }
    }
}

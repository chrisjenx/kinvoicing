package com.chrisjenx.kinvoicing.compose.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chrisjenx.kinvoicing.InvoiceElement
import com.chrisjenx.kinvoicing.InvoiceSection
import com.chrisjenx.kinvoicing.compose.*

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
                fontSize = InvoiceTypography.bodyLarge,
                color = style.textComposeColor,
            )
        }
        is InvoiceElement.Spacer -> {
            Spacer(modifier = Modifier.height(element.height.dp))
        }
        is InvoiceElement.Divider -> {
            HorizontalDivider(color = style.borderComposeColor, modifier = Modifier.padding(vertical = InvoiceSpacing.sm))
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
        is InvoiceElement.Link -> {
            val linkWrapper = LocalLinkWrapper.current
            linkWrapper(element.href) {
                Text(
                    text = element.text,
                    fontSize = InvoiceTypography.bodyLarge,
                    color = style.primaryComposeColor,
                )
            }
        }
        is InvoiceElement.Image -> {
            val imageRenderer = LocalImageRenderer.current
            imageRenderer(element.source, element.width, element.height, null)
        }
    }
}

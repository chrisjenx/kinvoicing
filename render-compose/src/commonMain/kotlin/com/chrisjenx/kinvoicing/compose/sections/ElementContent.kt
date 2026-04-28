package com.chrisjenx.kinvoicing.compose.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chrisjenx.kinvoicing.InvoiceElement
import com.chrisjenx.kinvoicing.LinkStyle
import com.chrisjenx.kinvoicing.compose.*

/**
 * Renders any [InvoiceElement] to Compose UI. Single source of truth for how
 * inline content (text, links, buttons, dividers, images, rows) is presented.
 *
 * Used by [CustomSection], [PaymentInfoSection], and [FooterSection].
 */
@Composable
internal fun ElementContent(element: InvoiceElement) {
    val style = LocalInvoiceStyle.current
    when (element) {
        is InvoiceElement.Text -> Text(
            text = element.value,
            fontSize = InvoiceTypography.bodyLarge,
            color = style.textComposeColor,
        )

        is InvoiceElement.Spacer -> Spacer(modifier = Modifier.height(element.height.dp))

        is InvoiceElement.Divider -> HorizontalDivider(
            color = style.borderComposeColor,
            modifier = Modifier.padding(vertical = InvoiceSpacing.sm),
        )

        is InvoiceElement.Row -> Row(modifier = Modifier.fillMaxWidth()) {
            element.children.forEachIndexed { i, child ->
                val weight = if (i < element.weights.size) element.weights[i] else 1f
                Box(modifier = Modifier.weight(weight)) {
                    ElementContent(child)
                }
            }
        }

        is InvoiceElement.Link -> {
            val linkWrapper = LocalLinkWrapper.current
            when (element.style) {
                LinkStyle.TEXT -> linkWrapper(element.href) {
                    Text(
                        text = element.text,
                        fontSize = InvoiceTypography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = style.primaryComposeColor,
                    )
                }
                LinkStyle.BUTTON -> linkWrapper(element.href) {
                    Box(
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .background(style.primaryComposeColor, RoundedCornerShape(20.dp))
                            .padding(horizontal = 24.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = element.text,
                            fontSize = InvoiceTypography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                        )
                    }
                }
            }
        }

        is InvoiceElement.Image -> {
            val imageRenderer = LocalImageRenderer.current
            imageRenderer(element.source, element.width, element.height, null)
        }
    }
}

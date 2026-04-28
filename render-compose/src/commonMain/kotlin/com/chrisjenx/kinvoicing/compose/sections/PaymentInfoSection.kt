package com.chrisjenx.kinvoicing.compose.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chrisjenx.kinvoicing.InvoiceSection
import com.chrisjenx.kinvoicing.compose.*

@Composable
internal fun PaymentInfoSection(payment: InvoiceSection.PaymentInfo) {
    val style = LocalInvoiceStyle.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(style.mutedBgComposeColor, RoundedCornerShape(4.dp))
            .padding(InvoiceSpacing.lg),
    ) {
        Text(
            text = "PAYMENT INFORMATION",
            fontSize = InvoiceTypography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = style.secondaryComposeColor,
        )
        Spacer(modifier = Modifier.height(InvoiceSpacing.sm))

        payment.bankName?.let {
            Text(text = "Bank: $it", fontSize = InvoiceTypography.bodyMedium, color = style.textComposeColor)
        }
        payment.accountNumber?.let {
            Text(text = "Account: $it", fontSize = InvoiceTypography.bodyMedium, color = style.textComposeColor)
        }
        payment.routingNumber?.let {
            Text(text = "Routing: $it", fontSize = InvoiceTypography.bodyMedium, color = style.textComposeColor)
        }
        payment.paymentLink?.let { link ->
            Spacer(modifier = Modifier.height(InvoiceSpacing.sm))
            ElementContent(link)
        }
        payment.notes?.let { elements ->
            Spacer(modifier = Modifier.height(InvoiceSpacing.sm))
            elements.forEach { ElementContent(it) }
        }
    }
}

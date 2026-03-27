package com.chrisjenx.invoicekit.compose.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrisjenx.invoicekit.InvoiceSection
import com.chrisjenx.invoicekit.compose.*

@Composable
internal fun PaymentInfoSection(payment: InvoiceSection.PaymentInfo) {
    val style = LocalInvoiceStyle.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgMutedColor, RoundedCornerShape(4.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "PAYMENT INFORMATION",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = style.secondaryComposeColor,
        )
        Spacer(modifier = Modifier.height(8.dp))

        payment.bankName?.let {
            Text(text = "Bank: $it", fontSize = 13.sp, color = style.textComposeColor)
        }
        payment.accountNumber?.let {
            Text(text = "Account: $it", fontSize = 13.sp, color = style.textComposeColor)
        }
        payment.routingNumber?.let {
            Text(text = "Routing: $it", fontSize = 13.sp, color = style.textComposeColor)
        }
        payment.paymentLink?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pay Online: $it",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = style.primaryComposeColor,
            )
        }
        payment.notes?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, fontSize = 12.sp, color = style.secondaryComposeColor)
        }
    }
}

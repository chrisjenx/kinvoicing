package com.chrisjenx.invoicekit.compose.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrisjenx.invoicekit.compose.*

@Composable
internal fun PartySection(
    name: String,
    address: List<String>,
    email: String?,
    phone: String?,
    label: String,
) {
    val style = LocalInvoiceStyle.current

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = style.secondaryComposeColor,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = style.textComposeColor,
        )
        address.forEach { line ->
            Text(text = line, fontSize = 13.sp, color = style.textComposeColor)
        }
        email?.let {
            Text(text = it, fontSize = 13.sp, color = style.secondaryComposeColor)
        }
        phone?.let {
            Text(text = it, fontSize = 13.sp, color = style.secondaryComposeColor)
        }
    }
}

package com.chrisjenx.kinvoicing.compose.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.chrisjenx.kinvoicing.ContactInfo
import com.chrisjenx.kinvoicing.compose.*

@Composable
internal fun PartySection(contact: ContactInfo, label: String) {
    val style = LocalInvoiceStyle.current

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = InvoiceSpacing.sm)) {
        Text(
            text = label.uppercase(),
            fontSize = InvoiceTypography.caption,
            fontWeight = FontWeight.Bold,
            color = style.secondaryComposeColor,
        )
        Spacer(modifier = Modifier.height(InvoiceSpacing.xs))
        Text(
            text = contact.name,
            fontSize = InvoiceTypography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = style.textComposeColor,
        )
        contact.address.forEach { line ->
            Text(text = line, fontSize = InvoiceTypography.bodyMedium, color = style.textComposeColor)
        }
        contact.email?.let {
            Text(text = it, fontSize = InvoiceTypography.bodyMedium, color = style.secondaryComposeColor)
        }
        contact.phone?.let {
            Text(text = it, fontSize = InvoiceTypography.bodyMedium, color = style.secondaryComposeColor)
        }
    }
}

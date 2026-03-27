package com.chrisjenx.kinvoicing.compose.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrisjenx.kinvoicing.InvoiceSection
import com.chrisjenx.kinvoicing.compose.*

@Composable
internal fun FooterSection(footer: InvoiceSection.Footer) {
    val style = LocalInvoiceStyle.current

    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        HorizontalDivider(color = BorderColor)
        Spacer(modifier = Modifier.height(12.dp))

        footer.notes?.let {
            Text(text = it, fontSize = 13.sp, color = style.secondaryComposeColor)
            Spacer(modifier = Modifier.height(4.dp))
        }
        footer.terms?.let {
            Text(text = it, fontSize = 12.sp, color = style.secondaryComposeColor)
        }
        footer.customContent?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, fontSize = 12.sp, color = style.secondaryComposeColor)
        }
    }
}

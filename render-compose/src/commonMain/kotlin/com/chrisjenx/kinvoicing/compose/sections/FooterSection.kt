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
internal fun FooterSection(footer: InvoiceSection.Footer) {
    val style = LocalInvoiceStyle.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = InvoiceSpacing.lg)
            .background(style.mutedBgComposeColor, RoundedCornerShape(4.dp))
            .padding(InvoiceSpacing.lg),
    ) {
        footer.notes?.let {
            Text(
                text = "NOTES",
                fontSize = InvoiceTypography.caption,
                fontWeight = FontWeight.Bold,
                color = style.secondaryComposeColor,
            )
            Spacer(modifier = Modifier.height(InvoiceSpacing.xs))
            Text(text = it, fontSize = InvoiceTypography.bodyMedium, color = style.secondaryComposeColor)
            Spacer(modifier = Modifier.height(InvoiceSpacing.sm))
        }
        footer.terms?.let {
            Text(
                text = "TERMS",
                fontSize = InvoiceTypography.caption,
                fontWeight = FontWeight.Bold,
                color = style.secondaryComposeColor,
            )
            Spacer(modifier = Modifier.height(InvoiceSpacing.xs))
            Text(text = it, fontSize = InvoiceTypography.bodySmall, color = style.secondaryComposeColor)
        }
        footer.customContent?.let {
            Spacer(modifier = Modifier.height(InvoiceSpacing.sm))
            Text(text = it, fontSize = InvoiceTypography.bodySmall, color = style.secondaryComposeColor)
        }
    }
}

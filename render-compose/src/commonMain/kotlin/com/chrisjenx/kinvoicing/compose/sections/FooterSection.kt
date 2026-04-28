package com.chrisjenx.kinvoicing.compose.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chrisjenx.kinvoicing.BrandLayout
import com.chrisjenx.kinvoicing.Branding
import com.chrisjenx.kinvoicing.InvoiceSection
import com.chrisjenx.kinvoicing.compose.*

@Composable
internal fun FooterSection(footer: InvoiceSection.Footer, branding: Branding? = null) {
    val style = LocalInvoiceStyle.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = InvoiceSpacing.lg)
            .background(style.mutedBgComposeColor, RoundedCornerShape(4.dp))
            .padding(InvoiceSpacing.lg),
    ) {
        footer.notes?.let { elements ->
            Text(
                text = "NOTES",
                fontSize = InvoiceTypography.caption,
                fontWeight = FontWeight.Bold,
                color = style.secondaryComposeColor,
            )
            Spacer(modifier = Modifier.height(InvoiceSpacing.xs))
            elements.forEach { ElementContent(it) }
            Spacer(modifier = Modifier.height(InvoiceSpacing.sm))
        }
        footer.terms?.let { elements ->
            Text(
                text = "TERMS",
                fontSize = InvoiceTypography.caption,
                fontWeight = FontWeight.Bold,
                color = style.secondaryComposeColor,
            )
            Spacer(modifier = Modifier.height(InvoiceSpacing.xs))
            elements.forEach { ElementContent(it) }
        }
        footer.customContent?.let { elements ->
            Spacer(modifier = Modifier.height(InvoiceSpacing.sm))
            elements.forEach { ElementContent(it) }
        }
        val pb = branding?.takeIf { it.layout == BrandLayout.POWERED_BY_FOOTER }?.poweredBy
        if (pb != null) {
            Spacer(modifier = Modifier.height(InvoiceSpacing.lg))
            HorizontalDivider(color = style.dividerComposeColor)
            Spacer(modifier = Modifier.height(InvoiceSpacing.sm))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                pb.logo?.let { logoSource ->
                    val imageRenderer = LocalImageRenderer.current
                    imageRenderer(logoSource, null, 24, "${pb.name} logo")
                    Spacer(modifier = Modifier.width(InvoiceSpacing.sm))
                }
                Text(
                    text = pb.tagline ?: "Powered by ${pb.name}",
                    fontSize = InvoiceTypography.caption,
                    color = style.secondaryComposeColor,
                )
            }
        }
    }
}

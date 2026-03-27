package com.chrisjenx.invoicekit.compose.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrisjenx.invoicekit.*
import com.chrisjenx.invoicekit.compose.*

@Composable
internal fun HeaderSection(header: InvoiceSection.Header) {
    val style = LocalInvoiceStyle.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            // Left: Branding
            Column(modifier = Modifier.weight(1f)) {
                header.branding?.let { branding ->
                    BrandingContent(branding, style)
                }
            }

            // Right: Invoice details
            Column(horizontalAlignment = Alignment.End) {
                header.invoiceNumber?.let {
                    Text(
                        text = it,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = style.primaryComposeColor,
                    )
                }
                header.issueDate?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Issue Date: $it",
                        fontSize = 13.sp,
                        color = style.secondaryComposeColor,
                    )
                }
                header.dueDate?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Due Date: $it",
                        fontSize = 13.sp,
                        color = style.secondaryComposeColor,
                    )
                }
            }
        }

        if (style.accentBorder) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 3.dp, color = style.primaryComposeColor)
        }
    }
}

@Composable
private fun BrandingContent(branding: Branding, style: InvoiceStyle) {
    Text(
        text = branding.primary.name,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = style.textComposeColor,
    )
    branding.primary.address.forEach { line ->
        Text(text = line, fontSize = 13.sp, color = style.secondaryComposeColor)
    }
    branding.primary.email?.let {
        Text(text = it, fontSize = 13.sp, color = style.secondaryComposeColor)
    }
    branding.primary.phone?.let {
        Text(text = it, fontSize = 13.sp, color = style.secondaryComposeColor)
    }
    branding.primary.website?.let {
        Text(text = it, fontSize = 13.sp, color = style.primaryComposeColor)
    }

    branding.poweredBy?.let { pb ->
        Spacer(modifier = Modifier.height(8.dp))
        when (branding.layout) {
            BrandLayout.POWERED_BY_FOOTER -> {
                Text(
                    text = pb.tagline ?: "Powered by ${pb.name}",
                    fontSize = 11.sp,
                    color = style.secondaryComposeColor,
                )
            }
            BrandLayout.POWERED_BY_HEADER -> {
                Text(
                    text = pb.name,
                    fontSize = 12.sp,
                    color = style.secondaryComposeColor,
                )
            }
            BrandLayout.DUAL_HEADER -> {
                Text(
                    text = pb.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.textComposeColor,
                )
            }
        }
    }
}

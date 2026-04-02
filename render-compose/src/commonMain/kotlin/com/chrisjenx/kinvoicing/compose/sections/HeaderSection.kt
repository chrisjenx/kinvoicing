package com.chrisjenx.kinvoicing.compose.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.compose.*

@Composable
internal fun HeaderSection(header: InvoiceSection.Header) {
    val style = LocalInvoiceStyle.current

    Column(modifier = Modifier.fillMaxWidth()) {
        when (style.headerLayout) {
            HeaderLayout.HORIZONTAL -> HorizontalHeader(header, style)
            HeaderLayout.STACKED -> StackedHeader(header, style)
        }

        if (style.accentBorder) {
            Spacer(modifier = Modifier.height(InvoiceSpacing.md))
            HorizontalDivider(thickness = 3.dp, color = style.primaryComposeColor)
        }
    }
}

@Composable
private fun HorizontalHeader(header: InvoiceSection.Header, style: InvoiceStyle) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            header.branding?.let { branding ->
                BrandingContent(branding, style)
            }
        }
        InvoiceDetailsColumn(header, style)
    }
}

@Composable
private fun StackedHeader(header: InvoiceSection.Header, style: InvoiceStyle) {
    val alignment = when (style.logoPlacement) {
        LogoPlacement.LEFT -> Alignment.Start
        LogoPlacement.CENTER -> Alignment.CenterHorizontally
        LogoPlacement.RIGHT -> Alignment.End
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        header.branding?.let { branding ->
            BrandingContent(branding, style)
        }
        Spacer(modifier = Modifier.height(InvoiceSpacing.lg))
        InvoiceDetailsColumn(header, style)
    }
}

@Composable
private fun InvoiceDetailsColumn(header: InvoiceSection.Header, style: InvoiceStyle) {
    Column(horizontalAlignment = Alignment.End) {
        header.invoiceNumber?.let {
            Text(
                text = it,
                fontSize = InvoiceTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = style.primaryComposeColor,
            )
        }
        header.issueDate?.let {
            Spacer(modifier = Modifier.height(InvoiceSpacing.xs))
            Text(
                text = "Issue Date: $it",
                fontSize = InvoiceTypography.bodyMedium,
                color = style.secondaryComposeColor,
            )
        }
        header.dueDate?.let {
            Spacer(modifier = Modifier.height(InvoiceSpacing.xs))
            Text(
                text = "Due Date: $it",
                fontSize = InvoiceTypography.bodyMedium,
                color = style.secondaryComposeColor,
            )
        }
    }
}

@Composable
private fun BrandingContent(branding: Branding, style: InvoiceStyle) {
    val linkWrapper = LocalLinkWrapper.current
    val imageRenderer = LocalImageRenderer.current

    branding.primary.logo?.let { logoSource ->
        imageRenderer(
            logoSource,
            null,
            null,
            "${branding.primary.name} logo",
        )
        Spacer(modifier = Modifier.height(InvoiceSpacing.sm))
    }
    Text(
        text = branding.primary.name,
        fontSize = InvoiceTypography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = style.textComposeColor,
    )
    branding.primary.address.forEach { line ->
        Text(text = line, fontSize = InvoiceTypography.bodyMedium, color = style.secondaryComposeColor)
    }
    branding.primary.email?.let { email ->
        linkWrapper("mailto:$email") {
            Text(text = email, fontSize = InvoiceTypography.bodyMedium, color = style.secondaryComposeColor)
        }
    }
    branding.primary.phone?.let { phone ->
        linkWrapper("tel:$phone") {
            Text(text = phone, fontSize = InvoiceTypography.bodyMedium, color = style.secondaryComposeColor)
        }
    }
    branding.primary.website?.let { website ->
        linkWrapper(website) {
            Text(text = website, fontSize = InvoiceTypography.bodyMedium, color = style.primaryComposeColor)
        }
    }

    branding.poweredBy?.let { pb ->
        Spacer(modifier = Modifier.height(InvoiceSpacing.sm))
        when (branding.layout) {
            BrandLayout.POWERED_BY_FOOTER -> {
                Text(
                    text = pb.tagline ?: "Powered by ${pb.name}",
                    fontSize = InvoiceTypography.caption,
                    color = style.secondaryComposeColor,
                )
            }
            BrandLayout.POWERED_BY_HEADER -> {
                Text(
                    text = pb.name,
                    fontSize = InvoiceTypography.bodySmall,
                    color = style.secondaryComposeColor,
                )
            }
            BrandLayout.DUAL_HEADER -> {
                Text(
                    text = pb.name,
                    fontSize = InvoiceTypography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = style.textComposeColor,
                )
            }
        }
    }
}

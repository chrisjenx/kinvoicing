package com.chrisjenx.kinvoicing.compose.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.compose.*
import com.chrisjenx.kinvoicing.util.CurrencyFormatter
import com.chrisjenx.kinvoicing.util.formatAsQuantity
import com.chrisjenx.kinvoicing.util.labelWithPercent

@Composable
internal fun LineItemsSection(lineItems: InvoiceSection.LineItems, currency: String) {
    val style = LocalInvoiceStyle.current

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = InvoiceSpacing.lg)) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(style.primaryComposeColor.copy(alpha = 0.06f))
                .padding(vertical = InvoiceSpacing.sm, horizontal = InvoiceSpacing.sm)
        ) {
            lineItems.columnHeaders.forEachIndexed { i, header ->
                Text(
                    text = header.uppercase(),
                    fontSize = InvoiceTypography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = style.secondaryComposeColor,
                    textAlign = if (i == lineItems.columnHeaders.lastIndex) TextAlign.End else TextAlign.Start,
                    modifier = Modifier.weight(if (i == 0) 2f else 1f),
                )
            }
        }

        if (style.showGridLines) {
            HorizontalDivider(color = style.borderComposeColor)
        }

        // Data rows
        lineItems.rows.forEachIndexed { rowIdx, item ->
            val bgColor = if (rowIdx % 2 == 1) style.mutedBgComposeColor else Color.Transparent

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(vertical = InvoiceSpacing.sm, horizontal = InvoiceSpacing.sm)
            ) {
                Text(
                    text = item.description,
                    fontSize = InvoiceTypography.bodyLarge,
                    color = style.textComposeColor,
                    modifier = Modifier.weight(2f),
                )
                if (lineItems.columnHeaders.size >= 3) {
                    Text(
                        text = item.quantity?.formatAsQuantity() ?: "",
                        fontSize = InvoiceTypography.bodyLarge,
                        color = style.textComposeColor,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (lineItems.columnHeaders.size >= 4) {
                    Text(
                        text = item.unitPrice?.let { CurrencyFormatter.format(it, currency) } ?: "",
                        fontSize = InvoiceTypography.bodyLarge,
                        color = style.textComposeColor,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    text = CurrencyFormatter.format(item.amount, currency),
                    fontSize = InvoiceTypography.bodyLarge,
                    color = if (item.amount < 0) style.negativeComposeColor else style.textComposeColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }

            if (style.showGridLines) {
                HorizontalDivider(color = style.borderComposeColor)
            } else {
                HorizontalDivider(color = style.dividerComposeColor)
            }

            // Sub-items
            item.subItems.forEach { sub ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = InvoiceSpacing.xs, horizontal = InvoiceSpacing.sm)
                        .padding(start = InvoiceSpacing.lg)
                ) {
                    Text(
                        text = sub.description,
                        fontSize = InvoiceTypography.bodySmall,
                        color = style.secondaryComposeColor,
                        modifier = Modifier.weight(2f),
                    )
                    if (lineItems.columnHeaders.size >= 3) {
                        Text(
                            text = sub.quantity?.formatAsQuantity() ?: "",
                            fontSize = InvoiceTypography.bodySmall,
                            color = style.secondaryComposeColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (lineItems.columnHeaders.size >= 4) {
                        Text(
                            text = sub.unitPrice?.let { CurrencyFormatter.format(it, currency) } ?: "",
                            fontSize = InvoiceTypography.bodySmall,
                            color = style.secondaryComposeColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Text(
                        text = CurrencyFormatter.format(sub.amount, currency),
                        fontSize = InvoiceTypography.bodySmall,
                        color = style.secondaryComposeColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Item-level discounts
            item.discounts.forEach { disc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = InvoiceSpacing.xxs, horizontal = InvoiceSpacing.sm)
                        .padding(start = InvoiceSpacing.lg)
                ) {
                    Text(
                        text = disc.labelWithPercent,
                        fontSize = InvoiceTypography.bodySmall,
                        color = style.negativeComposeColor,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

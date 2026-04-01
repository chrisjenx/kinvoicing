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
    val hasColumn = lineItems.columns.map { it.column }.toSet()

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = InvoiceSpacing.lg)) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(style.primaryComposeColor.copy(alpha = 0.06f))
                .padding(vertical = InvoiceSpacing.sm, horizontal = InvoiceSpacing.sm)
        ) {
            lineItems.columns.forEachIndexed { i, col ->
                Text(
                    text = col.label.uppercase(),
                    fontSize = InvoiceTypography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = style.secondaryComposeColor,
                    textAlign = if (col.column == LineItemColumn.AMOUNT) TextAlign.End else TextAlign.Start,
                    modifier = Modifier.weight(if (col.column == LineItemColumn.DESCRIPTION) 2f else 1f),
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
                lineItems.columns.forEach { col ->
                    val text = when (col.column) {
                        LineItemColumn.DESCRIPTION -> item.description
                        LineItemColumn.QUANTITY -> item.quantity?.formatAsQuantity() ?: ""
                        LineItemColumn.UNIT_PRICE -> item.unitPrice?.let { CurrencyFormatter.format(it, currency) } ?: ""
                        LineItemColumn.AMOUNT -> CurrencyFormatter.format(item.amount, currency)
                    }
                    val color = if (col.column == LineItemColumn.AMOUNT && item.amount < 0) {
                        style.negativeComposeColor
                    } else {
                        style.textComposeColor
                    }
                    Text(
                        text = text,
                        fontSize = InvoiceTypography.bodyLarge,
                        color = color,
                        textAlign = if (col.column == LineItemColumn.AMOUNT) TextAlign.End else TextAlign.Start,
                        modifier = Modifier.weight(if (col.column == LineItemColumn.DESCRIPTION) 2f else 1f),
                    )
                }
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
                    lineItems.columns.forEach { col ->
                        val text = when (col.column) {
                            LineItemColumn.DESCRIPTION -> sub.description
                            LineItemColumn.QUANTITY -> sub.quantity?.formatAsQuantity() ?: ""
                            LineItemColumn.UNIT_PRICE -> sub.unitPrice?.let { CurrencyFormatter.format(it, currency) } ?: ""
                            LineItemColumn.AMOUNT -> CurrencyFormatter.format(sub.amount, currency)
                        }
                        Text(
                            text = text,
                            fontSize = InvoiceTypography.bodySmall,
                            color = style.secondaryComposeColor,
                            textAlign = if (col.column == LineItemColumn.AMOUNT) TextAlign.End else TextAlign.Start,
                            modifier = Modifier.weight(if (col.column == LineItemColumn.DESCRIPTION) 2f else 1f),
                        )
                    }
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

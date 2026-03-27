package com.chrisjenx.invoicekit.compose.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrisjenx.invoicekit.*
import com.chrisjenx.invoicekit.compose.*
import com.chrisjenx.invoicekit.util.CurrencyFormatter
import com.chrisjenx.invoicekit.util.formatAsQuantity
import com.chrisjenx.invoicekit.util.labelWithPercent

@Composable
internal fun LineItemsSection(lineItems: InvoiceSection.LineItems, currency: String) {
    val style = LocalInvoiceStyle.current

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(style.primaryComposeColor.copy(alpha = 0.06f))
                .padding(vertical = 10.dp, horizontal = 8.dp)
        ) {
            lineItems.columnHeaders.forEachIndexed { i, header ->
                Text(
                    text = header.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.secondaryComposeColor,
                    textAlign = if (i == lineItems.columnHeaders.lastIndex) TextAlign.End else TextAlign.Start,
                    modifier = Modifier.weight(if (i == 0) 2f else 1f),
                )
            }
        }

        if (style.showGridLines) {
            HorizontalDivider(color = BorderColor)
        }

        // Data rows
        lineItems.rows.forEachIndexed { rowIdx, item ->
            val bgColor = if (rowIdx % 2 == 1) BgMutedColor else Color.Transparent

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(vertical = 10.dp, horizontal = 8.dp)
            ) {
                Text(
                    text = item.description,
                    fontSize = 14.sp,
                    color = style.textComposeColor,
                    modifier = Modifier.weight(2f),
                )
                if (lineItems.columnHeaders.size >= 3) {
                    Text(
                        text = item.quantity?.formatAsQuantity() ?: "",
                        fontSize = 14.sp,
                        color = style.textComposeColor,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (lineItems.columnHeaders.size >= 4) {
                    Text(
                        text = item.unitPrice?.let { CurrencyFormatter.format(it, currency) } ?: "",
                        fontSize = 14.sp,
                        color = style.textComposeColor,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    text = CurrencyFormatter.format(item.amount, currency),
                    fontSize = 14.sp,
                    color = if (item.amount < 0) NegativeColor else style.textComposeColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }

            if (style.showGridLines) {
                HorizontalDivider(color = BorderColor)
            } else {
                HorizontalDivider(color = DividerColor)
            }

            // Sub-items
            item.subItems.forEach { sub ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = sub.description,
                        fontSize = 12.sp,
                        color = style.secondaryComposeColor,
                        modifier = Modifier.weight(2f),
                    )
                    if (lineItems.columnHeaders.size >= 3) {
                        Text(
                            text = sub.quantity?.formatAsQuantity() ?: "",
                            fontSize = 12.sp,
                            color = style.secondaryComposeColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (lineItems.columnHeaders.size >= 4) {
                        Text(
                            text = sub.unitPrice?.let { CurrencyFormatter.format(it, currency) } ?: "",
                            fontSize = 12.sp,
                            color = style.secondaryComposeColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Text(
                        text = CurrencyFormatter.format(sub.amount, currency),
                        fontSize = 12.sp,
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
                        .padding(vertical = 2.dp, horizontal = 8.dp)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = disc.labelWithPercent,
                        fontSize = 12.sp,
                        color = NegativeColor,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

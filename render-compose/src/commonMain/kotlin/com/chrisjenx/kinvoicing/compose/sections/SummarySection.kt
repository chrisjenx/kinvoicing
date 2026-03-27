package com.chrisjenx.kinvoicing.compose.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrisjenx.kinvoicing.*
import com.chrisjenx.kinvoicing.compose.*
import com.chrisjenx.kinvoicing.util.CurrencyFormatter
import com.chrisjenx.kinvoicing.util.displayAmount
import com.chrisjenx.kinvoicing.util.labelWithPercent

@Composable
internal fun SummarySection(summary: InvoiceSection.Summary, currency: String) {
    val style = LocalInvoiceStyle.current

    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.width(250.dp),
            horizontalAlignment = Alignment.End,
        ) {
            // Subtotal
            SummaryRow(
                label = "Subtotal",
                value = CurrencyFormatter.format(summary.subtotal, currency),
                labelColor = style.secondaryComposeColor,
                valueColor = style.textComposeColor,
            )

            // Adjustments
            summary.adjustments.forEach { adj ->
                val adjAmount = adj.displayAmount(summary.subtotal)
                val color = if (adjAmount < 0) NegativeColor else style.textComposeColor
                SummaryRow(
                    label = adj.labelWithPercent,
                    value = CurrencyFormatter.format(adjAmount, currency),
                    labelColor = if (adj.type == AdjustmentType.DISCOUNT || adj.type == AdjustmentType.CREDIT) {
                        NegativeColor
                    } else {
                        style.secondaryComposeColor
                    },
                    valueColor = color,
                )
            }

            // Total divider
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(thickness = 2.dp, color = style.primaryComposeColor)
            Spacer(modifier = Modifier.height(8.dp))

            // Total
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Total",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.textComposeColor,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = CurrencyFormatter.format(summary.total, currency),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.textComposeColor,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, fontSize = 13.sp, color = labelColor)
        Text(text = value, fontSize = 13.sp, color = valueColor, textAlign = TextAlign.End)
    }
}

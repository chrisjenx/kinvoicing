package com.chrisjenx.invoicekit.compose.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrisjenx.invoicekit.InvoiceSection
import com.chrisjenx.invoicekit.compose.*

@Composable
internal fun MetaBlockSection(metaBlock: InvoiceSection.MetaBlock) {
    val style = LocalInvoiceStyle.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgMutedColor, RoundedCornerShape(4.dp))
            .padding(12.dp)
    ) {
        metaBlock.entries.forEach { entry ->
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = style.secondaryComposeColor)) {
                        append("${entry.label}: ")
                    }
                    withStyle(SpanStyle(color = style.textComposeColor)) {
                        append(entry.value)
                    }
                },
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
    }
}

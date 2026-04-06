package com.chrisjenx.kinvoicing.compose.sections

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrisjenx.kinvoicing.InvoiceStatus
import com.chrisjenx.kinvoicing.InvoiceStyle
import com.chrisjenx.kinvoicing.StatusDisplay
import com.chrisjenx.kinvoicing.compose.InvoiceSpacing
import com.chrisjenx.kinvoicing.compose.InvoiceTypography
import com.chrisjenx.kinvoicing.compose.toComposeColor

/**
 * Small colored pill displaying the status label. Intended to sit next to the invoice number.
 */
@Composable
public fun StatusBadge(status: InvoiceStatus) {
    val bgColor = status.color.toComposeColor()
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = InvoiceSpacing.sm, vertical = InvoiceSpacing.xxs),
    ) {
        Text(
            text = status.label,
            fontSize = InvoiceTypography.caption,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 0.5.sp,
        )
    }
}

/**
 * Full-width colored bar displayed at the top of the invoice, before sections.
 */
@Composable
public fun StatusBanner(status: InvoiceStatus, style: InvoiceStyle) {
    val bgColor = status.color.toComposeColor()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = InvoiceSpacing.lg, vertical = InvoiceSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = status.label,
            fontSize = InvoiceTypography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 1.sp,
        )
    }
}

/**
 * Large diagonal text drawn across a full-width Canvas.
 * Uses [Canvas] + [drawText] with rotation so it renders correctly
 * in both interactive Compose and compose2pdf's vector SVG pipeline.
 */
@Composable
public fun StatusWatermark(status: InvoiceStatus, display: StatusDisplay.Watermark) {
    val color = status.color.toComposeColor().copy(alpha = display.opacity)
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        fontSize = 72.sp,
        fontWeight = FontWeight.ExtraBold,
        color = color,
        letterSpacing = 8.sp,
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
    ) {
        val measured = textMeasurer.measure(status.label, textStyle)
        rotate(degrees = -30f, pivot = Offset(size.width / 2, size.height / 2)) {
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = (size.width - measured.size.width) / 2,
                    y = (size.height - measured.size.height) / 2,
                ),
            )
        }
    }
}

/**
 * Rotated stamp/seal drawn via [Canvas] in the upper-right area.
 * Uses canvas rotation so it renders in compose2pdf's vector SVG pipeline.
 */
@Composable
public fun StatusStamp(status: InvoiceStatus, display: StatusDisplay.Stamp) {
    val color = status.color.toComposeColor().copy(alpha = display.opacity)
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = color,
        letterSpacing = 2.sp,
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopEnd,
    ) {
        Canvas(
            modifier = Modifier
                .padding(top = 8.dp, end = 16.dp)
                .width(200.dp)
                .height(60.dp),
        ) {
            val measured = textMeasurer.measure(status.label, textStyle)
            val borderPadH = 20.dp.toPx()
            val borderPadV = 10.dp.toPx()
            val rectW = measured.size.width + borderPadH * 2
            val rectH = measured.size.height + borderPadV * 2

            rotate(degrees = -15f, pivot = Offset(size.width / 2, size.height / 2)) {
                val left = (size.width - rectW) / 2
                val top = (size.height - rectH) / 2
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(rectW, rectH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx()),
                )
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(
                        x = left + borderPadH,
                        y = top + borderPadV,
                    ),
                )
            }
        }
    }
}

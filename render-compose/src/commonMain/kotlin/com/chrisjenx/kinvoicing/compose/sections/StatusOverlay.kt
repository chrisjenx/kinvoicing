package com.chrisjenx.kinvoicing.compose.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
 * Returns a [Modifier] that draws large diagonal watermark text on top of the element's content.
 *
 * Uses [drawWithContent] so the watermark is drawn AFTER child content (sections),
 * appearing as a true overlay. Works in compose2pdf's vector SVG pipeline because
 * it uses canvas draw operations (not graphicsLayer).
 */
@Composable
public fun statusWatermarkModifier(status: InvoiceStatus, display: StatusDisplay.Watermark): Modifier {
    val color = status.color.toComposeColor().copy(alpha = display.opacity)
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        fontSize = 72.sp,
        fontWeight = FontWeight.ExtraBold,
        color = color,
        letterSpacing = 8.sp,
    )
    val measured = textMeasurer.measure(status.label, textStyle)

    return Modifier.drawWithContent {
        drawContent()
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
 * Returns a [Modifier] that draws a rotated stamp/seal below the header in the right area.
 *
 * Uses [drawWithContent] so the stamp is drawn AFTER child content (sections),
 * appearing as a true overlay. Works in compose2pdf's vector SVG pipeline.
 */
@Composable
public fun statusStampModifier(status: InvoiceStatus, display: StatusDisplay.Stamp): Modifier {
    val color = status.color.toComposeColor().copy(alpha = display.opacity)
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = color,
        letterSpacing = 2.sp,
    )
    val measured = textMeasurer.measure(status.label, textStyle)
    val borderPadH = 20f
    val borderPadV = 10f

    return Modifier.drawWithContent {
        drawContent()
        val rectW = measured.size.width + borderPadH * 2 * density
        val rectH = measured.size.height + borderPadV * 2 * density
        val stampX = size.width - rectW - 32 * density
        val stampY = 120 * density
        val cx = stampX + rectW / 2
        val cy = stampY + rectH / 2

        rotate(degrees = -15f, pivot = Offset(cx, cy)) {
            drawRoundRect(
                color = color,
                topLeft = Offset(stampX, stampY),
                size = Size(rectW, rectH),
                cornerRadius = CornerRadius(8 * density),
                style = Stroke(width = 3 * density),
            )
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = stampX + borderPadH * density,
                    y = stampY + borderPadV * density,
                ),
            )
        }
    }
}

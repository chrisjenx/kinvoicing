package com.chrisjenx.kinvoicing.compose.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
 * Large diagonal text overlaid across the invoice body at low opacity.
 */
@Composable
public fun StatusWatermark(status: InvoiceStatus, display: StatusDisplay.Watermark) {
    val color = status.color.toComposeColor()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = display.opacity },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = status.label,
            fontSize = 72.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            letterSpacing = 8.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.rotate(-30f),
        )
    }
}

/**
 * Rotated stamp/seal overlay in the upper-right area of the invoice.
 */
@Composable
public fun StatusStamp(status: InvoiceStatus, display: StatusDisplay.Stamp) {
    val color = status.color.toComposeColor()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 40.dp, end = 16.dp)
                .graphicsLayer {
                    rotationZ = -15f
                    alpha = display.opacity
                }
                .border(3.dp, color, RoundedCornerShape(8.dp))
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = status.label,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                letterSpacing = 2.sp,
            )
        }
    }
}

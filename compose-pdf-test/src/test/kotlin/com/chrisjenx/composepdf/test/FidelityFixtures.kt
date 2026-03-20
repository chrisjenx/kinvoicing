package com.chrisjenx.composepdf.test

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrisjenx.composepdf.PdfLink
import kotlin.math.cos
import kotlin.math.sin
import org.jetbrains.skia.Color as SkColor
import org.jetbrains.skia.Paint as SkPaint
import org.jetbrains.skia.Rect as SkRect
import org.jetbrains.skia.Surface as SkSurface

data class Fixture(
    val name: String,
    val category: String = "basic",
    val description: String = "",
    val vectorThreshold: Double = 0.15,
    val content: @Composable () -> Unit,
)

val fidelityFixtures = listOf(
    // Basic
    Fixture("simple-text", "basic", "Basic text rendering with multiple lines") { SimpleTextFixture() },
    Fixture("with-image", "basic", "Embedded bitmap image with text") { WithImageFixture() },
    // Text
    Fixture("styled-text", "text", "Bold, italic, colored, and sized text variants") { StyledTextFixture() },
    Fixture("text-wrapping", "text", "Long paragraph with soft wrap and ellipsis overflow", 0.15) { TextWrappingFixture() },
    Fixture("text-decoration", "text", "Underline, strikethrough, letter spacing, line height", 0.20) { TextDecorationFixture() },
    Fixture("text-alignment", "text", "Center, end, and justify text alignment", 0.20) { TextAlignmentFixture() },
    // Shapes
    Fixture("rectangles", "shapes", "Filled and bordered rectangles in various sizes") { RectanglesFixture() },
    Fixture("rounded-corners", "shapes", "Rounded corners including circles and asymmetric radii") { RoundedCornersFixture() },
    Fixture("custom-drawing", "shapes", "Canvas drawing: circles, rectangles, lines, and arcs") { CustomDrawingFixture() },
    Fixture("complex-path", "shapes", "Canvas paths: star, cubic and quadratic beziers") { ComplexPathFixture() },
    Fixture("clip-shapes", "shapes", "Clipped content with circle, rounded, and nested clips") { ClipShapesFixture() },
    // Layout
    Fixture("column-row-layout", "layout", "Column and Row layouts with weights and alignment") { ColumnRowLayoutFixture() },
    Fixture("borders-variety", "layout", "Various border widths, colors, shapes, and dividers") { BordersVarietyFixture() },
    Fixture("dense-grid", "layout", "8x8 grid of colored cells with text overlay", 0.40) { DenseGridFixture() },
    // Visual
    Fixture("colors-backgrounds", "visual", "Solid color backgrounds with overlay text", 0.30) { ColorsBackgroundsFixture() },
    Fixture("opacity", "visual", "Semi-transparent overlapping colored boxes") { OpacityFixture() },
    // Composite
    Fixture("invoice-like", "composite", "Invoice layout with headers, line items, and totals") { InvoiceLikeFixture() },
    Fixture("mixed-content", "composite", "Dashboard card with image, text, shapes, and backgrounds", 0.20) { MixedContentFixture() },
    Fixture("pdf-links", "composite", "PdfLink annotations: plain, button, inline, large area") { PdfLinkFixture() },
    // Image variety
    Fixture("transparent-image", "basic", "Overlapping semi-transparent circles on white + colored backgrounds") { TransparentImageFixture() },
    Fixture("gradient-image", "basic", "Horizontal gradient via thin vertical strips") { GradientImageFixture() },
    Fixture("multiple-images", "basic", "6 images in a 2x3 grid") { MultipleImagesFixture() },
    Fixture("scaled-image", "basic", "32x32 checkerboard at 32dp, 64dp, 128dp sizes") { ScaledImageFixture() },
    // Edge cases
    Fixture("deep-nesting", "edge-case", "8 levels of clip + background + padding") { DeepNestingFixture() },
    Fixture("overlapping-elements", "edge-case", "Z-order with 3 overlapping semi-transparent boxes + text") { OverlappingElementsFixture() },
    Fixture("color-bands", "edge-case", "32 thin HSL color bands") { ColorBandsFixture() },
    Fixture("empty-page", "edge-case", "Nearly blank page with 1dp black marker") { EmptyPageFixture() },
)

// ── Basic ──

@Composable
private fun SimpleTextFixture() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Hello, PDF!", fontSize = 24.sp)
        Spacer(Modifier.height(8.dp))
        Text("This is a simple text fixture for fidelity testing.")
        Spacer(Modifier.height(8.dp))
        Text("Line 3: numbers 0123456789 and symbols @#\$%")
    }
}

@Composable
private fun WithImageFixture() {
    val imageBitmap = remember {
        val w = 120
        val h = 80
        val surface = SkSurface.makeRasterN32Premul(w, h)
        val canvas = surface.canvas
        val halfW = w / 2f
        val halfH = h / 2f
        fun fill(x: Float, y: Float, fw: Float, fh: Float, r: Int, g: Int, b: Int) {
            canvas.drawRect(
                SkRect.makeXYWH(x, y, fw, fh),
                SkPaint().apply { color = SkColor.makeARGB(255, r, g, b) },
            )
        }
        fill(0f, 0f, halfW, halfH, 220, 50, 50)
        fill(halfW, 0f, halfW, halfH, 50, 100, 220)
        fill(0f, halfH, halfW, halfH, 50, 180, 80)
        fill(halfW, halfH, halfW, halfH, 240, 200, 50)
        surface.makeImageSnapshot().toComposeImageBitmap()
    }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Image Rendering Test", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Image(
            bitmap = imageBitmap,
            contentDescription = "Test pattern",
            modifier = Modifier.size(120.dp, 80.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text("Programmatic bitmap with four colored quadrants.")
    }
}

// ── Text ──

@Composable
private fun StyledTextFixture() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Bold Text", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Italic Text", fontSize = 20.sp, fontStyle = FontStyle.Italic)
        Spacer(Modifier.height(4.dp))
        Text("Large Text", fontSize = 36.sp, color = Color.Blue)
        Spacer(Modifier.height(4.dp))
        Text("Small Red Text", fontSize = 10.sp, color = Color.Red)
        Spacer(Modifier.height(4.dp))
        Text("Medium Green", fontSize = 16.sp, color = Color(0xFF006400))
    }
}

@Composable
private fun TextWrappingFixture() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            "This is a very long paragraph that should wrap across multiple lines to test how " +
                "text wrapping is rendered in PDF output. The quick brown fox jumps over the lazy " +
                "dog. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
                "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam.",
            fontSize = 14.sp,
            softWrap = true,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "This text has a max of two lines and should show ellipsis if it overflows. " +
                "Adding enough text here to make sure it overflows the available space in " +
                "this column layout to trigger the ellipsis behavior correctly.",
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TextDecorationFixture() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Underlined Text", fontSize = 18.sp, textDecoration = TextDecoration.Underline)
        Spacer(Modifier.height(8.dp))
        Text("Strikethrough Text", fontSize = 18.sp, textDecoration = TextDecoration.LineThrough)
        Spacer(Modifier.height(8.dp))
        Text(
            "Combined Decorations",
            fontSize = 18.sp,
            textDecoration = TextDecoration.Underline + TextDecoration.LineThrough,
        )
        Spacer(Modifier.height(8.dp))
        Text("Wide Letter Spacing", fontSize = 16.sp, letterSpacing = 4.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Increased line height makes this text have more vertical space between lines " +
                "when it wraps to multiple lines in the column.",
            fontSize = 14.sp,
            lineHeight = 28.sp,
        )
    }
}

@Composable
private fun TextAlignmentFixture() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            "Left aligned (default)",
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Center aligned text",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Right aligned text",
            fontSize = 16.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().background(Color(0xFFF5F5F5)).padding(8.dp)) {
            Text(
                "This is a paragraph of justified text. It should stretch to fill the full " +
                    "width of the container with even spacing between words on each line " +
                    "except the last line which remains left-aligned.",
                fontSize = 14.sp,
                textAlign = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Shapes ──

@Composable
private fun RectanglesFixture() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Box(Modifier.fillMaxWidth().height(60.dp).background(Color.Blue))
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(40.dp).background(Color.Red))
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.size(100.dp)
                .border(2.dp, Color.Black)
                .background(Color.Yellow),
        )
        Spacer(Modifier.height(8.dp))
        Row {
            Box(Modifier.size(50.dp).background(Color.Cyan))
            Spacer(Modifier.width(4.dp))
            Box(Modifier.size(50.dp).background(Color.Magenta))
            Spacer(Modifier.width(4.dp))
            Box(Modifier.size(50.dp).background(Color.Green))
        }
    }
}

@Composable
private fun RoundedCornersFixture() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Box(
            Modifier.fillMaxWidth().height(60.dp)
                .background(Color.Blue, RoundedCornerShape(12.dp)),
        )
        Spacer(Modifier.height(8.dp))
        Box(Modifier.size(80.dp).background(Color.Red, CircleShape))
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.fillMaxWidth().height(40.dp)
                .background(Color.Green, RoundedCornerShape(topStart = 20.dp, bottomEnd = 20.dp)),
        )
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.size(100.dp)
                .border(3.dp, Color.DarkGray, RoundedCornerShape(16.dp))
                .background(Color.LightGray, RoundedCornerShape(16.dp)),
        )
    }
}

@Composable
private fun CustomDrawingFixture() {
    Canvas(Modifier.fillMaxSize().padding(24.dp)) {
        drawCircle(color = Color.Red, radius = 40f, center = Offset(60f, 60f))
        drawCircle(
            color = Color.Blue, radius = 40f, center = Offset(160f, 60f),
            style = Stroke(width = 3f),
        )
        drawRect(color = Color.Green, topLeft = Offset(10f, 120f), size = Size(100f, 60f))
        drawRect(
            color = Color.DarkGray, topLeft = Offset(130f, 120f), size = Size(100f, 60f),
            style = Stroke(width = 2f),
        )
        drawLine(Color.Black, Offset(10f, 200f), Offset(250f, 200f), strokeWidth = 2f)
        drawLine(Color.Red, Offset(10f, 210f), Offset(250f, 250f), strokeWidth = 1f)
        drawArc(
            color = Color.Blue,
            startAngle = 0f, sweepAngle = 270f,
            useCenter = true,
            topLeft = Offset(10f, 270f), size = Size(80f, 80f),
        )
    }
}

@Composable
private fun ComplexPathFixture() {
    Canvas(Modifier.fillMaxSize().padding(24.dp)) {
        // 5-pointed star
        val starPath = Path().apply {
            val cx = 100f
            val cy = 100f
            val outerR = 80f
            val innerR = 35f
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) outerR else innerR
                val angle = Math.PI / 2 + i * Math.PI / 5
                val x = cx + (r * cos(angle)).toFloat()
                val y = cy - (r * sin(angle)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(starPath, Color(0xFFFF6600))
        drawPath(starPath, Color.Black, style = Stroke(width = 2f))

        // Cubic bezier curve
        val bezierPath = Path().apply {
            moveTo(10f, 250f)
            cubicTo(80f, 180f, 180f, 320f, 260f, 250f)
        }
        drawPath(bezierPath, Color.Blue, style = Stroke(width = 3f))

        // Quadratic bezier wave
        val wavePath = Path().apply {
            moveTo(10f, 350f)
            quadraticTo(70f, 300f, 130f, 350f)
            quadraticTo(190f, 400f, 250f, 350f)
        }
        drawPath(wavePath, Color(0xFF006400), style = Stroke(width = 2f))
    }
}

@Composable
private fun ClipShapesFixture() {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(100.dp).clip(CircleShape).background(Color.Red),
            contentAlignment = Alignment.Center,
        ) {
            Text("Circle", color = Color.White, fontSize = 12.sp)
        }
        Box(
            Modifier.fillMaxWidth().height(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Blue),
            contentAlignment = Alignment.Center,
        ) {
            Text("Rounded Clip", color = Color.White)
        }
        Box(
            Modifier.size(120.dp, 80.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, bottomEnd = 24.dp))
                .background(Color(0xFF6600FF)),
        )
        // Nested clips
        Box(
            Modifier.size(100.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Yellow)
                .padding(10.dp)
                .clip(CircleShape)
                .background(Color.Green),
        )
    }
}

// ── Layout ──

@Composable
private fun ColumnRowLayoutFixture() {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Header", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Left")
            Text("Center")
            Text("Right")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.weight(1f).height(30.dp).background(Color.Red))
            Box(Modifier.weight(2f).height(30.dp).background(Color.Green))
            Box(Modifier.weight(1f).height(30.dp).background(Color.Blue))
        }
        Column(
            Modifier.fillMaxWidth().padding(16.dp).background(Color.LightGray),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Centered content")
            Text("Inside padded box")
        }
    }
}

@Composable
private fun BordersVarietyFixture() {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(40.dp).border(1.dp, Color.Black))
        Box(Modifier.fillMaxWidth().height(40.dp).border(2.dp, Color.Red))
        Box(
            Modifier.fillMaxWidth().height(40.dp)
                .border(3.dp, Color.Blue, RoundedCornerShape(8.dp)),
        )
        Box(
            Modifier.fillMaxWidth().height(40.dp)
                .border(4.dp, Color(0xFF006400), RoundedCornerShape(20.dp)),
        )
        Divider(color = Color.Black, thickness = 1.dp)
        Divider(color = Color.Red, thickness = 2.dp)
        Divider(color = Color.Blue, thickness = 3.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(60.dp).border(1.dp, Color.Black).background(Color.LightGray))
            Box(
                Modifier.size(60.dp)
                    .border(2.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                    .background(Color.Cyan, RoundedCornerShape(12.dp)),
            )
            Box(
                Modifier.size(60.dp)
                    .border(3.dp, Color.Magenta, CircleShape)
                    .background(Color.Yellow, CircleShape),
            )
        }
    }
}

@Composable
private fun DenseGridFixture() {
    val gridColors = listOf(
        Color.Red, Color.Blue, Color.Green, Color.Yellow,
        Color.Cyan, Color.Magenta, Color(0xFFFF6600), Color(0xFF6600FF),
    )
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        for (row in 0 until 8) {
            Row(
                Modifier.fillMaxWidth().height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                for (col in 0 until 8) {
                    val colorIndex = (row + col) % gridColors.size
                    Box(
                        Modifier.weight(1f).fillMaxHeight().background(gridColors[colorIndex]),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${row * 8 + col}",
                            fontSize = 8.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        }
    }
}

// ── Visual ──

@Composable
private fun ColorsBackgroundsFixture() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        val colors = listOf(
            Color.Red, Color.Green, Color.Blue,
            Color.Yellow, Color.Cyan, Color.Magenta,
            Color(0xFFFF6600), Color(0xFF6600FF), Color(0xFF00FF66),
        )
        for (color in colors) {
            Box(
                Modifier.fillMaxWidth().height(24.dp).background(color),
            ) {
                Text(
                    "Color: ${color.hashCode()}",
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun OpacityFixture() {
    Box(Modifier.fillMaxSize().padding(24.dp)) {
        Box(Modifier.size(200.dp).background(Color.White))
        Box(
            Modifier.padding(start = 20.dp, top = 20.dp)
                .size(120.dp)
                .background(Color.Red.copy(alpha = 0.5f)),
        )
        Box(
            Modifier.padding(start = 60.dp, top = 60.dp)
                .size(120.dp)
                .background(Color.Blue.copy(alpha = 0.5f)),
        )
        Box(
            Modifier.padding(start = 40.dp, top = 100.dp)
                .size(120.dp)
                .background(Color.Green.copy(alpha = 0.3f)),
        )
    }
}

// ── Composite ──

@Composable
private fun InvoiceLikeFixture() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("INVOICE", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                Text("#INV-2024-001", fontSize = 14.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Acme Corp", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("123 Business St", fontSize = 12.sp, color = Color.Gray)
                Text("contact@acme.com", fontSize = 12.sp, color = Color.Blue)
            }
        }

        Spacer(Modifier.height(16.dp))
        Divider(color = Color.LightGray, thickness = 1.dp)
        Spacer(Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth().background(Color(0xFFF5F5F5)).padding(8.dp),
        ) {
            Text("Item", Modifier.weight(3f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Qty", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Price", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Total", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        val items = listOf(
            listOf("Widget Pro", "5", "\$10.00", "\$50.00"),
            listOf("Gadget Basic", "2", "\$25.00", "\$50.00"),
            listOf("Service Fee", "1", "\$100.00", "\$100.00"),
        )
        for (item in items) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(item[0], Modifier.weight(3f), fontSize = 12.sp)
                Text(item[1], Modifier.weight(1f), fontSize = 12.sp)
                Text(item[2], Modifier.weight(1f), fontSize = 12.sp)
                Text(item[3], Modifier.weight(1f), fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(8.dp))
        Divider(color = Color.LightGray, thickness = 1.dp)
        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Text("Total: ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("\$200.00", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF006400))
        }
    }
}

@Composable
private fun MixedContentFixture() {
    val imageBitmap = remember {
        val surface = SkSurface.makeRasterN32Premul(60, 60)
        val canvas = surface.canvas
        val p = SkPaint()
        p.color = SkColor.makeARGB(255, 70, 130, 180)
        canvas.drawRect(SkRect.makeXYWH(0f, 0f, 60f, 60f), p)
        p.color = SkColor.makeARGB(255, 255, 255, 255)
        canvas.drawCircle(30f, 30f, 20f, p)
        surface.makeImageSnapshot().toComposeImageBitmap()
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        // Card header
        Box(
            Modifier.fillMaxWidth()
                .background(Color(0xFF2196F3), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .padding(16.dp),
        ) {
            Text("Dashboard Card", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        // Card body
        Column(
            Modifier.fillMaxWidth()
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("John Doe", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Software Engineer", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("42", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF4CAF50))
                    Text("Tasks", fontSize = 11.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("8", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFFF9800))
                    Text("Pending", fontSize = 11.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("97%", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF2196F3))
                    Text("Score", fontSize = 11.sp, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(12.dp))
            // Progress bar
            Box(
                Modifier.fillMaxWidth().height(8.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp)),
            ) {
                Box(
                    Modifier.fillMaxWidth(0.75f).height(8.dp)
                        .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp)),
                )
            }
        }
    }
}

// ── Composite: PdfLink ──

@Composable
internal fun PdfLinkFixture() {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Plain text link
        PdfLink(href = "https://example.com") {
            Text(
                "Visit Example.com",
                fontSize = 16.sp,
                color = Color.Blue,
                textDecoration = TextDecoration.Underline,
            )
        }
        // Button-style link
        PdfLink(href = "https://github.com") {
            Box(
                Modifier
                    .background(Color(0xFF2196F3), RoundedCornerShape(8.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text("GitHub", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        // Multiple inline links
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PdfLink(href = "https://a.com") {
                Text("Link A", color = Color.Blue, fontSize = 14.sp)
            }
            PdfLink(href = "https://b.com") {
                Text("Link B", color = Color.Red, fontSize = 14.sp)
            }
            PdfLink(href = "https://c.com") {
                Text("Link C", color = Color(0xFF006400), fontSize = 14.sp)
            }
        }
        // Large clickable area
        PdfLink(href = "https://large-area.com") {
            Box(
                Modifier.fillMaxWidth().height(80.dp)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("Large Clickable Area", fontSize = 18.sp, color = Color.DarkGray)
            }
        }
    }
}

// ── Image Variety ──

@Composable
private fun TransparentImageFixture() {
    val imageBitmap = remember {
        val w = 160
        val h = 120
        val surface = SkSurface.makeRasterN32Premul(w, h)
        val canvas = surface.canvas
        // White background
        canvas.drawRect(
            SkRect.makeXYWH(0f, 0f, w.toFloat(), h.toFloat()),
            SkPaint().apply { color = SkColor.makeARGB(255, 255, 255, 255) },
        )
        // Semi-transparent overlapping circles
        canvas.drawCircle(50f, 50f, 40f, SkPaint().apply { color = SkColor.makeARGB(128, 255, 0, 0) })
        canvas.drawCircle(90f, 50f, 40f, SkPaint().apply { color = SkColor.makeARGB(128, 0, 0, 255) })
        canvas.drawCircle(70f, 80f, 40f, SkPaint().apply { color = SkColor.makeARGB(128, 0, 180, 0) })
        surface.makeImageSnapshot().toComposeImageBitmap()
    }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Transparent Image Test", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        // On white background
        Image(bitmap = imageBitmap, contentDescription = "Transparent on white", modifier = Modifier.size(160.dp, 120.dp))
        Spacer(Modifier.height(12.dp))
        // On colored background
        Box(Modifier.background(Color(0xFFFFE0B2)).padding(8.dp)) {
            Image(bitmap = imageBitmap, contentDescription = "Transparent on orange", modifier = Modifier.size(160.dp, 120.dp))
        }
    }
}

@Composable
private fun GradientImageFixture() {
    val imageBitmap = remember {
        val w = 256
        val h = 60
        val surface = SkSurface.makeRasterN32Premul(w, h)
        val canvas = surface.canvas
        // Draw thin vertical strips to simulate a gradient
        for (x in 0 until w) {
            val ratio = x.toFloat() / (w - 1)
            val r = (255 * (1f - ratio)).toInt()
            val g = (255 * ratio).toInt()
            val b = 128
            canvas.drawRect(
                SkRect.makeXYWH(x.toFloat(), 0f, 1f, h.toFloat()),
                SkPaint().apply { color = SkColor.makeARGB(255, r, g, b) },
            )
        }
        surface.makeImageSnapshot().toComposeImageBitmap()
    }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Gradient Image Test", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Image(bitmap = imageBitmap, contentDescription = "Gradient", modifier = Modifier.fillMaxWidth().height(60.dp))
        Spacer(Modifier.height(8.dp))
        Text("Red to Green horizontal gradient with constant blue channel", fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun MultipleImagesFixture() {
    val images = remember {
        val colors = listOf(
            Triple(220, 50, 50), Triple(50, 100, 220), Triple(50, 180, 80),
            Triple(240, 200, 50), Triple(180, 50, 220), Triple(50, 200, 200),
        )
        colors.map { (r, g, b) ->
            val surface = SkSurface.makeRasterN32Premul(60, 60)
            val canvas = surface.canvas
            canvas.drawRect(
                SkRect.makeXYWH(0f, 0f, 60f, 60f),
                SkPaint().apply { color = SkColor.makeARGB(255, r, g, b) },
            )
            // Draw a white diagonal line
            canvas.drawLine(0f, 0f, 60f, 60f, SkPaint().apply {
                color = SkColor.makeARGB(200, 255, 255, 255)
                strokeWidth = 3f
            })
            surface.makeImageSnapshot().toComposeImageBitmap()
        }
    }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Multiple Images (2x3 Grid)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        for (row in 0 until 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (col in 0 until 3) {
                    Image(
                        bitmap = images[row * 3 + col],
                        contentDescription = "Image ${row * 3 + col}",
                        modifier = Modifier.size(80.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ScaledImageFixture() {
    val checkerboard = remember {
        val size = 32
        val surface = SkSurface.makeRasterN32Premul(size, size)
        val canvas = surface.canvas
        val cellSize = 4f
        for (row in 0 until (size / cellSize.toInt())) {
            for (col in 0 until (size / cellSize.toInt())) {
                val isWhite = (row + col) % 2 == 0
                canvas.drawRect(
                    SkRect.makeXYWH(col * cellSize, row * cellSize, cellSize, cellSize),
                    SkPaint().apply {
                        color = if (isWhite) SkColor.makeARGB(255, 255, 255, 255)
                        else SkColor.makeARGB(255, 0, 0, 0)
                    },
                )
            }
        }
        surface.makeImageSnapshot().toComposeImageBitmap()
    }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Scaled Image Test", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(bitmap = checkerboard, contentDescription = "32dp", modifier = Modifier.size(32.dp))
                Text("32dp", fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(bitmap = checkerboard, contentDescription = "64dp", modifier = Modifier.size(64.dp))
                Text("64dp", fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(bitmap = checkerboard, contentDescription = "128dp", modifier = Modifier.size(128.dp))
                Text("128dp", fontSize = 10.sp)
            }
        }
    }
}

// ── Edge Cases ──

@Composable
private fun DeepNestingFixture() {
    val colors = listOf(
        Color(0xFFE53935), Color(0xFFFF9800), Color(0xFFFDD835),
        Color(0xFF43A047), Color(0xFF1E88E5), Color(0xFF5E35B1),
        Color(0xFFE91E63), Color(0xFF00ACC1),
    )
    var content: @Composable () -> Unit = {
        Text("Deep!", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
    for (i in colors.indices.reversed()) {
        val inner = content
        val color = colors[i]
        content = {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(RoundedCornerShape((4 + i * 2).dp))
                    .background(color)
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                inner()
            }
        }
    }
    Box(Modifier.fillMaxSize().padding(24.dp)) {
        content()
    }
}

@Composable
private fun OverlappingElementsFixture() {
    Box(Modifier.fillMaxSize().padding(24.dp)) {
        // White base
        Box(Modifier.fillMaxSize().background(Color.White))
        // Three overlapping semi-transparent boxes
        Box(
            Modifier.padding(start = 20.dp, top = 40.dp)
                .size(180.dp, 120.dp)
                .background(Color.Red.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
        )
        Box(
            Modifier.padding(start = 80.dp, top = 80.dp)
                .size(180.dp, 120.dp)
                .background(Color.Blue.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
        )
        Box(
            Modifier.padding(start = 50.dp, top = 140.dp)
                .size(180.dp, 120.dp)
                .background(Color.Green.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
        )
        // Text on top
        Box(
            Modifier.padding(start = 60.dp, top = 100.dp)
                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                .padding(8.dp),
        ) {
            Text("Z-Order Test", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
private fun ColorBandsFixture() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("32 Color Bands", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        for (i in 0 until 32) {
            val hue = i * (360f / 32f)
            // HSL to RGB approximation
            val c = hslToColor(hue, 0.7f, 0.5f)
            Box(
                Modifier.fillMaxWidth().height(12.dp).background(c),
            )
        }
    }
}

@Composable
private fun EmptyPageFixture() {
    Box(Modifier.fillMaxSize()) {
        // Nearly blank — just a tiny 1dp marker in the top-left corner
        Box(
            Modifier.padding(start = 1.dp, top = 1.dp)
                .size(1.dp)
                .background(Color.Black),
        )
    }
}

// ── Helpers ──

private fun hslToColor(h: Float, s: Float, l: Float): Color {
    val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f
    val (r, g, b) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(r + m, g + m, b + m)
}

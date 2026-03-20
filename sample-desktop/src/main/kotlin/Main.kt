import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.singleWindowApplication
import com.chrisjenx.composepdf.PdfLink
import com.chrisjenx.composepdf.PdfMargins
import com.chrisjenx.composepdf.PdfPageConfig
import com.chrisjenx.composepdf.RenderMode
import com.chrisjenx.composepdf.renderToPdf
import java.io.File
import org.jetbrains.skia.Color as SkColor
import org.jetbrains.skia.Paint as SkPaint
import org.jetbrains.skia.Rect as SkRect
import org.jetbrains.skia.Surface as SkSurface

fun main() = singleWindowApplication(title = "compose-pdf sample") {
    var status by remember { mutableStateOf("Click to generate PDF") }

    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text("compose-pdf demo", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    status = "Generating vector PDF..."
                    try {
                        val config = PdfPageConfig.A4.copy(margins = PdfMargins.Normal)
                        val bytes = renderToPdf(pages = 2, config = config, mode = RenderMode.VECTOR) { pageIndex ->
                            InvoicePage(pageIndex)
                        }
                        val file = File("sample-vector.pdf")
                        file.writeBytes(bytes)
                        status = "Vector PDF: ${bytes.size} bytes → ${file.absolutePath}"
                    } catch (e: Exception) {
                        status = "Error: ${e.message}"
                    }
                }) {
                    Text("Export Vector PDF")
                }

                Button(onClick = {
                    status = "Generating raster PDF..."
                    try {
                        val config = PdfPageConfig.A4.copy(margins = PdfMargins.Normal)
                        val bytes = renderToPdf(pages = 2, config = config, mode = RenderMode.RASTER) { pageIndex ->
                            InvoicePage(pageIndex)
                        }
                        val file = File("sample-raster.pdf")
                        file.writeBytes(bytes)
                        status = "Raster PDF: ${bytes.size} bytes → ${file.absolutePath}"
                    } catch (e: Exception) {
                        status = "Error: ${e.message}"
                    }
                }) {
                    Text("Export Raster PDF")
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(status)
        }
    }
}

@Composable
fun InvoicePage(pageIndex: Int) {
    val logo = remember {
        val surface = SkSurface.makeRasterN32Premul(40, 40)
        val canvas = surface.canvas
        canvas.drawRect(
            SkRect.makeXYWH(0f, 0f, 40f, 40f),
            SkPaint().apply { color = SkColor.makeARGB(255, 33, 150, 243) },
        )
        canvas.drawRect(
            SkRect.makeXYWH(8f, 8f, 24f, 24f),
            SkPaint().apply { color = SkColor.makeARGB(255, 255, 255, 255) },
        )
        surface.makeImageSnapshot().toComposeImageBitmap()
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    "INVOICE",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                )
                Text("#INV-2024-001", fontSize = 14.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Image(
                    bitmap = logo,
                    contentDescription = "Company logo",
                    modifier = Modifier.size(32.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text("Acme Corp", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("123 Business St", fontSize = 12.sp, color = Color.Gray)
                PdfLink(href = "mailto:contact@acme.com") {
                    Text("contact@acme.com", fontSize = 12.sp, color = Color.Blue)
                }
                PdfLink(href = "https://acme.example.com") {
                    Text("acme.example.com", fontSize = 12.sp, color = Color.Blue)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
        Spacer(Modifier.height(16.dp))

        // Decorative shape
        Box(
            Modifier.fillMaxWidth().height(4.dp)
                .background(Color(0xFF2196F3), RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.height(16.dp))

        // Line items
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
        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Text("Total: ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("\$200.00", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF006400))
        }

        Spacer(Modifier.height(24.dp))

        // Custom drawing
        Canvas(Modifier.fillMaxWidth().height(40.dp)) {
            val w = size.width
            drawLine(Color(0xFF2196F3), Offset(0f, 20f), Offset(w, 20f), strokeWidth = 2f)
            drawCircle(Color(0xFF2196F3), radius = 6f, center = Offset(0f, 20f))
            drawCircle(Color(0xFF2196F3), radius = 6f, center = Offset(w, 20f))
        }

        Spacer(Modifier.height(8.dp))
        Text("Page ${pageIndex + 1} of 2", fontSize = 10.sp, color = Color.Gray)
    }
}

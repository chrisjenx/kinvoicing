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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrisjenx.composepdf.PdfMargins
import com.chrisjenx.composepdf.PdfPageConfig
import com.chrisjenx.composepdf.renderToPdf
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText(
                    """
                    <h1>compose-pdf Ktor Sample</h1>
                    <p><a href="/invoice.pdf">Download Invoice PDF</a></p>
                    """.trimIndent(),
                    ContentType.Text.Html,
                )
            }

            get("/invoice.pdf") {
                val config = PdfPageConfig.A4.copy(
                    margins = PdfMargins(top = 36.dp, bottom = 36.dp, left = 36.dp, right = 36.dp)
                )
                val pdfBytes = renderToPdf(config = config) {
                    InvoiceContent()
                }
                call.respondBytes(pdfBytes, ContentType.Application.Pdf, HttpStatusCode.OK)
            }
        }
    }.start(wait = true)
}

@Composable
private fun InvoiceContent() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
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
            }
        }

        Spacer(Modifier.height(16.dp))
        Box(
            Modifier.fillMaxWidth().height(3.dp)
                .background(Color(0xFF2196F3), RoundedCornerShape(1.dp))
        )
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth().background(Color(0xFFF5F5F5)).padding(8.dp)) {
            Text("Item", Modifier.weight(3f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Qty", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Price", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Total", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        val items = listOf(
            listOf("Widget Pro", "5", "\$10.00", "\$50.00"),
            listOf("Gadget Basic", "2", "\$25.00", "\$50.00"),
            listOf("Consulting", "1", "\$100.00", "\$100.00"),
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

        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
            Text("Total: ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("\$200.00", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF006400))
        }

        Spacer(Modifier.height(24.dp))
        Text("Generated headlessly via compose-pdf + Ktor", fontSize = 10.sp, color = Color.Gray)
    }
}

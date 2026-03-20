import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.chrisjenx.composepdf.PdfPageConfig
import com.chrisjenx.composepdf.PdfMargins
import com.chrisjenx.composepdf.renderToPdf
import java.io.File

fun main() = singleWindowApplication(title = "compose-pdf sample") {
    var status by remember { mutableStateOf("Click to generate PDF") }

    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text("compose-pdf demo", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                status = "Generating..."
                try {
                    val config = PdfPageConfig.A4.copy(margins = PdfMargins.Normal)
                    val bytes = renderToPdf(pages = 2, config = config) { pageIndex ->
                        InvoicePage(pageIndex)
                    }
                    val file = File("sample-output.pdf")
                    file.writeBytes(bytes)
                    status = "Saved ${bytes.size} bytes to ${file.absolutePath}"
                } catch (e: Exception) {
                    status = "Error: ${e.message}"
                }
            }) {
                Text("Export PDF")
            }
            Spacer(Modifier.height(8.dp))
            Text(status)
        }
    }
}

@androidx.compose.runtime.Composable
fun InvoicePage(pageIndex: Int) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Sample Invoice", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text("Page ${pageIndex + 1}")
        Spacer(Modifier.height(16.dp))
        Text("This is a demonstration of compose-pdf rendering Compose UI to PDF.")
    }
}

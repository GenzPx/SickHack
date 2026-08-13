package self.apk.sickhack.genz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import self.apk.sickhack.genz.core.scanner.Scanner
import self.apk.sickhack.genz.ui.components.OutputBox
import self.apk.sickhack.genz.ui.components.SectionTitle
import self.apk.sickhack.genz.ui.components.TButton
import self.apk.sickhack.genz.ui.components.TInput
import self.apk.sickhack.genz.ui.components.TerminalScaffold
import self.apk.sickhack.genz.ui.components.rememberCopier
import self.apk.sickhack.genz.ui.theme.TerminalCyan
import self.apk.sickhack.genz.ui.theme.TerminalGreen
import self.apk.sickhack.genz.ui.theme.TerminalRed
import self.apk.sickhack.genz.ui.theme.TerminalYellow

@Composable
fun AutoScannerScreen(onBack: () -> Unit) {
    var target by rememberSaveable { mutableStateOf("") }
    var progressText by remember { mutableStateOf("// idle") }
    var done by remember { mutableStateOf(0) }
    var total by remember { mutableStateOf(0) }
    var findings by remember { mutableStateOf<List<Scanner.Finding>>(emptyList()) }
    var running by remember { mutableStateOf(false) }
    var job: Job? = null
    val scope = rememberCoroutineScope()
    val copy = rememberCopier()

    TerminalScaffold(title = "Auto Scanner", onBack = onBack) {
        SectionTitle("target")
        TInput(
            value = target,
            onValueChange = { target = it },
            label = "target url",
            placeholder = "http://target.com/page.php?id=1"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = if (running) "RUNNING..." else "RUN FULL SCAN",
                color = TerminalGreen,
                enabled = target.isNotBlank() && !running,
                onClick = {
                    findings = emptyList()
                    done = 0
                    total = 0
                    running = true
                    progressText = "// menyiapkan payload..."
                    job = scope.launch {
                        val result = Scanner.fullScan(
                            target = target,
                            onProgress = { p ->
                                progressText = p.text
                                done = p.done
                                total = p.total
                            },
                            isCancelled = { !running }
                        )
                        findings = result
                        running = false
                        progressText = "// selesai — ${result.size} temuan"
                    }
                }
            )
            TButton(
                text = "CANCEL",
                color = TerminalRed,
                enabled = running,
                onClick = {
                    running = false
                    job?.cancel()
                    progressText = "// dibatalkan"
                }
            )
            TButton(
                text = "COPY REPORT",
                color = TerminalCyan,
                enabled = findings.isNotEmpty() || progressText.contains("selesai"),
                onClick = {
                    val report = buildString {
                        append("SICKHACK SCAN REPORT\n")
                        append("TARGET: $target\n")
                        append("WAKTU: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n")
                        append("=".repeat(40)).append('\n')
                        if (findings.isEmpty()) append("// tidak ada temuan signifikan\n")
                        findings.forEach { append(it.full()).append("\n\n") }
                    }
                    copy(report)
                }
            )
        }
        if (total > 0) {
            LinearProgressIndicator(
                progress = { (done.toFloat() / total.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = TerminalGreen
            )
        }
        Text(
            text = progressText,
            color = TerminalGreen,
            fontSize = 11.sp,
            style = MaterialTheme.typography.bodySmall
        )
        SectionTitle("temuan [${findings.size}]")
        if (findings.isEmpty()) {
            OutputBox(text = "", emptyText = "// belum ada temuan — jalankan scan", height = 90)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                findings.forEach { f ->
                    val color = when (f.severity) {
                        "HIGH" -> TerminalRed
                        "MEDIUM" -> TerminalYellow
                        else -> TerminalCyan
                    }
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(
                            text = f.line(),
                            color = color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "  URL: ${f.url}\n  PAYLOAD: ${f.payload}",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                            fontSize = 10.sp,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

package self.apk.sickhack.genz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.launch
import self.apk.sickhack.genz.core.net.HttpClient
import self.apk.sickhack.genz.core.payloads.Payloads
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
fun AdminFinderScreen(onBack: () -> Unit) {
    var target by rememberSaveable { mutableStateOf("") }
    var progress by remember { mutableStateOf("// idle") }
    var found by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val copy = rememberCopier()

    TerminalScaffold(title = "Admin Finder", onBack = onBack) {
        SectionTitle("target")
        TInput(value = target, onValueChange = { target = it }, label = "base url", placeholder = "https://target.com")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = if (busy) "SCANNING..." else "SCAN ${Payloads.adminPaths.size} PATHS",
                color = TerminalGreen,
                enabled = target.isNotBlank() && !busy,
                onClick = {
                    busy = true
                    found = emptyList()
                    scope.launch {
                        val base = target.trim().trimEnd('/')
                        val results = mutableListOf<Pair<String, Int>>()
                        Payloads.adminPaths.forEachIndexed { i, path ->
                            progress = "[${i + 1}/${Payloads.adminPaths.size}] $path"
                            val resp = HttpClient.get(base + path)
                            if (resp.statusCode != 404 && resp.statusCode != 0) {
                                results.add(Pair(path, resp.statusCode))
                            }
                        }
                        found = results
                        progress = "// selesai — ${results.size} path ditemukan"
                        busy = false
                    }
                }
            )
            TButton(
                text = "COPY",
                color = TerminalCyan,
                enabled = found.isNotEmpty(),
                onClick = {
                    copy(found.joinToString("\n") { (p, c) -> "$c  $p" })
                }
            )
        }
        Text(progress, color = TerminalGreen, fontSize = 11.sp)

        SectionTitle("ditemukan [${found.size}]")
        if (found.isEmpty()) {
            OutputBox(text = "", emptyText = "// belum ada hasil", height = 90)
        } else {
            found.forEach { (p, c) ->
                val color = if (c in 200..299) TerminalGreen else if (c in 300..399) TerminalYellow else TerminalCyan
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        text = "$c",
                        color = color,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = androidx.compose.ui.Modifier.padding(end = 10.dp)
                    )
                    Text(
                        text = p,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Text(
            text = "// 200/301/302/403 biasanya menandakan path aktif",
            color = TerminalRed,
            fontSize = 10.sp
        )
    }
}

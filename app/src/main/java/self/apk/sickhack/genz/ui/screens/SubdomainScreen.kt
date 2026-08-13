package self.apk.sickhack.genz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import java.net.InetAddress

@Composable
fun SubdomainScreen(onBack: () -> Unit) {
    var domain by rememberSaveable { mutableStateOf("") }
    var progress by remember { mutableStateOf("// idle") }
    var results by remember { mutableStateOf<List<String>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val copy = rememberCopier()

    TerminalScaffold(title = "Subdomain", onBack = onBack) {
        SectionTitle("domain")
        TInput(value = domain, onValueChange = { domain = it }, label = "domain", placeholder = "example.com")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = "crt.sh ENUM",
                color = TerminalGreen,
                enabled = domain.isNotBlank() && !busy,
                onClick = {
                    busy = true
                    results = emptyList()
                    scope.launch {
                        progress = "// querying crt.sh..."
                        val resp = HttpClient.get("https://crt.sh/?q=%25.$domain&output=json")
                        val set = LinkedHashSet<String>()
                        if (resp.statusCode == 200) {
                            try {
                                val arr = org.json.JSONArray(resp.body)
                                for (i in 0 until arr.length()) {
                                    val obj = arr.getJSONObject(i)
                                    val nv = obj.optString("name_value", "")
                                    nv.split("\n").forEach { line ->
                                        val clean = line.trim().lowercase().removePrefix("*.")
                                        if (clean.endsWith(domain) && clean.isNotBlank()) set.add(clean)
                                    }
                                }
                            } catch (e: Exception) {
                                progress = "// parse error: ${e.message}"
                            }
                        } else {
                            progress = "// crt.sh status ${resp.statusCode}"
                        }
                        results = set.sorted().take(200)
                        progress = "// selesai — ${results.size} subdomain dari crt.sh"
                        busy = false
                    }
                }
            )
            TButton(
                text = "DNS BRUTE",
                color = TerminalCyan,
                enabled = domain.isNotBlank() && !busy,
                onClick = {
                    busy = true
                    results = emptyList()
                    scope.launch {
                        progress = "// brute ${Payloads.dnsBruteNames.size} nama..."
                        val found = withContext(Dispatchers.IO) {
                            val f = mutableListOf<String>()
                            Payloads.dnsBruteNames.forEach { name ->
                                val host = "$name.$domain"
                                try {
                                    val ips = InetAddress.getAllByName(host)
                                    if (ips.isNotEmpty()) {
                                        val ip = ips.first().hostAddress ?: "?"
                                        f.add("$host -> $ip")
                                    }
                                } catch (e: Exception) {
                                    // NXDOMAIN — skip
                                }
                            }
                            f
                        }
                        results = found
                        progress = "// selesai — ${found.size} subdomain ditemukan"
                        busy = false
                    }
                }
            )
            TButton(text = "COPY", color = TerminalGreen, enabled = results.isNotEmpty(), onClick = { copy(results.joinToString("\n")) })
        }

        Text(progress, color = TerminalGreen, fontSize = 11.sp)
        SectionTitle("hasil [${results.size}]")
        if (results.isEmpty()) {
            OutputBox(text = "", emptyText = "// belum ada hasil", height = 90)
        } else {
            results.forEach { r ->
                Text(
                    text = r,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

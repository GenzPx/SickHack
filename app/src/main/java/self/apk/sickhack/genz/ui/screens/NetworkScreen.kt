package self.apk.sickhack.genz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
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
import java.net.InetAddress

@Composable
fun NetworkScreen(onBack: () -> Unit) {
    var host by rememberSaveable { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val copy = rememberCopier()

    TerminalScaffold(title = "Network", onBack = onBack) {
        SectionTitle("host / IP")
        TInput(value = host, onValueChange = { host = it }, label = "host", placeholder = "target.com atau 8.8.8.8")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = "PORT SCAN",
                color = TerminalGreen,
                enabled = host.isNotBlank() && !busy,
                onClick = {
                    busy = true
                    output = "// scanning ${Payloads.scanPorts.size} port..."
                    scope.launch {
                        val res = withContext(Dispatchers.IO) {
                            val sb = StringBuilder()
                            Payloads.scanPorts.forEachIndexed { i, port ->
                                if (i % 8 == 0) sb.append("// [${i}/${Payloads.scanPorts.size}]...\n")
                                val open = HttpClient.tcpConnect(host.trim(), port)
                                if (open) {
                                    sb.append("OPEN   $port\n")
                                }
                            }
                            sb.toString()
                        }
                        output = "// hasil port scan $host:\n$res"
                        busy = false
                    }
                }
            )
            TButton(
                text = "DNS LOOKUP",
                color = TerminalCyan,
                enabled = host.isNotBlank() && !busy,
                onClick = {
                    busy = true
                    scope.launch {
                        output = withContext(Dispatchers.IO) {
                            try {
                                val addrs = InetAddress.getAllByName(host.trim())
                                "// DNS $host:\n" + addrs.map { "  " + it.hostAddress }.joinToString("\n")
                            } catch (e: Exception) {
                                "// DNS gagal: ${e.message}"
                            }
                        }
                        busy = false
                    }
                }
            )
            TButton(
                text = "IP INFO",
                color = TerminalCyan,
                enabled = host.isNotBlank() && !busy,
                onClick = {
                    busy = true
                    output = "// query ip-api.com..."
                    scope.launch {
                        output = withContext(Dispatchers.IO) {
                            try {
                                val target = host.trim()
                                val q = if (target.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}$"))) target else target
                                val resp = HttpClient.get("http://ip-api.com/json/$q")
                                if (resp.statusCode == 200) {
                                    val j = JSONObject(resp.body)
                                    if (j.optString("status") == "success") {
                                        "IP: ${j.optString("query")}\n" +
                                            "negara: ${j.optString("country")} (${j.optString("countryCode")})\n" +
                                            "region: ${j.optString("regionName")}\n" +
                                            "kota: ${j.optString("city")}\n" +
                                            "ISP: ${j.optString("isp")}\n" +
                                            "org: ${j.optString("org")}\n" +
                                            "AS: ${j.optString("as")}\n" +
                                            "lat/lon: ${j.optString("lat")}, ${j.optString("lon")}\n" +
                                            "timezone: ${j.optString("timezone")}"
                                    } else {
                                        "// ip-api: ${j.optString("message")}"
                                    }
                                } else {
                                    "// ip-api status ${resp.statusCode}"
                                }
                            } catch (e: Exception) {
                                "// error: ${e.message}"
                            }
                        }
                        busy = false
                    }
                }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "COPY", color = TerminalGreen, enabled = output.isNotEmpty(), onClick = { copy(output) })
            TButton(text = "CLEAR", color = TerminalRed, enabled = output.isNotEmpty(), onClick = { output = "" })
        }
        Text("// 24 port umum: 21 22 23 25 53 80 110 135 139 143 443 445 993 995 1433 1521 2049 2375 3306 3389 5432 5900 6379 8080 8443 9090", color = self.apk.sickhack.genz.ui.theme.TerminalGreenDim, fontSize = 10.sp)
        OutputBox(output, height = 340)
    }
}

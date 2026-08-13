package self.apk.sickhack.genz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import self.apk.sickhack.genz.core.net.HttpClient
import self.apk.sickhack.genz.core.scanner.Scanner
import self.apk.sickhack.genz.ui.components.OutputBox
import self.apk.sickhack.genz.ui.components.SectionTitle
import self.apk.sickhack.genz.ui.components.TButton
import self.apk.sickhack.genz.ui.components.TInput
import self.apk.sickhack.genz.ui.components.TerminalScaffold
import self.apk.sickhack.genz.ui.components.rememberCopier
import self.apk.sickhack.genz.ui.theme.TerminalCyan
import self.apk.sickhack.genz.ui.theme.TerminalGreen
import self.apk.sickhack.genz.ui.theme.TerminalYellow

@Composable
fun AutoDiagnoseScreen(onBack: () -> Unit) {
    var target by rememberSaveable { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val copy = rememberCopier()

    val securityHeaders = listOf(
        "Strict-Transport-Security",
        "Content-Security-Policy",
        "X-Frame-Options",
        "X-Content-Type-Options",
        "Referrer-Policy",
        "Permissions-Policy",
        "X-XSS-Protection"
    )

    val wafMarkers = listOf(
        "mod_security", "modsecurity", "cloudflare", "sucuri", "imperva",
        "akamai", "request rejected", "access denied", "blocked by",
        "forbidden", "owasp", "barracuda", "incapsula", "reblaze", "qrator"
    )

    TerminalScaffold(title = "Auto Diagnose", onBack = onBack) {
        SectionTitle("target")
        TInput(value = target, onValueChange = { target = it }, label = "url", placeholder = "https://target.com")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = if (busy) "RUNNING..." else "DIAGNOSE",
                color = TerminalGreen,
                enabled = target.isNotBlank() && !busy,
                onClick = {
                    busy = true
                    output = "// menjalankan diagnose..."
                    scope.launch {
                        output = try {
                            val base = target.trim()
                            val normal = HttpClient.get(base)
                            val injected = Scanner.injectParam(base, "sickhack", "<script>alert(1)</script>")
                            val xss = HttpClient.get(injected)
                            val sb = StringBuilder()
                            sb.append("== SECURITY HEADERS ==\n")
                            securityHeaders.forEach { h ->
                                val v = normal.responseHeaders.entries.firstOrNull { it.key.equals(h, true) }?.value
                                sb.append(if (v != null) "[OK]    $h: ${v.take(60)}\n" else "[MISS]  $h\n")
                            }
                            sb.append("\n== WAF DETECTION ==\n")
                            sb.append("response normal: ${normal.statusCode}\n")
                            sb.append("response dengan payload XSS: ${xss.statusCode}\n")
                            val bodyLower = xss.body.lowercase()
                            val server = normal.responseHeaders.entries.firstOrNull { it.key.equals("Server", true) }?.value
                            val cfRay = normal.responseHeaders.entries.firstOrNull { it.key.contains("cf-ray", true) }?.value
                            val hit = wafMarkers.firstOrNull { bodyLower.contains(it) }
                                ?: server?.lowercase()?.let { s -> wafMarkers.firstOrNull { s.contains(it) } }
                                ?: if (cfRay != null) "cloudflare (cf-ray: $cfRay)" else null
                            if (xss.statusCode in listOf(403, 406, 429, 503) && xss.statusCode != normal.statusCode) {
                                sb.append("[WAF?] status beda: normal=${normal.statusCode}, payload=${xss.statusCode}\n")
                            }
                            if (hit != null) {
                                sb.append("[WAF!] terdeteksi indikator: $hit\n")
                            } else {
                                sb.append("[-]    tidak ada indikator WAF kuat\n")
                            }
                            if (server != null) sb.append("server: $server\n")
                            if (cfRay != null) sb.append("cf-ray: $cfRay\n")

                            sb.append("\n== XSS REFLECTION TEST ==\n")
                            if (xss.body.contains("<script") || xss.body.contains("<img src=x")) {
                                sb.append("[VULN] payload XSS ter-reflect di body!\n")
                            } else {
                                sb.append("[-]    tidak ter-reflect (atau di-encode)\n")
                            }
                            sb.append("\n== SERVER HEADERS ==\n")
                            normal.responseHeaders.forEach { (k, v) -> sb.append("$k: ${v.take(80)}\n") }
                            sb.toString()
                        } catch (e: Exception) {
                            "// error: ${e.message}"
                        } finally {
                            busy = false
                        }
                    }
                }
            )
            TButton(text = "COPY", color = TerminalCyan, enabled = output.isNotEmpty(), onClick = { copy(output) })
        }
        Text("// audit 7 security header + deteksi WAF + tes refleksi XSS", color = TerminalYellow, fontSize = 10.sp)
        OutputBox(output, height = 400)
    }
}

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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import self.apk.sickhack.genz.core.net.HttpClient
import self.apk.sickhack.genz.core.payloads.Payloads
import self.apk.sickhack.genz.ui.components.CategoryTabs
import self.apk.sickhack.genz.ui.components.OutputBox
import self.apk.sickhack.genz.ui.components.Pill
import self.apk.sickhack.genz.ui.components.SectionTitle
import self.apk.sickhack.genz.ui.components.TButton
import self.apk.sickhack.genz.ui.components.TInput
import self.apk.sickhack.genz.ui.components.TerminalScaffold
import self.apk.sickhack.genz.ui.components.rememberCopier
import self.apk.sickhack.genz.ui.theme.TerminalCyan
import self.apk.sickhack.genz.ui.theme.TerminalGreen

@Composable
fun RequestScreen(onBack: () -> Unit) {
    val methods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD")
    var method by rememberSaveable { mutableStateOf(0) }
    var url by rememberSaveable { mutableStateOf("") }
    var headersRaw by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val copy = rememberCopier()

    TerminalScaffold(title = "Request Builder", onBack = onBack) {
        SectionTitle("method")
        CategoryTabs(tabs = methods, selected = method, onSelect = { method = it })

        SectionTitle("target")
        TInput(value = url, onValueChange = { url = it }, label = "url", placeholder = "https://target.com/api")

        SectionTitle("headers (satu per baris: Key: Value)")
        TInput(value = headersRaw, onValueChange = { headersRaw = it }, label = "headers", minLines = 4)

        SectionTitle("user-agent quick pick")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Payloads.userAgents.take(4).forEach { ua ->
                Pill(text = ua.split("/")[0].substringAfterLast(" ").takeIf { it.isNotBlank() } ?: "UA", onClick = {
                    headersRaw = headersRaw.lines().filter { !it.startsWith("User-Agent", true) }.joinToString("\n").let {
                        (if (it.isBlank()) "" else it + "\n") + "User-Agent: $ua"
                    }
                })
            }
        }

        SectionTitle("body (untuk POST/PUT)")
        TInput(value = body, onValueChange = { body = it }, label = "body", minLines = 3)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = if (busy) "SENDING..." else "SEND",
                color = TerminalGreen,
                enabled = url.isNotBlank() && !busy,
                onClick = {
                    busy = true
                    result = "// mengirim..."
                    scope.launch {
                        val headers = HttpClient.parseHeaders(headersRaw)
                        result = try {
                            val m = methods[method]
                            val resp = if (body.isBlank()) {
                                HttpClient.execute(m, url, headers)
                            } else {
                                HttpClient.execute(m, url, headers, body)
                            }
                            buildString {
                                append(resp.summary()).append('\n')
                                append("--- REQUEST HEADERS ---\n")
                                headers.forEach { (k, v) -> append("$k: $v\n") }
                                if (body.isNotBlank()) append("BODY: $body\n")
                                append("--- RESPONSE HEADERS ---\n")
                                resp.responseHeaders.forEach { (k, v) -> append("$k: $v\n") }
                                append("--- BODY ---\n")
                                append(resp.body.take(4000))
                            }
                        } catch (e: Exception) {
                            "// error: ${e.message}"
                        } finally {
                            busy = false
                        }
                    }
                }
            )
            TButton(text = "COPY", color = TerminalCyan, enabled = result.isNotEmpty(), onClick = { copy(result) })
        }
        OutputBox(result, height = 320)
    }
}

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
import self.apk.sickhack.genz.core.net.HttpResponse
import self.apk.sickhack.genz.ui.components.CategoryTabs
import self.apk.sickhack.genz.ui.components.OutputBox
import self.apk.sickhack.genz.ui.components.SectionTitle
import self.apk.sickhack.genz.ui.components.TButton
import self.apk.sickhack.genz.ui.components.TInput
import self.apk.sickhack.genz.ui.components.TerminalScaffold
import self.apk.sickhack.genz.ui.components.rememberCopier
import self.apk.sickhack.genz.ui.theme.TerminalCyan
import self.apk.sickhack.genz.ui.theme.TerminalGreen

@Composable
fun SideBySideScreen(onBack: () -> Unit) {
    val methods = listOf("GET", "POST", "PUT", "PATCH", "DELETE")
    var method by rememberSaveable { mutableStateOf(0) }
    var url by rememberSaveable { mutableStateOf("") }
    var headersRaw by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var requestText by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf("") }
    var history by remember { mutableStateOf<List<String>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val copy = rememberCopier()

    TerminalScaffold(title = "Side By Side", onBack = onBack) {
        SectionTitle("request")
        CategoryTabs(tabs = methods, selected = method, onSelect = { method = it })
        TInput(value = url, onValueChange = { url = it }, label = "url", placeholder = "https://target.com/api")
        TInput(value = headersRaw, onValueChange = { headersRaw = it }, label = "headers (Key: Value)", minLines = 3)
        TInput(value = body, onValueChange = { body = it }, label = "body", minLines = 2)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = if (busy) "SENDING..." else "SEND",
                color = TerminalGreen,
                enabled = url.isNotBlank() && !busy,
                onClick = {
                    busy = true
                    scope.launch {
                        try {
                            val headers = HttpClient.parseHeaders(headersRaw)
                            val m = methods[method]
                            val resp: HttpResponse = if (body.isBlank()) {
                                HttpClient.execute(m, url, headers)
                            } else {
                                HttpClient.execute(m, url, headers, body)
                            }
                            requestText = buildString {
                                append("$m ").append(url).append('\n')
                                headers.forEach { (k, v) -> append("$k: $v\n") }
                                if (body.isNotBlank()) append("\n$body\n")
                            }
                            responseText = buildString {
                                append(resp.summary()).append('\n')
                                resp.responseHeaders.forEach { (k, v) -> append("$k: $v\n") }
                                append('\n')
                                append(resp.body.take(2500))
                            }
                            history = listOf(resp.summary()) + history.take(14)
                        } catch (e: Exception) {
                            responseText = "// error: ${e.message}"
                        } finally {
                            busy = false
                        }
                    }
                }
            )
            TButton(text = "COPY REQ", color = TerminalCyan, enabled = requestText.isNotEmpty(), onClick = { copy(requestText) })
            TButton(text = "COPY RES", color = TerminalCyan, enabled = responseText.isNotEmpty(), onClick = { copy(responseText) })
        }

        SectionTitle(">>> request keluar")
        OutputBox(requestText, height = 160, emptyText = "// belum ada request")
        SectionTitle("<<< response masuk")
        OutputBox(responseText, height = 260, emptyText = "// belum ada response")

        SectionTitle("riwayat")
        if (history.isEmpty()) {
            OutputBox(text = "", emptyText = "// kosong", height = 60)
        } else {
            history.forEach { h ->
                OutputBox(text = h, height = 50)
            }
        }
    }
}

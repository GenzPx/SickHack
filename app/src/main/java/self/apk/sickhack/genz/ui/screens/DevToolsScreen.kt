package self.apk.sickhack.genz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import java.net.URI
import self.apk.sickhack.genz.core.codec.Codec
import self.apk.sickhack.genz.ui.components.OutputBox
import self.apk.sickhack.genz.ui.components.SectionTitle
import self.apk.sickhack.genz.ui.components.TButton
import self.apk.sickhack.genz.ui.components.TInput
import self.apk.sickhack.genz.ui.components.TerminalScaffold
import self.apk.sickhack.genz.ui.components.rememberCopier
import self.apk.sickhack.genz.ui.theme.TerminalCyan
import self.apk.sickhack.genz.ui.theme.TerminalGreen

@Composable
fun DevToolsScreen(onBack: () -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    var output by rememberSaveable { mutableStateOf("") }
    val copy = rememberCopier()

    fun run(f: (String) -> String) {
        output = try {
            f(input)
        } catch (e: Exception) {
            "// error: ${e.message}"
        }
    }

    TerminalScaffold(title = "Dev Tools", onBack = onBack) {
        SectionTitle("input")
        TInput(value = input, onValueChange = { input = it }, label = "input", minLines = 4)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "FORMAT JSON", color = TerminalGreen, onClick = { run { Codec.prettyJson(it) } })
            TButton(text = "JWT DECODE", color = TerminalCyan, onClick = { run { Codec.decodeJwt(it) } })
            TButton(text = "PARSE URL", color = TerminalGreen, onClick = { run { parseUrl(it) } })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "HTML ENC", onClick = { run { Codec.htmlEncode(it) } })
            TButton(text = "HTML DEC", color = TerminalCyan, onClick = { run { Codec.htmlDecode(it) } })
            TButton(text = "HASH ID", color = TerminalCyan, onClick = { run { Codec.identifyHash(it) } })
        }
        TButton(text = "COPY OUT", color = TerminalGreen, enabled = output.isNotEmpty(), onClick = { copy(output) })
        SectionTitle("output")
        OutputBox(output, height = 300)
    }
}

private fun parseUrl(s: String): String {
    return try {
        val u = URI(s)
        val sb = StringBuilder()
        sb.append("scheme:   ").append(u.scheme).append('\n')
        sb.append("host:     ").append(u.host).append('\n')
        sb.append("port:     ").append(if (u.port > 0) u.port.toString() else "(default)").append('\n')
        sb.append("path:     ").append(u.path).append('\n')
        sb.append("query:    ").append(u.rawQuery).append('\n')
        sb.append("fragment: ").append(u.fragment).append('\n')
        if (!u.rawQuery.isNullOrBlank()) {
            sb.append("params:\n")
            u.rawQuery.split("&").forEach { pair ->
                val idx = pair.indexOf('=')
                if (idx > 0) {
                    sb.append("  ").append(pair.substring(0, idx)).append(" = ").append(pair.substring(idx + 1)).append('\n')
                } else {
                    sb.append("  ").append(pair).append('\n')
                }
            }
        }
        sb.toString()
    } catch (e: Exception) {
        "INVALID URL: ${e.message}"
    }
}

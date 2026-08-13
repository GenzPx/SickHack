package self.apk.sickhack.genz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import self.apk.sickhack.genz.core.payloads.Payloads
import self.apk.sickhack.genz.ui.components.CopyRow
import self.apk.sickhack.genz.ui.components.OutputBox
import self.apk.sickhack.genz.ui.components.Pill
import self.apk.sickhack.genz.ui.components.SectionTitle
import self.apk.sickhack.genz.ui.components.TButton
import self.apk.sickhack.genz.ui.components.TInput
import self.apk.sickhack.genz.ui.components.TerminalScaffold
import self.apk.sickhack.genz.ui.components.rememberCopier
import self.apk.sickhack.genz.ui.theme.TerminalCyan
import self.apk.sickhack.genz.ui.theme.TerminalGreen
import self.apk.sickhack.genz.ui.theme.TerminalGreenDim

@Composable
fun GeneratorScreen(onBack: () -> Unit) {
    val copy = rememberCopier()

    // ---- password generator ----
    var len by rememberSaveable { mutableIntStateOf(16) }
    var useUpper by rememberSaveable { mutableStateOf(true) }
    var useLower by rememberSaveable { mutableStateOf(true) }
    var useDigits by rememberSaveable { mutableStateOf(true) }
    var useSym by rememberSaveable { mutableStateOf(true) }
    var password by remember { mutableStateOf("") }

    // ---- reverse shell ----
    val shellTypes = listOf("bash", "nc", "python", "php", "perl", "ruby", "powershell", "socat", "busybox")
    var shellType by rememberSaveable { mutableStateOf(0) }
    var ip by rememberSaveable { mutableStateOf("") }
    var port by rememberSaveable { mutableStateOf("4444") }
    var shellOut by remember { mutableStateOf("") }

    TerminalScaffold(title = "Generator", onBack = onBack) {
        SectionTitle("password generator")
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("len: $len", color = TerminalGreenDim, fontSize = 12.sp, modifier = androidx.compose.ui.Modifier.weight(1f))
        }
        Slider(
            value = len.toFloat(),
            onValueChange = { len = it.toInt() },
            valueRange = 4f..64f
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Checkbox(checked = useUpper, onCheckedChange = { useUpper = it })
            Text("A-Z", color = TerminalGreenDim, fontSize = 12.sp)
            Checkbox(checked = useLower, onCheckedChange = { useLower = it })
            Text("a-z", color = TerminalGreenDim, fontSize = 12.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Checkbox(checked = useDigits, onCheckedChange = { useDigits = it })
            Text("0-9", color = TerminalGreenDim, fontSize = 12.sp)
            Checkbox(checked = useSym, onCheckedChange = { useSym = it })
            Text("!@#", color = TerminalGreenDim, fontSize = 12.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = "GENERATE",
                color = TerminalGreen,
                enabled = useUpper || useLower || useDigits || useSym,
                onClick = {
                    val pool = buildString {
                        if (useUpper) append("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
                        if (useLower) append("abcdefghijklmnopqrstuvwxyz")
                        if (useDigits) append("0123456789")
                        if (useSym) append("!@#\$%^&*()-_=+[]{};:,.<>?/")
                    }
                    password = (1..len).map { pool[Random.nextInt(pool.length)] }.joinToString("")
                }
            )
            TButton(text = "COPY", color = TerminalCyan, enabled = password.isNotEmpty(), onClick = { copy(password) })
        }
        OutputBox(password, height = 60)

        SectionTitle("reverse shell generator")
        TInput(value = ip, onValueChange = { ip = it }, label = "IP / listener", placeholder = "10.0.0.1")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TInput(value = port, onValueChange = { port = it }, label = "port", singleLine = true)
        }
        Row(
            modifier = androidx.compose.ui.Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            shellTypes.forEachIndexed { i, s ->
                Pill(text = s, selected = i == shellType, onClick = { shellType = i })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = "GENERATE",
                color = TerminalGreen,
                enabled = ip.isNotBlank(),
                onClick = {
                    val templates = Payloads.reverseShells.filter {
                        when (shellTypes[shellType]) {
                            "bash" -> it.startsWith("bash") || it.startsWith("0<&196") || it.startsWith("sh -i")
                            "nc" -> it.startsWith("nc ") || it.startsWith("ncat") || it.startsWith("busybox nc")
                            "python" -> it.startsWith("python")
                            "php" -> it.startsWith("php -r")
                            "perl" -> it.startsWith("perl")
                            "ruby" -> it.startsWith("ruby")
                            "powershell" -> it.startsWith("powershell")
                            "socat" -> it.startsWith("socat")
                            "busybox" -> it.startsWith("busybox")
                            else -> true
                        }
                    }
                    shellOut = if (templates.isEmpty()) {
                        "// tidak ada template untuk tipe ini"
                    } else {
                        templates.joinToString("\n\n") { t ->
                            t.replace("{IP}", ip.trim()).replace("{PORT}", port.trim())
                        }
                    }
                }
            )
            TButton(text = "COPY", color = TerminalCyan, enabled = shellOut.isNotBlank(), onClick = { copy(shellOut) })
        }
        OutputBox(shellOut, height = 220)

        SectionTitle("wordlist password umum [${Payloads.commonPasswords.size}]")
        TButton(
            text = "COPY ALL",
            color = TerminalGreen,
            onClick = { copy(Payloads.commonPasswords.joinToString("\n")) }
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Payloads.commonPasswords.chunked(6).forEach { chunk ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chunk.forEach { w ->
                        Pill(text = w, onClick = { copy(w) })
                    }
                }
            }
        }
    }
}

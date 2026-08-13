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
import self.apk.sickhack.genz.core.codec.Codec
import self.apk.sickhack.genz.core.payloads.Payloads
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
fun HashCrackScreen(onBack: () -> Unit) {
    var hash by rememberSaveable { mutableStateOf("") }
    var customWords by rememberSaveable { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val copy = rememberCopier()

    TerminalScaffold(title = "Hash Crack", onBack = onBack) {
        SectionTitle("hash")
        TInput(value = hash, onValueChange = { hash = it }, label = "hash", minLines = 2)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = "IDENTIFY",
                color = TerminalCyan,
                enabled = hash.isNotBlank(),
                onClick = { output = "// identifikasi: ${Codec.identifyHash(hash)}" }
            )
            TButton(
                text = "CRACK (OFFLINE)",
                color = TerminalGreen,
                enabled = hash.isNotBlank() && !busy,
                onClick = {
                    busy = true
                    output = "// cracking..."
                    scope.launch {
                        output = withContext(Dispatchers.Default) {
                            val h = hash.trim().lowercase()
                            val words = if (customWords.isNotBlank()) {
                                Payloads.commonPasswords + customWords.lines().map { it.trim() }.filter { it.isNotBlank() }
                            } else {
                                Payloads.commonPasswords
                            }
                            val candidates = words + words.map { it.capitalize() } + words.map { it + "123" } + words.map { it + "!" }
                            val alg = when (h.length) {
                                32 -> "MD5"
                                40 -> "SHA-1"
                                64 -> "SHA-256"
                                56 -> "SHA-224"
                                96 -> "SHA-384"
                                128 -> "SHA-512"
                                else -> "?"
                            }
                            if (alg == "?") {
                                "// algoritma tidak dikenali dari panjang hash"
                            } else {
                                val hashFn: (String) -> String = when (alg) {
                                    "MD5" -> { Codec::md5 }
                                    "SHA-1" -> { Codec::sha1 }
                                    "SHA-224" -> { Codec::sha224 }
                                    "SHA-256" -> { Codec::sha256 }
                                    "SHA-384" -> { Codec::sha384 }
                                    else -> { Codec::sha512 }
                                }
                                var found: String? = null
                                for ((i, w) in candidates.withIndex()) {
                                    if (hashFn(w) == h) {
                                        found = w
                                        break
                                    }
                                    if (i % 200 == 0 && i > 0) {
                                        // keep UI breathing
                                        kotlinx.coroutines.yield()
                                    }
                                }
                                if (found != null) {
                                    "// PASSWORD DITEMUKAN: $found\n// algoritma: $alg"
                                } else {
                                    "// tidak ditemukan di wordlist (${candidates.size} kata)\n// algoritma: $alg"
                                }
                            }
                        }
                        busy = false
                    }
                }
            )
            TButton(text = "COPY", color = TerminalGreen, enabled = output.isNotEmpty(), onClick = { copy(output) })
        }

        SectionTitle("wordlist custom (opsional, satu per baris)")
        TInput(value = customWords, onValueChange = { customWords = it }, label = "wordlist", minLines = 3)

        Text(
            "// wordlist bawaan: ${Payloads.commonPasswords.size} kata + varian (capitalize, +123, +!)",
            color = TerminalYellow,
            fontSize = 10.sp
        )
        OutputBox(output, height = 200)
    }
}

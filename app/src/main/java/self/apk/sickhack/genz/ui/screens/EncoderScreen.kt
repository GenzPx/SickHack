package self.apk.sickhack.genz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
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
fun EncoderScreen(onBack: () -> Unit) {
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

    TerminalScaffold(title = "Encoder", onBack = onBack) {
        SectionTitle("input")
        TInput(value = input, onValueChange = { input = it }, label = "text", minLines = 3)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "CLEAR", onClick = { input = ""; output = "" })
            TButton(text = "COPY OUT", color = TerminalCyan, enabled = output.isNotEmpty(), onClick = { copy(output) })
        }

        SectionTitle("encoding")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "B64 ENC", onClick = { run { Codec.b64encode(it) } })
            TButton(text = "B64 DEC", color = TerminalCyan, onClick = { run { Codec.b64decode(it) } })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "B32 ENC", onClick = { run { Codec.b32encode(it) } })
            TButton(text = "B32 DEC", color = TerminalCyan, onClick = { run { Codec.b32decode(it) } })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "URL ENC", onClick = { run { Codec.urlEncode(it) } })
            TButton(text = "URL DEC", color = TerminalCyan, onClick = { run { Codec.urlDecode(it) } })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "HEX ENC", onClick = { run { Codec.hexEncode(it) } })
            TButton(text = "HEX DEC", color = TerminalCyan, onClick = { run { Codec.hexDecode(it) } })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "BINARY", onClick = { run { Codec.toBinary(it) } })
            TButton(text = "FROM BIN", color = TerminalCyan, onClick = { run { Codec.fromBinary(it) } })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "ASCII", onClick = { run { Codec.toAsciiCodes(it) } })
            TButton(text = "FROM ASCII", color = TerminalCyan, onClick = { run { Codec.fromAsciiCodes(it) } })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "HTML ENC", onClick = { run { Codec.htmlEncode(it) } })
            TButton(text = "HTML DEC", color = TerminalCyan, onClick = { run { Codec.htmlDecode(it) } })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "JS CHARCODE", onClick = { run { Codec.toJsCharcode(it) } })
            TButton(text = "JS \\x HEX", color = TerminalCyan, onClick = { run { Codec.toJsHex(it) } })
        }

        SectionTitle("transform")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "ROT13", onClick = { run { Codec.rot13(it) } })
            TButton(text = "REVERSE", color = TerminalCyan, onClick = { run { Codec.reverse(it) } })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "UPPER", onClick = { run { Codec.upper(it) } })
            TButton(text = "LOWER", color = TerminalCyan, onClick = { run { Codec.lower(it) } })
            TButton(text = "TOGGLE", onClick = { run { Codec.toggleCase(it) } })
        }

        SectionTitle("hash")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "MD5", onClick = { run { Codec.md5(it) } })
            TButton(text = "SHA1", onClick = { run { Codec.sha1(it) } })
            TButton(text = "SHA224", onClick = { run { Codec.sha224(it) } })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(text = "SHA256", onClick = { run { Codec.sha256(it) } })
            TButton(text = "SHA384", onClick = { run { Codec.sha384(it) } })
            TButton(text = "SHA512", onClick = { run { Codec.sha512(it) } })
        }

        SectionTitle("output")
        OutputBox(output, height = 240)
        Column {}
    }
}

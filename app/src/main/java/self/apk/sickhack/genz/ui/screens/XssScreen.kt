package self.apk.sickhack.genz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import self.apk.sickhack.genz.core.codec.Codec
import self.apk.sickhack.genz.core.payloads.Payloads
import self.apk.sickhack.genz.ui.components.PayloadLab
import self.apk.sickhack.genz.ui.components.TButton
import self.apk.sickhack.genz.ui.theme.TerminalCyan

@Composable
fun XssScreen(onBack: () -> Unit) {
    val tabs = listOf("Raw", "Encoded", "WAF Bypass")
    PayloadLab(
        title = "XSS",
        tabs = tabs,
        payloads = { i ->
            when (i) {
                0 -> Payloads.xssRaw
                1 -> Payloads.xssEncoded
                2 -> Payloads.xssWafBypass
                else -> emptyList()
            }
        },
        onBack = onBack,
        extraActions = { current, setCurrent ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TButton(
                    text = "JS CHARCODE",
                    color = TerminalCyan,
                    enabled = current.isNotEmpty(),
                    onClick = { setCurrent(Codec.toJsCharcode(current)) }
                )
                TButton(
                    text = "\\x HEX",
                    color = TerminalCyan,
                    enabled = current.isNotEmpty(),
                    onClick = { setCurrent(Codec.toJsHex(current)) }
                )
                TButton(
                    text = "HTML ENT",
                    color = TerminalCyan,
                    enabled = current.isNotEmpty(),
                    onClick = { setCurrent(Codec.toHtmlDecimal(current)) }
                )
            }
        }
    )
}

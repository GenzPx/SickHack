package self.apk.sickhack.genz.ui.screens

import androidx.compose.runtime.Composable
import self.apk.sickhack.genz.core.payloads.Payloads
import self.apk.sickhack.genz.ui.components.PayloadLab

@Composable
fun LfiScreen(onBack: () -> Unit) {
    val tabs = listOf("LFI", "RFI", "RCE")
    PayloadLab(
        title = "LFI / RFI / RCE",
        tabs = tabs,
        payloads = { i ->
            when (i) {
                0 -> Payloads.lfi
                1 -> Payloads.rfi
                2 -> Payloads.rce
                else -> emptyList()
            }
        },
        onBack = onBack
    )
}

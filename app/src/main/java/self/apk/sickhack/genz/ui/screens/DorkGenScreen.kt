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
import self.apk.sickhack.genz.core.payloads.Payloads
import self.apk.sickhack.genz.ui.components.CopyRow
import self.apk.sickhack.genz.ui.components.SectionTitle
import self.apk.sickhack.genz.ui.components.TButton
import self.apk.sickhack.genz.ui.components.TInput
import self.apk.sickhack.genz.ui.components.TerminalScaffold
import self.apk.sickhack.genz.ui.components.rememberCopier
import self.apk.sickhack.genz.ui.theme.TerminalCyan
import self.apk.sickhack.genz.ui.theme.TerminalGreen

@Composable
fun DorkGenScreen(onBack: () -> Unit) {
    var domain by rememberSaveable { mutableStateOf("") }
    val copy = rememberCopier()

    val dorks = remember(domain) {
        if (domain.isBlank()) Payloads.dorks
        else Payloads.dorks.map { it.replace("{DOMAIN}", domain.trim()) }
    }

    TerminalScaffold(title = "Dork Gen", onBack = onBack) {
        SectionTitle("domain")
        TInput(value = domain, onValueChange = { domain = it }, label = "domain", placeholder = "example.com")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = "COPY ALL [${dorks.size}]",
                color = TerminalGreen,
                enabled = dorks.isNotEmpty(),
                onClick = { copy(dorks.joinToString("\n")) }
            )
            TButton(
                text = "COPY SEPARATOR",
                color = TerminalCyan,
                enabled = dorks.isNotEmpty(),
                onClick = { copy(dorks.joinToString("\n" + "-".repeat(40) + "\n")) }
            )
        }
        SectionTitle("dorks")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            dorks.forEach { d ->
                CopyRow(text = d, copy = copy)
            }
        }
    }
}

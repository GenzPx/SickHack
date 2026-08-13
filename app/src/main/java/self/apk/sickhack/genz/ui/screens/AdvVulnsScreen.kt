package self.apk.sickhack.genz.ui.screens

import androidx.compose.runtime.Composable
import self.apk.sickhack.genz.core.payloads.Payloads
import self.apk.sickhack.genz.ui.components.PayloadLab

@Composable
fun AdvVulnsScreen(onBack: () -> Unit) {
    val tabs = listOf("SSTI", "SSRF", "XXE", "CRLF", "OpenRedirect", "LDAP", "XPath", "NoSQL", "Header/Host")
    PayloadLab(
        title = "Adv. Vulns",
        tabs = tabs,
        payloads = { i ->
            when (i) {
                0 -> Payloads.ssti
                1 -> Payloads.ssrf
                2 -> Payloads.xxe
                3 -> Payloads.crlf
                4 -> Payloads.openRedirect
                5 -> Payloads.ldap
                6 -> Payloads.xpath
                7 -> Payloads.nosql
                8 -> Payloads.headerInjection
                else -> emptyList()
            }
        },
        onBack = onBack
    )
}

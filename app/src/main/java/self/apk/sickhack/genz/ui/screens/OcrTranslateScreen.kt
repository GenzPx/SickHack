package self.apk.sickhack.genz.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import self.apk.sickhack.genz.ui.components.CategoryTabs
import self.apk.sickhack.genz.ui.components.OutputBox
import self.apk.sickhack.genz.ui.components.SectionTitle
import self.apk.sickhack.genz.ui.components.TButton
import self.apk.sickhack.genz.ui.components.TerminalScaffold
import self.apk.sickhack.genz.ui.components.rememberCopier
import self.apk.sickhack.genz.ui.theme.TerminalCyan
import self.apk.sickhack.genz.ui.theme.TerminalGreen
import self.apk.sickhack.genz.ui.theme.TerminalYellow

@Composable
fun OcrTranslateScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val copy = rememberCopier()
    var recognized by remember { mutableStateOf("") }
    var translated by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("// pilih gambar untuk OCR") }
    var busy by remember { mutableStateOf(false) }

    val sourceLangs = listOf("EN", "ID", "AR", "ES", "FR", "DE", "JA", "ZH", "RU", "KO")
    val targetLangs = listOf("ID", "EN", "AR", "ES", "FR", "DE", "JA", "ZH", "RU", "KO")
    var source by remember { mutableStateOf(0) }
    var target by remember { mutableStateOf(0) }

    fun lang(tag: String) = when (tag) {
        "ID" -> TranslateLanguage.INDONESIAN
        "AR" -> TranslateLanguage.ARABIC
        "ES" -> TranslateLanguage.SPANISH
        "FR" -> TranslateLanguage.FRENCH
        "DE" -> TranslateLanguage.GERMAN
        "JA" -> TranslateLanguage.JAPANESE
        "ZH" -> TranslateLanguage.CHINESE
        "RU" -> TranslateLanguage.RUSSIAN
        "KO" -> TranslateLanguage.KOREAN
        else -> TranslateLanguage.ENGLISH
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            status = "// membaca teks dengan ML Kit..."
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = try {
                InputImage.fromFilePath(context, uri)
            } catch (e: Exception) {
                status = "// error membaca gambar: ${e.message}"
                null
            }
            if (image != null) {
                recognizer.process(image)
                    .addOnSuccessListener { result ->
                        recognized = result.text
                        status = "// OCR selesai — ${result.text.length} karakter"
                    }
                    .addOnFailureListener { e ->
                        status = "// OCR gagal (butuh Google Play Services?): ${e.message}"
                    }
            }
        }
    }

    val scope = rememberCoroutineScope()

    TerminalScaffold(title = "OCR Translate", onBack = onBack) {
        SectionTitle("gambar")
        TButton(
            text = "PILIH GAMBAR",
            color = TerminalGreen,
            enabled = !busy,
            onClick = { picker.launch("image/*") }
        )
        Text(status, color = TerminalYellow, fontSize = 11.sp)

        SectionTitle("hasil OCR")
        OutputBox(recognized, height = 150)

        SectionTitle("terjemahan (ML Kit translate)")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TButton(
                text = "TRANSLATE",
                color = TerminalCyan,
                enabled = recognized.isNotBlank() && !busy,
                onClick = {
                    busy = true
                    translated = "// mendownload model & menerjemahkan..."
                    scope.launch {
                        val res = withContext(Dispatchers.IO) {
                            try {
                                val translator = Translation.getClient(
                                    TranslatorOptions.Builder()
                                        .setSourceLanguage(lang(sourceLangs[source]))
                                        .setTargetLanguage(lang(targetLangs[target]))
                                        .build()
                                )
                                val download = kotlinx.coroutines.CompletableDeferred<Boolean>()
                                translator.downloadModelIfNeeded()
                                    .addOnSuccessListener { download.complete(true) }
                                    .addOnFailureListener { e -> download.complete(false) }
                                if (download.await()) {
                                    val t = kotlinx.coroutines.CompletableDeferred<String>()
                                    translator.translate(recognized)
                                        .addOnSuccessListener { t.complete(it) }
                                        .addOnFailureListener { e -> t.complete("// gagal translate: ${e.message}") }
                                    t.await()
                                } else {
                                    "// gagal download model (butuh Google Play Services)"
                                }
                            } catch (e: Exception) {
                                "// error: ${e.message}"
                            }
                        }
                        translated = res
                        busy = false
                    }
                }
            )
            TButton(text = "COPY OCR", enabled = recognized.isNotBlank(), onClick = { copy(recognized) })
            TButton(text = "COPY TR", color = TerminalCyan, enabled = translated.isNotBlank() && !translated.startsWith("//"), onClick = { copy(translated) })
        }
        Text("sumber:", color = self.apk.sickhack.genz.ui.theme.TerminalGreenDim, fontSize = 11.sp)
        CategoryTabs(tabs = sourceLangs, selected = source, onSelect = { source = it })
        Text("tujuan:", color = self.apk.sickhack.genz.ui.theme.TerminalGreenDim, fontSize = 11.sp)
        CategoryTabs(tabs = targetLangs, selected = target, onSelect = { target = it })
        OutputBox(translated, height = 160)
    }
}

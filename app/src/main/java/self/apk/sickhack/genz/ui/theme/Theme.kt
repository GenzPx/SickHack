package self.apk.sickhack.genz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SickColorScheme = darkColorScheme(
    primary = TerminalGreen,
    onPrimary = Color(0xFF00340C),
    primaryContainer = Color(0xFF0A2E14),
    onPrimaryContainer = TerminalGreen,
    secondary = TerminalCyan,
    onSecondary = Color(0xFF00252B),
    background = BlackBg,
    onBackground = OnBg,
    surface = SurfaceBg,
    onSurface = OnBg,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = TerminalGreenDim,
    error = TerminalRed,
    onError = Color(0xFF2A0000),
    outline = TerminalGreenDim
)

@Composable
fun SickHackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SickColorScheme,
        typography = SickTypography,
        content = content
    )
}

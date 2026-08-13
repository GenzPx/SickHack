package self.apk.sickhack.genz.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

// Semua teks monospace — tema terminal hacker
val Mono = FontFamily.Monospace

val SickTypography = Typography(
    displayLarge = TextStyle(fontFamily = Mono, fontSize = 44.sp, letterSpacing = 2.sp),
    headlineLarge = TextStyle(fontFamily = Mono, fontSize = 28.sp),
    headlineMedium = TextStyle(fontFamily = Mono, fontSize = 22.sp),
    titleLarge = TextStyle(fontFamily = Mono, fontSize = 19.sp),
    titleMedium = TextStyle(fontFamily = Mono, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = Mono, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = Mono, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = Mono, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = Mono, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = Mono, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = Mono, fontSize = 10.sp)
)

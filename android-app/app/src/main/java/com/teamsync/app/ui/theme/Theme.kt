package com.teamsync.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

val Lime         = Color(0xFF39FF14)
val LimeDim      = Color(0xFF2BD30E)
val LimeGlow     = Color(0x3339FF14)
val Bg950        = Color(0xFF0A0A0A)
val Bg900        = Color(0xFF111111)
val Bg800        = Color(0xFF1A1A1A)
val Border       = Color(0xFF262626)
val TextHi       = Color(0xFFFAFAFA)
val TextMid      = Color(0xFFA3A3A3)
val TextLo       = Color(0xFF525252)
val DangerRed    = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF22C55E)
val WarnAmber    = Color(0xFFF59E0B)
val InfoBlue     = Color(0xFF3B82F6)

private val TeamSyncColors = darkColorScheme(
    primary = Lime,
    onPrimary = Bg950,
    primaryContainer = LimeDim,
    onPrimaryContainer = Bg950,
    secondary = Lime,
    onSecondary = Bg950,
    background = Bg950,
    onBackground = TextHi,
    surface = Bg900,
    onSurface = TextHi,
    surfaceVariant = Bg800,
    onSurfaceVariant = TextMid,
    outline = Border,
    error = DangerRed,
)

private val AppTypography = Typography(
    displayLarge   = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 40.sp),
    headlineLarge  = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 28.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 22.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 18.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 15.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 15.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 13.sp),
    labelMedium    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 11.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 10.sp),
)

@Composable
fun TeamSyncTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Bg950.value.toInt()
            window.navigationBarColor = Bg950.value.toInt()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = TeamSyncColors,
        typography = AppTypography,
        content = content,
    )
}

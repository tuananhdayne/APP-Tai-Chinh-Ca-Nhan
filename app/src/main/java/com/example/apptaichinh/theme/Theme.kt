package com.example.apptaichinh.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Bảng màu chuẩn Radiant Modern Light Theme
private val NeumorphicLightColorScheme = lightColorScheme(
    primary = NeonAzure,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = NeonIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEF2FF),
    onSecondaryContainer = Color(0xFF3730A3),
    tertiary = NeonAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF92400E),
    background = CarbonBackground,
    onBackground = TextHighContrast,
    surface = CarbonSurface,
    onSurface = TextHighContrast,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextMediumContrast,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = NeonCoral,
    onError = Color.White,
    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = Color(0xFF9F1239)
)

private val NeumorphicCarbonDarkColorScheme = darkColorScheme(
    primary = NeonAzure,
    onPrimary = Color(0xFF0A0D14),
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFFE2E8F0),
    secondary = NeonIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF26334D),
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = NeonAmber,
    onTertiary = Color(0xFF0F172A),
    tertiaryContainer = Color(0xFF161E2E),
    onTertiaryContainer = Color(0xFFFDE68A),
    background = Color(0xFF0A0D14),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF131926),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF161E2E),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF26334D),
    outlineVariant = Color(0xFF1E2638),
    error = NeonCoral,
    onError = Color.White,
    errorContainer = Color(0xFF4C0519),
    onErrorContainer = Color(0xFFFECDD3)
)

@Composable
fun AppTaiChinhTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) NeumorphicCarbonDarkColorScheme else NeumorphicLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

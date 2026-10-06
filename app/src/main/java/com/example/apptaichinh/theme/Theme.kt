package com.example.apptaichinh.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Bảng màu chuẩn Skeuomorphic-Neumorphic Dark Mode
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
    background = CarbonBackground,
    onBackground = TextHighContrast,
    surface = CarbonSurface,
    onSurface = TextHighContrast,
    surfaceVariant = CarbonSurfaceGlass,
    onSurfaceVariant = TextMediumContrast,
    outline = Color(0xFF26334D),
    outlineVariant = Color(0xFF1E2638),
    error = NeonCoral,
    onError = Color.White,
    errorContainer = Color(0xFF4C0519),
    onErrorContainer = Color(0xFFFECDD3)
)

@Composable
fun AppTaiChinhTheme(
    // Mặc định luôn chạy Dark Mode Carbon theo yêu cầu thiết kế Neumorphic-Glassmorphism
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = NeumorphicCarbonDarkColorScheme,
        typography = Typography,
        content = content
    )
}

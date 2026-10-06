package com.example.apptaichinh.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =======================================================================
// DEEP CARBON SKEUOMORPHIC & GLASSMORPHIC DESIGN SYSTEM PALETTE
// =======================================================================

// Nền Carbon Tối Sâu (Deep Carbon Dark Mode - OLED Calibrated)
val CarbonBackground = Color(0xFF0A0D14)       // Nền chính tối sâu siêu sang
val CarbonSurface = Color(0xFF131926)          // Bề mặt Card/Panel cơ bản
val CarbonSurfaceGlass = Color(0xFF161E2E)     // Thẻ kính mờ pha lam than
val CarbonSurfaceLight = Color(0xFF1C2538)     // Lớp nâng nổi bậc 2
val CarbonSurfaceRecessed = Color(0xFF07090E)  // Bề mặt lõm xuống (numpad/input)

// Bảng màu Neon rực rỡ bão hòa cao (High-Vibrancy Neon Accents)
val NeonEmerald = Color(0xFF10B981)            // Thu nhập (+), An toàn, Tiết kiệm
val NeonEmeraldGlow = Color(0xFF34D399)        // Vòng hào quang xanh ngọc
val NeonCoral = Color(0xFFFF4D6D)              // Chi tiêu (-), Cảnh báo đỏ
val NeonCoralGlow = Color(0xFFFB7185)          // Vòng hào quang đỏ san hô
val NeonAmber = Color(0xFFF59E0B)              // Ăn uống, Hạn mức gần đạt
val NeonAmberGlow = Color(0xFFFBBF24)          // Hào quang vàng hổ phách
val NeonAzure = Color(0xFF38BDF8)              // Đi lại, Xanh điện tử
val NeonViolet = Color(0xFF8B5CF6)             // Mua sắm, Đa phương tiện
val NeonPink = Color(0xFFEC4899)               // Giải trí, Quà tặng
val NeonIndigo = Color(0xFF6366F1)             // Công nghệ, Trợ lý AI

// Đường viền tóc phản quang (Specular Hairline Glass Borders)
val GlassHairlineTopLeft = Color.White.copy(alpha = 0.18f)
val GlassHairlineBottomRight = Color.White.copy(alpha = 0.04f)

val SpecularBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.22f),
        Color.White.copy(alpha = 0.06f),
        Color(0xFF38BDF8).copy(alpha = 0.12f)
    )
)

val ActiveNeonBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF38BDF8),
        Color(0xFF818CF8),
        Color(0xFFEC4899)
    )
)

// Chữ & Biểu tượng (Typography Contrast)
val TextHighContrast = Color(0xFFF8FAFC)        // Trắng bạc rực rỡ
val TextMediumContrast = Color(0xFF94A3B8)      // Xám bạc trung tính
val TextMuted = Color(0xFF64748B)               // Xám mờ phụ trợ

// Tương thích ngược với hệ thống theme hiện tại
val FinanceBluePrimary = Color(0xFF3B82F6)
val FinanceBlueDark = Color(0xFF1D4ED8)
val FinanceBlueLight = Color(0xFF60A5FA)
val FinanceIndigo = NeonIndigo
val FinanceViolet = NeonViolet
val AiSparkCyan = NeonAzure
val AiSparkAmber = NeonAmber
val AiSparkPink = NeonPink
val FinanceIncomeGreen = NeonEmerald
val FinanceIncomeGreenLight = Color(0xFF064E3B)
val FinanceExpenseRed = NeonCoral
val FinanceExpenseRedLight = Color(0xFF4C0519)

val SlateLightBackground = Color(0xFF0A0D14)
val SlateLightSurface = Color(0xFF131926)
val SlateLightSurfaceVariant = Color(0xFF161E2E)
val SlateLightTextPrimary = TextHighContrast
val SlateLightTextSecondary = TextMediumContrast
val SlateLightOutline = Color(0xFF334155)

val SlateDarkBackground = CarbonBackground
val SlateDarkSurface = CarbonSurface
val SlateDarkSurfaceVariant = CarbonSurfaceGlass
val SlateDarkTextPrimary = TextHighContrast
val SlateDarkTextSecondary = TextMediumContrast
val SlateDarkOutline = Color(0xFF26334D)

val Purple80 = FinanceBlueLight
val PurpleGrey80 = SlateDarkTextSecondary
val Pink80 = AiSparkPink
val Purple40 = FinanceBluePrimary
val PurpleGrey40 = SlateLightTextSecondary
val Pink40 = FinanceExpenseRed

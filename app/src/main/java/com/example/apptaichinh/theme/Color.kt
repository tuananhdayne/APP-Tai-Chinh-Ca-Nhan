package com.example.apptaichinh.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =======================================================================
// RADIANT MODERN LIGHT THEME & GLASSMORPHIC DESIGN SYSTEM PALETTE
// =======================================================================

// Nền Sáng Tinh Tế (Clean Light Mode - High Contrast & Soft Surface)
val CarbonBackground = Color(0xFFF8FAFC)       // Nền chính Slate 50 tinh tế, sáng sủa
val CarbonSurface = Color(0xFFFFFFFF)          // Bề mặt Card/Panel trắng sáng tinh khôi
val CarbonSurfaceGlass = Color(0xFFFFFFFF)     // Thẻ kính mờ trắng sáng
val CarbonSurfaceLight = Color(0xFFF1F5F9)     // Lớp nâng nổi bậc 2 (Slate 100)
val CarbonSurfaceRecessed = Color(0xFFF1F5F9)  // Bề mặt lõm xuống (numpad/input, Slate 100)

// Bảng màu rực rỡ bão hòa chuẩn mực (High-Vibrancy Accents)
val NeonEmerald = Color(0xFF059669)            // Thu nhập (+), An toàn, Tiết kiệm (Emerald 600)
val NeonEmeraldGlow = Color(0xFF10B981)        // Vòng hào quang xanh ngọc (Emerald 500)
val NeonCoral = Color(0xFFE11D48)              // Chi tiêu (-), Cảnh báo đỏ (Rose 600)
val NeonCoralGlow = Color(0xFFF43F5E)          // Vòng hào quang đỏ san hô (Rose 500)
val NeonAmber = Color(0xFFD97706)              // Ăn uống, Hạn mức gần đạt (Amber 600)
val NeonAmberGlow = Color(0xFFF59E0B)          // Hào quang vàng hổ phách (Amber 500)
val NeonAzure = Color(0xFF2563EB)              // Đi lại, Xanh điện tử (Blue 600)
val NeonViolet = Color(0xFF7C3AED)             // Mua sắm, Đa phương tiện (Violet 600)
val NeonPink = Color(0xFFDB2777)               // Giải trí, Quà tặng (Pink 600)
val NeonIndigo = Color(0xFF4F46E5)             // Công nghệ, Trợ lý AI (Indigo 600)

// Đường viền tóc phản quang (Specular Hairline Glass Borders)
val GlassHairlineTopLeft = Color(0xFFE2E8F0)
val GlassHairlineBottomRight = Color(0xFFF1F5F9)

val SpecularBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFE2E8F0),
        Color(0xFFCBD5E1),
        Color(0xFFE2E8F0)
    )
)

val ActiveNeonBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF2563EB),
        Color(0xFF4F46E5),
        Color(0xFFEC4899)
    )
)

// Chữ & Biểu tượng (Typography Contrast - Đậm, sắc nét trên nền sáng)
val TextHighContrast = Color(0xFF0F172A)        // Slate 900 - Đen than sắc nét
val TextMediumContrast = Color(0xFF475569)      // Slate 600 - Xám ghi trung tính
val TextMuted = Color(0xFF64748B)               // Slate 500 - Xám mờ phụ trợ

// Tương thích ngược với hệ thống theme hiện tại
val FinanceBluePrimary = Color(0xFF2563EB)
val FinanceBlueDark = Color(0xFF1D4ED8)
val FinanceBlueLight = Color(0xFF3B82F6)
val FinanceIndigo = NeonIndigo
val FinanceViolet = NeonViolet
val AiSparkCyan = NeonAzure
val AiSparkAmber = NeonAmber
val AiSparkPink = NeonPink
val FinanceIncomeGreen = NeonEmerald
val FinanceIncomeGreenLight = Color(0xFFD1FAE5)
val FinanceExpenseRed = NeonCoral
val FinanceExpenseRedLight = Color(0xFFFFE4E6)

val SlateLightBackground = Color(0xFFF8FAFC)
val SlateLightSurface = Color(0xFFFFFFFF)
val SlateLightSurfaceVariant = Color(0xFFF1F5F9)
val SlateLightTextPrimary = TextHighContrast
val SlateLightTextSecondary = TextMediumContrast
val SlateLightOutline = Color(0xFFE2E8F0)

val SlateDarkBackground = CarbonBackground
val SlateDarkSurface = CarbonSurface
val SlateDarkSurfaceVariant = CarbonSurfaceGlass
val SlateDarkTextPrimary = TextHighContrast
val SlateDarkTextSecondary = TextMediumContrast
val SlateDarkOutline = Color(0xFFCBD5E1)

val Purple80 = FinanceBlueLight
val PurpleGrey80 = SlateLightTextSecondary
val Pink80 = AiSparkPink
val Purple40 = FinanceBluePrimary
val PurpleGrey40 = SlateLightTextSecondary
val Pink40 = FinanceExpenseRed

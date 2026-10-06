package com.example.apptaichinh.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptaichinh.theme.CarbonSurface
import com.example.apptaichinh.theme.CarbonSurfaceGlass
import com.example.apptaichinh.theme.NeonEmerald
import com.example.apptaichinh.theme.SpecularBorderBrush
import com.example.apptaichinh.theme.TextHighContrast

/**
 * Modifier tạo hiệu ứng co nén xúc giác (Tactile Micro-interaction)
 * Khi chạm vào, phần tử co nhẹ (scale down ~0.95) và nảy lại mượt mà khi nhấc tay.
 */
fun Modifier.tactileClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.95f,
    onClick: () -> Unit
): Modifier = this.then(
    Modifier.pointerInput(enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            val upOrCancel = waitForUpOrCancellation()
            if (upOrCancel != null) {
                onClick()
            }
        }
    }
)

/**
 * Bảng Panel / Card theo phong cách Skeuomorphic-Neumorphic kết hợp Subtle Glassmorphism
 * - Nền lam than mờ đục cao cấp (Deep Blue-Gray Glass).
 * - Viền tóc phản quang chéo (Specular Hairline Gradient).
 * - Bóng kép mềm mại (Dual-layer soft drop shadow) tạo chiều sâu nổi lơ lửng.
 */
@Composable
fun NeumorphicGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 6.dp,
    containerColor: Color = CarbonSurfaceGlass.copy(alpha = 0.90f),
    borderColor: Brush = SpecularBorderBrush,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.7f),
                spotColor = Color(0xFF1E293B)
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        containerColor,
                        CarbonSurface.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = borderColor,
                shape = shape
            )
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * Biểu tượng 3D Glyph Tinh Xảo (3D Jewel Glyph Icon)
 * - Vòng hào quang đa sắc phía ngoài tỏa sáng.
 * - Khối nút nổi có độ sâu quang học bevel viền bóng bẩy.
 * - Hiển thị biểu tượng/emoji sắc nét, rực rỡ bão hòa cao.
 */
@Composable
fun Glyph3DIcon(
    modifier: Modifier = Modifier,
    icon: String = "💰",
    accentColor: Color = NeonEmerald,
    size: Dp = 44.dp,
    fontSize: Float = 20f
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = accentColor.copy(alpha = 0.4f),
                spotColor = accentColor.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.28f),
                        Color(0xFF131926).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        accentColor.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = fontSize.sp
        )
    }
}

/**
 * Biểu tượng 3D Glyph bằng Vector Image (dành cho các icon hệ thống)
 */
@Composable
fun Glyph3DVectorIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    accentColor: Color = NeonEmerald,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = accentColor.copy(alpha = 0.4f),
                spotColor = accentColor.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.25f),
                        Color(0xFF131926).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        accentColor.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Thẻ Pill dạng kính nổi hiển thị trạng thái hoặc số dư
 */
@Composable
fun TactilePillChip(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    leadingDot: Boolean = true
) {
    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.18f),
                        Color(0xFF161E2E).copy(alpha = 0.85f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = TextHighContrast,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

package com.example.apptaichinh.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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
import com.example.apptaichinh.theme.NeonAmber
import com.example.apptaichinh.theme.NeonAzure
import com.example.apptaichinh.theme.NeonCoral
import com.example.apptaichinh.theme.NeonEmerald
import com.example.apptaichinh.theme.SpecularBorderBrush
import com.example.apptaichinh.theme.TextHighContrast
import com.example.apptaichinh.theme.TextMediumContrast
import com.example.apptaichinh.theme.TextMuted

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
 * Modifier nén đàn hồi xúc giác phản ứng ngay lập tức với cảm ứng ngón tay (Press State)
 */
fun Modifier.tactileBounceClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.93f,
    onClick: () -> Unit
): Modifier = this.composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tactileBounce"
    )
    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Modifier gắn hiệu ứng nhún ngón tay vào một InteractionSource đã có
 */
fun Modifier.tactilePress(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.93f
): Modifier = this.composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tactileScale"
    )
    this.scale(scale)
}

/**
 * Bảng Panel / Card theo phong cách Clean Light Glassmorphism Hiện Đại
 * - Nền trắng sáng tinh tế với độ phản quang nhẹ.
 * - Viền tóc phản quang chéo (Specular Hairline Gradient).
 * - Bóng kép mềm mại (Dual-layer soft drop shadow) tạo chiều sâu nổi lơ lửng.
 */
@Composable
fun NeumorphicGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 3.dp,
    containerColor: Color = Color.White,
    borderColor: Brush = SpecularBorderBrush,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color(0xFF64748B).copy(alpha = 0.10f),
                spotColor = Color(0xFF0F172A).copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        containerColor,
                        CarbonSurface
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
 * - Khối nút nổi có độ sâu quang học bevel viền bóng bẩy trên nền sáng.
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
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = accentColor.copy(alpha = 0.20f),
                spotColor = accentColor.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.18f),
                        Color(0xFFF8FAFC)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.40f),
                        accentColor.copy(alpha = 0.15f),
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
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = accentColor.copy(alpha = 0.20f),
                spotColor = accentColor.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.16f),
                        Color(0xFFF8FAFC)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.35f),
                        accentColor.copy(alpha = 0.15f),
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
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp), ambientColor = Color(0xFF64748B).copy(alpha = 0.1f))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.12f),
                        Color(0xFFF1F5F9)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.35f),
                        Color(0xFFCBD5E1)
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

/**
 * Nút CTA chính với dải gradient rực rỡ, viền phản quang specular highlight và bóng hào quang màu phát sáng
 * Tích hợp animation co nén xúc giác (Tactile Spring Compression) nảy cơ học khi chạm ngón tay.
 */
@Composable
fun TactileGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradientColors: List<Color> = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)),
    glowColor: Color = Color(0xFF2563EB),
    textColor: Color = Color.White,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    shape: Shape = RoundedCornerShape(18.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "btnScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (!enabled) 1.dp else if (isPressed) 2.dp else 6.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnElev"
    )

    val backgroundBrush = if (!enabled) {
        Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
    } else if (isPressed) {
        Brush.verticalGradient(gradientColors.map { it.copy(alpha = 0.90f) })
    } else {
        Brush.verticalGradient(gradientColors)
    }

    val specularBorder = if (enabled) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.65f),
                Color.White.copy(alpha = 0.15f),
                Color.Transparent
            )
        )
    } else {
        Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
    }

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (enabled) glowColor.copy(alpha = 0.40f) else Color(0xFF64748B).copy(alpha = 0.10f),
                spotColor = if (enabled) glowColor.copy(alpha = 0.35f) else Color(0xFF0F172A).copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(backgroundBrush)
            .border(width = 1.2.dp, brush = specularBorder, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) textColor else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) textColor else Color(0xFF94A3B8),
                letterSpacing = 0.2.sp
            )
        }
    }
}

/**
 * Phím Numpad xúc giác cao cấp kiểu Keycap cơ học
 * - Có bevel viền bóng bẩy trên nền sứ trắng ngọc trai hoặc tinted tone
 * - Phân loại rõ ràng: Phím số thường, Phím xóa 'C' (Rose/Coral), Phím lùi '⌫' (Amber/Gold)
 * - Nén đàn hồi xúc giác chân thực (Spring Bouncy) khi ngón tay ấn xuống
 */
@Composable
fun TactileKeyButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActionKey: Boolean = false, // Backspace
    isDangerKey: Boolean = false, // Clear 'C'
    height: Dp = 54.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "keyScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 0.5.dp else 2.5.dp,
        label = "keyElev"
    )

    val shape = RoundedCornerShape(15.dp)

    val (bgBrush, borderBrush, textColor, shadowColor) = when {
        isDangerKey -> {
            // Phím C: Tone Rose/Coral tinh tế
            val bg = if (isPressed) {
                Brush.verticalGradient(listOf(Color(0xFFFFE4E6), Color(0xFFFECDD3)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6)))
            }
            val border = Brush.linearGradient(listOf(Color(0xFFFDA4AF), Color(0xFFFECDD3)))
            QuadKeyColors(bg, border, Color(0xFFE11D48), Color(0xFFE11D48))
        }
        isActionKey -> {
            // Phím ⌫: Tone Amber/Gold ấm áp
            val bg = if (isPressed) {
                Brush.verticalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)))
            }
            val border = Brush.linearGradient(listOf(Color(0xFFFCD34D), Color(0xFFFDE68A)))
            QuadKeyColors(bg, border, Color(0xFFD97706), Color(0xFFD97706))
        }
        else -> {
            // Phím số thường: Sứ trắng ngọc trai với bevel phản quang
            val bg = if (isPressed) {
                Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
            } else {
                Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFC)))
            }
            val border = if (isPressed) {
                Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
            } else {
                Brush.linearGradient(
                    listOf(
                        Color.White,
                        Color(0xFFE2E8F0),
                        Color(0xFFCBD5E1)
                    )
                )
            }
            QuadKeyColors(bg, border, Color(0xFF0F172A), Color(0xFF64748B))
        }
    }

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = shadowColor.copy(alpha = 0.15f),
                spotColor = shadowColor.copy(alpha = 0.12f)
            )
            .clip(shape)
            .background(bgBrush)
            .border(width = 1.1.dp, brush = borderBrush, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (label == "⌫" || label == "C") 18.sp else 22.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

private data class QuadKeyColors(
    val bg: Brush,
    val border: Brush,
    val text: Color,
    val shadow: Color
)

/**
 * Phím chip cộng nhanh số tiền dạng viên thuốc xúc giác (+10k, +50k... và nút Xóa hết)
 */
@Composable
fun TactileQuickChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDanger: Boolean = false,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "chipScale"
    )

    val shape = RoundedCornerShape(11.dp)

    val bgBrush = if (isDanger) {
        if (isPressed) Brush.verticalGradient(listOf(Color(0xFFFEE2E2), Color(0xFFFECDD3)))
        else Brush.verticalGradient(listOf(Color(0xFFFFF1F2), Color(0xFFFEE2E2)))
    } else {
        if (isPressed) Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
        else Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFC)))
    }

    val borderBrush = if (isDanger) {
        Brush.linearGradient(listOf(Color(0xFFFECDD3), Color(0xFFFDA4AF)))
    } else {
        Brush.linearGradient(listOf(Color.White, Color(0xFFE2E8F0)))
    }

    val contentColor = if (isDanger) Color(0xFFE11D48) else Color(0xFF0F172A)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 0.5.dp else 1.5.dp,
                shape = shape,
                ambientColor = if (isDanger) Color(0xFFE11D48).copy(alpha = 0.15f) else Color(0xFF64748B).copy(alpha = 0.10f)
            )
            .clip(shape)
            .background(bgBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 11.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

/**
 * Tab lớn chuyển đổi loại giao dịch xúc giác (Chi Tiêu vs Thu Nhập)
 * Có phản hồi chạm đàn hồi, viền phản quang và chấm trạng thái phát sáng
 */
@Composable
fun TactileTypeTab(
    title: String,
    isSelected: Boolean,
    activeGradient: List<Color>,
    glowColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isSelected) 1.02f else 0.98f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tabScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) (if (isPressed) 2.dp else 5.dp) else 1.dp,
        label = "tabElev"
    )

    val shape = RoundedCornerShape(18.dp)

    val bgBrush = if (isSelected) {
        Brush.verticalGradient(activeGradient)
    } else {
        Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFC)))
    }

    val borderBrush = if (isSelected) {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.65f),
                Color.White.copy(alpha = 0.20f),
                glowColor.copy(alpha = 0.50f)
            )
        )
    } else {
        SpecularBorderBrush
    }

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (isSelected) glowColor.copy(alpha = 0.40f) else Color(0xFF64748B).copy(alpha = 0.10f),
                spotColor = if (isSelected) glowColor.copy(alpha = 0.35f) else Color(0xFF0F172A).copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(bgBrush)
            .border(width = if (isSelected) 1.5.dp else 1.dp, brush = borderBrush, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else glowColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF475569)
            )
        }
    }
}

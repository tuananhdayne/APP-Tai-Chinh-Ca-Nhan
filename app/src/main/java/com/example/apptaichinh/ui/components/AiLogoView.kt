package com.example.apptaichinh.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Các kích thước chuẩn cho Logo Trợ Lý AI
 */
enum class AiLogoSize(val dp: Dp) {
    TINY(18.dp),     // Huy hiệu nhỏ inline
    SMALL(28.dp),    // Avatar bong bóng chat & thanh tiêu đề
    MEDIUM(48.dp),   // Bong bóng nổi màn hình (Floating Bubble) & Cards
    LARGE(76.dp),    // Hero banner màn hình chào đón (Empty state)
    XLARGE(96.dp)    // Splash / Showcase
}

/**
 * Biểu tượng Trợ lý AI thế hệ mới (Next-Gen AI Star Logo)
 * - Lấy cảm hứng từ ngôi sao 4 cánh tỏa sáng siêu cong (radiant astroid star) của Google Gemini / DeepMind.
 * - Phối màu gradient đa sắc hiện đại: Indigo -> Electric Violet -> Cyan -> Amber Spark.
 * - Hào quang neon tỏa sáng (Glow aura) với hiệu ứng nhịp thở êm ái.
 * - Trạng thái suy nghĩ (isThinking): Xoay luồng ánh sáng đa chiều & nhịp thở tăng tốc.
 */
@Composable
fun AiLogoView(
    modifier: Modifier = Modifier,
    size: Dp = AiLogoSize.SMALL.dp,
    isThinking: Boolean = false,
    withGlow: Boolean = true,
    withBackground: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aiLogoTransition")

    // Nhịp thở scale nhẹ nhàng
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isThinking) 1.10f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isThinking) 700 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Xoay gradient khi đang suy nghĩ
    val thinkingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinkingRotation"
    )

    // Độ rực rỡ của hào quang
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = if (isThinking) 0.6f else 0.35f,
        targetValue = if (isThinking) 0.95f else 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isThinking) 600 else 1600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    val content = @Composable {
        Box(
            modifier = Modifier
                .size(size)
                .scale(pulseScale),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                val radius = size.toPx() / 2f

                // 1. Hào quang tỏa sáng (Ambient Glow Aura)
                if (withGlow) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = auraAlpha),
                                Color(0xFF06B6D4).copy(alpha = auraAlpha * 0.5f),
                                Color(0xFFEC4899).copy(alpha = auraAlpha * 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius * 0.98f
                        ),
                        radius = radius * 0.98f,
                        center = center
                    )
                }

                // 2. Vẽ ngôi sao 4 cánh chính (Primary Radiant 4-Point Star)
                val starPath = createAstroidStarPath(
                    center = center,
                    outerRadius = radius * 0.78f,
                    innerWaistRatio = 0.22f
                )

                val gradientAngle = if (isThinking) thinkingRotation else 45f
                rotate(degrees = gradientAngle, pivot = center) {
                    val starBrush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF38BDF8), // Electric Cyan
                            Color(0xFF6366F1), // Royal Indigo
                            Color(0xFF8B5CF6), // Vivid Violet
                            Color(0xFFEC4899)  // Neon Pink
                        ),
                        start = Offset(center.x - radius, center.y - radius),
                        end = Offset(center.x + radius, center.y + radius)
                    )
                    drawPath(path = starPath, brush = starBrush, style = Fill)
                }

                // 3. Ngôi sao phụ 4 cánh nhỏ chéo 45 độ (Secondary Diagonal Spark)
                val subStarPath = createAstroidStarPath(
                    center = center,
                    outerRadius = radius * 0.36f,
                    innerWaistRatio = 0.18f
                )
                rotate(degrees = (if (isThinking) -thinkingRotation else 45f) + 45f, pivot = center) {
                    val subStarBrush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFDE047), // Sunny Amber
                            Color(0xFFF472B6)  // Soft Rose
                        ),
                        start = Offset(center.x - radius * 0.4f, center.y - radius * 0.4f),
                        end = Offset(center.x + radius * 0.4f, center.y + radius * 0.4f)
                    )
                    drawPath(path = subStarPath, brush = subStarBrush, style = Fill)
                }

                // 4. Lõi sáng kim cương trung tâm (Bright Center Energy Core)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color(0xFFE0E7FF).copy(alpha = 0.7f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 0.22f
                    ),
                    radius = radius * 0.22f,
                    center = center
                )
            }
        }
    }

    if (withBackground) {
        // Nền kính mờ bo tròn cao cấp (Glassmorphism Pod)
        Box(
            modifier = modifier
                .size(size * 1.35f)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E1B4B).copy(alpha = 0.92f), // Deep Midnight Indigo
                            Color(0xFF0F172A).copy(alpha = 0.95f)  // Slate Obsidian
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color(0xFF818CF8).copy(alpha = 0.4f),
                            Color(0xFF06B6D4).copy(alpha = 0.3f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            content()
        }
    }
}

/**
 * Quá trình tạo hình ngôi sao cong 4 cánh siêu mượt (Astroid/Superellipse Curve)
 * Sử dụng cubic Bézier Curves để tạo góc lượn tự nhiên chuẩn phong cách Google DeepMind.
 */
private fun createAstroidStarPath(
    center: Offset,
    outerRadius: Float,
    innerWaistRatio: Float
): Path {
    val path = Path()
    val cx = center.x
    val cy = center.y
    val r = outerRadius
    val w = outerRadius * innerWaistRatio // Bán kính thắt eo ở giữa

    // 4 đỉnh chính: Top, Right, Bottom, Left
    val top = Offset(cx, cy - r)
    val right = Offset(cx + r, cy)
    val bottom = Offset(cx, cy + r)
    val left = Offset(cx - r, cy)

    // Bắt đầu từ đỉnh Top
    path.moveTo(top.x, top.y)

    // Từ Top sang Right: uốn lượn qua vùng eo (cx + w, cy - w)
    path.cubicTo(
        cx, cy - w,
        cx + w, cy,
        right.x, right.y
    )

    // Từ Right sang Bottom: uốn lượn qua vùng eo (cx + w, cy + w)
    path.cubicTo(
        cx + w, cy,
        cx, cy + w,
        bottom.x, bottom.y
    )

    // Từ Bottom sang Left: uốn lượn qua vùng eo (cx - w, cy + w)
    path.cubicTo(
        cx, cy + w,
        cx - w, cy,
        left.x, left.y
    )

    // Từ Left về lại Top: uốn lượn qua vùng eo (cx - w, cy - w)
    path.cubicTo(
        cx - w, cy,
        cx, cy - w,
        top.x, top.y
    )

    path.close()
    return path
}

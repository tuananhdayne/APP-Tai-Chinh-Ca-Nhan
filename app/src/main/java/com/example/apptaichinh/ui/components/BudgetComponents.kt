package com.example.apptaichinh.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptaichinh.data.model.CategoryBudget
import com.example.apptaichinh.data.model.OverallBudget
import com.example.apptaichinh.theme.CarbonSurfaceGlass
import com.example.apptaichinh.theme.CarbonSurfaceRecessed
import com.example.apptaichinh.theme.NeonAmber
import com.example.apptaichinh.theme.NeonAzure
import com.example.apptaichinh.theme.NeonCoral
import com.example.apptaichinh.theme.NeonEmerald
import com.example.apptaichinh.theme.SpecularBorderBrush
import com.example.apptaichinh.theme.TextHighContrast
import com.example.apptaichinh.theme.TextMediumContrast
import com.example.apptaichinh.theme.TextMuted
import kotlin.math.cos
import kotlin.math.sin

/**
 * Thẻ Ngân Sách Tổng Thể 3D Neumorphic-Glassmorphism (Panel 1)
 * - Vòng tròn tiến trình 3D có chiều sâu (3D Circular Progress Gauge) với chuyển sắc Neon đa tầng.
 * - Hiệu ứng lơ lửng xúc giác (Tactile Floating Console) với bóng đổ kép mềm mại.
 * - Hai bảng số liệu lơ lửng hiển thị rõ ràng: "Đã chi tiêu" và "Còn lại".
 */
@Composable
fun OverallBudgetCard(
    budget: OverallBudget,
    onEditBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = budget.percentage.coerceIn(0f, 1f)
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    // Màu trạng thái dựa trên % chi tiêu
    val statusColor = when {
        budget.isOverBudget -> NeonCoral
        budget.percentage > 0.8f -> NeonAmber
        else -> NeonEmerald
    }

    val infiniteTransition = rememberInfiniteTransition(label = "gaugePulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    NeumorphicGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = 8.dp,
        containerColor = CarbonSurfaceGlass.copy(alpha = 0.88f),
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Tiêu đề & Nút sửa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Glyph3DIcon(
                        icon = "🎯",
                        accentColor = statusColor,
                        size = 38.dp,
                        fontSize = 18f
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Ngân Sách Tổng Cả Tháng",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = if (budget.isOverBudget) "⚠️ Đã vượt hạn mức ngân sách!" else "Kế hoạch chi tiêu chủ động",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (budget.isOverBudget) NeonCoral else TextMediumContrast
                        )
                    }
                }

                // Nút sửa xúc giác
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF))
                        .border(1.dp, Color(0xFFBFDBFE), CircleShape)
                        .clickable { onEditBudget() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Sửa ngân sách",
                        tint = NeonAzure,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // VÒNG TRÒN TIẾN TRÌNH 3D CÓ CHIỀU SÂU (3D Depth Circular Gauge)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(184.dp)) {
                    val strokeWidth = 16.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = arcSize / 2f

                    // 1. Rãnh nền lõm 3D (Recessed Track)
                    drawArc(
                        color = Color(0xFFE2E8F0),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Viền phản quang bên trong rãnh
                    drawArc(
                        color = Color.White.copy(alpha = 0.6f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth * 0.9f, cap = StrokeCap.Round)
                    )

                    // 2. Vòng tiến trình 3D Gradient tỏa sáng
                    val currentSweep = animatedProgress.value * 270f
                    if (currentSweep > 0f) {
                        val gradientBrush = Brush.sweepGradient(
                            colors = listOf(
                                NeonEmerald,
                                NeonAzure,
                                NeonAmber,
                                NeonCoral
                            ),
                            center = center
                        )

                        // Lớp tỏa sáng mờ dưới arc (Neon Bloom)
                        drawArc(
                            brush = gradientBrush,
                            startAngle = 135f,
                            sweepAngle = currentSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth * 1.25f, cap = StrokeCap.Round),
                            alpha = pulseGlow * 0.45f
                        )

                        // Lớp arc chính
                        drawArc(
                            brush = gradientBrush,
                            startAngle = 135f,
                            sweepAngle = currentSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Đầu hạt ngọc phát sáng ở chóp cung (Glow Tip Bead)
                        val angleRad = Math.toRadians((135f + currentSweep).toDouble())
                        val beadX = center.x + radius * cos(angleRad).toFloat()
                        val beadY = center.y + radius * sin(angleRad).toFloat()

                        drawCircle(
                            color = Color.White,
                            radius = strokeWidth * 0.42f,
                            center = Offset(beadX, beadY)
                        )
                        drawCircle(
                            color = statusColor.copy(alpha = pulseGlow),
                            radius = strokeWidth * 0.85f,
                            center = Offset(beadX, beadY)
                        )
                    }
                }

                // Cụm thông tin xúc giác ở giữa vòng tròn
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val pct = (budget.percentage * 100).coerceAtLeast(0f)
                    Text(
                        text = String.format("%.0f%%", pct),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextHighContrast,
                        letterSpacing = (-0.5).sp
                    )

                    Text(
                        text = "ĐÃ SỬ DỤNG",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMediumContrast,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    TactilePillChip(
                        text = if (budget.isOverBudget) "Vượt mức" else if (budget.percentage > 0.8f) "Cảnh báo" else "An toàn",
                        accentColor = statusColor,
                        leadingDot = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // HAI BẢNG SỐ LIỆU LƠ LỬNG (Floating Metrics Consoles)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Thẻ Đã Chi
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonCoral)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Đã chi tiêu",
                                fontSize = 11.5.sp,
                                color = Color(0xFFB91C1C),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = Formatters.formatVnd(budget.totalExpense),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCoral
                        )
                    }
                }

                // Thẻ Còn Lại
                val isRemainingPositive = budget.remaining >= 0
                val remainingBg = if (isRemainingPositive) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                val remainingBorder = if (isRemainingPositive) Color(0xFFA7F3D0) else Color(0xFFFECDD3)
                val remainingTextColor = if (isRemainingPositive) Color(0xFF047857) else Color(0xFFB91C1C)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(remainingBg)
                        .border(1.dp, remainingBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isRemainingPositive) NeonEmerald else NeonCoral)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRemainingPositive) "Còn lại" else "Vượt mức",
                                fontSize = 11.5.sp,
                                color = remainingTextColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = Formatters.formatVnd(Math.abs(budget.remaining)),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRemainingPositive) NeonEmerald else NeonCoral
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dòng Hạn mức mục tiêu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hạn mức tối đa cả tháng:",
                    fontSize = 12.sp,
                    color = TextMediumContrast
                )
                Text(
                    text = Formatters.formatVnd(budget.totalBudget),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHighContrast
                )
            }
        }
    }
}

/**
 * Thẻ Ngân Sách Cho Từng Danh Mục Riêng Biệt (Category Budget Card)
 * - Thiết kế Card lơ lửng riêng biệt (Floating Glass Tile) với biểu tượng 3D tùy chỉnh.
 * - Thanh tiến độ Neon chuyển sắc mượt mà với hiệu ứng phản quang.
 */
@Composable
fun CategoryBudgetCard(
    categoryBudget: CategoryBudget,
    onEditCategoryBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = categoryBudget.percentage.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "catBudgetProgress"
    )

    val catColor = Formatters.parseColor(categoryBudget.category.colorHex)
    val statusColor = when {
        categoryBudget.isOverBudget -> NeonCoral
        categoryBudget.percentage > 0.8f -> NeonAmber
        else -> NeonEmerald
    }

    NeumorphicGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEditCategoryBudget() },
        shape = RoundedCornerShape(20.dp),
        elevation = 4.dp,
        containerColor = CarbonSurfaceGlass.copy(alpha = 0.85f),
        contentPadding = PaddingValues(15.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Biểu tượng 3D Glyph của danh mục
                    Glyph3DIcon(
                        icon = categoryBudget.category.icon,
                        accentColor = catColor,
                        size = 44.dp,
                        fontSize = 20f
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = categoryBudget.category.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = if (categoryBudget.budgetAmount > 0) {
                                "Hạn mức: ${Formatters.formatVnd(categoryBudget.budgetAmount)}"
                            } else {
                                "Chưa đặt ngân sách"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumContrast
                        )
                    }
                }

                // Cột bên phải: Số tiền đã chi & %
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatVnd(categoryBudget.spentAmount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (categoryBudget.isOverBudget) NeonCoral else TextHighContrast
                    )
                    if (categoryBudget.budgetAmount > 0) {
                        val pctStr = String.format("%.0f%%", categoryBudget.percentage * 100)
                        TactilePillChip(
                            text = if (categoryBudget.isOverBudget) "Vượt $pctStr" else "$pctStr",
                            accentColor = statusColor,
                            leadingDot = false
                        )
                    }
                }
            }

            if (categoryBudget.budgetAmount > 0) {
                Spacer(modifier = Modifier.height(12.dp))

                // Thanh tiến độ Neon có rãnh 3D
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE2E8F0))
                        .border(0.8.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        catColor,
                                        statusColor
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (categoryBudget.remaining >= 0) {
                            "Còn lại: ${Formatters.formatVnd(categoryBudget.remaining)}"
                        } else {
                            "Vượt: ${Formatters.formatVnd(Math.abs(categoryBudget.remaining))}"
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (categoryBudget.remaining >= 0) TextMediumContrast else NeonCoral
                    )

                    Text(
                        text = "Chạm để chỉnh sửa ❯",
                        fontSize = 11.sp,
                        color = NeonAzure,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "+ Chạm để thiết lập hạn mức cho mục này",
                    fontSize = 12.sp,
                    color = NeonAzure,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

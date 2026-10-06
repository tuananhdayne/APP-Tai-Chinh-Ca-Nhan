package com.example.apptaichinh.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.apptaichinh.data.model.CategoryStat
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
 * Biểu Đồ Tròn Donut Phân Tích Danh Mục Chi Tiêu Với Hiệu Ứng Neon Mờ (Panel 2)
 * - Render Donut Chart với quầng hào quang phát sáng (Neon Bloom & Dual-Pass Shader).
 * - Bảng điều khiển kính mờ (Frosted Glass Panel) với viền tóc phản quang.
 * - Đĩa trung tâm 3D hiển thị tổng chi tiêu.
 * - Danh sách phân tích tỷ lệ dạng thẻ kính xúc giác bên dưới.
 */
@Composable
fun DonutChart(
    stats: List<CategoryStat>,
    totalExpense: Long,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(stats) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing)
        )
    }

    NeumorphicGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = 6.dp,
        containerColor = CarbonSurfaceGlass.copy(alpha = 0.88f),
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Glyph3DIcon(icon = "📊", accentColor = NeonAzure, size = 36.dp, fontSize = 17f)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Cơ Cấu Chi Tiêu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = "Phân bổ theo từng nhóm danh mục",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumContrast
                        )
                    }
                }
                TactilePillChip(
                    text = "${stats.size} nhóm",
                    accentColor = NeonAzure,
                    leadingDot = false
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (stats.isEmpty() || totalExpense == 0L) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có phát sinh chi tiêu trong tháng này",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            } else {
                // Khối Donut Neon 3D
                Box(
                    modifier = Modifier.size(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(190.dp)) {
                        val strokeWidth = 26.dp.toPx()
                        val arcSize = size.width - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                        // 1. Rãnh nền tối lõm phía dưới (Recessed Base Ring)
                        drawArc(
                            color = Color(0xFF0C101A),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth)
                        )

                        // 2. Pass 1: Lớp Neon Glow mờ phía sau
                        var startAngle = -90f
                        stats.forEach { stat ->
                            val sweepAngle = stat.percentage * 360f * animatedProgress.value
                            if (sweepAngle > 0f) {
                                val color = Formatters.parseColor(stat.colorHex)
                                drawArc(
                                    color = color.copy(alpha = 0.40f),
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth * 1.35f, cap = StrokeCap.Round)
                                )
                                startAngle += sweepAngle
                            }
                        }

                        // 3. Pass 2: Lớp cung màu chính sắc nét
                        startAngle = -90f
                        stats.forEach { stat ->
                            val sweepAngle = stat.percentage * 360f * animatedProgress.value
                            if (sweepAngle > 0f) {
                                val color = Formatters.parseColor(stat.colorHex)
                                drawArc(
                                    color = color,
                                    startAngle = startAngle + 1f, // Khoảng cách nhỏ giữa các phân đoạn
                                    sweepAngle = (sweepAngle - 2f).coerceAtLeast(1f),
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                startAngle += sweepAngle
                            }
                        }
                    }

                    // Đĩa gương 3D ở trung tâm Donut
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .shadow(elevation = 6.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF1C2538),
                                        Color(0xFF0D121D)
                                    )
                                )
                            )
                            .border(1.2.dp, SpecularBorderBrush, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TỔNG CHI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Formatters.formatVnd(totalExpense),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonCoral,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Danh sách phân tích tỷ lệ dạng hàng kính
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stats.forEach { stat ->
                        val color = Formatters.parseColor(stat.colorHex)
                        val percentStr = String.format("%.1f%%", stat.percentage * 100)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F1522).copy(alpha = 0.7f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Chấm ngọc màu danh mục
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(color.copy(alpha = 0.18f))
                                            .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = stat.categoryIcon, fontSize = 14.sp)
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = stat.categoryName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextHighContrast
                                        )
                                        // Thanh chỉ báo độ dài nhỏ
                                        Box(
                                            modifier = Modifier
                                                .width(80.dp)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color(0xFF1E2638))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(stat.percentage)
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(color)
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = Formatters.formatVnd(stat.amount),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextHighContrast
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TactilePillChip(
                                        text = percentStr,
                                        accentColor = color,
                                        leadingDot = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Biểu Đồ So Sánh Dòng Tiền Thu Nhập vs Chi Tiêu 3D (Panel 2)
 * - Các thanh biểu đồ có chiều sâu, chuyển sắc rõ ràng cho thu - chi.
 * - Thước tỷ lệ so sánh ngang với hiệu ứng kính mờ (Frosted Glass).
 */
@Composable
fun IncomeVsExpenseComparison(
    income: Long,
    expense: Long,
    modifier: Modifier = Modifier
) {
    val total = (income + expense).coerceAtLeast(1L)
    val incomeFraction = (income.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val expenseFraction = (expense.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val netCashFlow = income - expense

    NeumorphicGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = 6.dp,
        containerColor = CarbonSurfaceGlass.copy(alpha = 0.88f),
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Glyph3DIcon(icon = "⚖️", accentColor = NeonEmerald, size = 36.dp, fontSize = 17f)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "So Sánh Dòng Tiền",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = "Tỷ lệ Thu nhập vs Chi tiêu trong tháng",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumContrast
                        )
                    }
                }

                TactilePillChip(
                    text = if (netCashFlow >= 0) "Dương tiền" else "Âm tiền",
                    accentColor = if (netCashFlow >= 0) NeonEmerald else NeonCoral,
                    leadingDot = true
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // THƯỚC ĐO TỶ LỆ DÒNG TIỀN DẠNG KÍNH (Glassmorphic Flow Bar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0C101A))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(3.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (incomeFraction > 0.01f) {
                        Box(
                            modifier = Modifier
                                .weight(incomeFraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 11.dp, bottomStart = 11.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            NeonEmerald,
                                            Color(0xFF059669)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (incomeFraction > 0.15f) {
                                Text(
                                    text = String.format("%.0f%%", incomeFraction * 100),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (expenseFraction > 0.01f) {
                        Box(
                            modifier = Modifier
                                .weight(expenseFraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topEnd = 11.dp, bottomEnd = 11.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFDC2626),
                                            NeonCoral
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (expenseFraction > 0.15f) {
                                Text(
                                    text = String.format("%.0f%%", expenseFraction * 100),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // HAI CỘT THỐNG KÊ CHI TIẾT 3D (Tactile Stats Panels)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Khối Thu Nhập
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0F1522).copy(alpha = 0.9f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald)
                                    .shadow(elevation = 3.dp, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tổng thu nhập",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Formatters.formatVnd(income),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonEmerald
                        )
                    }
                }

                // Khối Chi Tiêu
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0F1522).copy(alpha = 0.9f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(NeonCoral)
                                    .shadow(elevation = 3.dp, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tổng chi tiêu",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Formatters.formatVnd(expense),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonCoral
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dòng Tổng chênh lệch ròng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chênh lệch dòng tiền:",
                    fontSize = 12.sp,
                    color = TextMediumContrast
                )
                Text(
                    text = "${if (netCashFlow >= 0) "+" else ""}${Formatters.formatVnd(netCashFlow)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (netCashFlow >= 0) NeonEmerald else NeonCoral
                )
            }
        }
    }
}

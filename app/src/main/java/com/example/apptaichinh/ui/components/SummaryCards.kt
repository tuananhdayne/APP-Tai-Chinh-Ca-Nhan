package com.example.apptaichinh.ui.components

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptaichinh.data.model.MonthSummary
import com.example.apptaichinh.theme.CarbonSurfaceGlass
import com.example.apptaichinh.theme.NeonAzure
import com.example.apptaichinh.theme.NeonCoral
import com.example.apptaichinh.theme.NeonEmerald
import com.example.apptaichinh.theme.SpecularBorderBrush
import com.example.apptaichinh.theme.TextHighContrast
import com.example.apptaichinh.theme.TextMediumContrast
import com.example.apptaichinh.theme.TextMuted

/**
 * Thanh chọn Tháng theo phong cách Kính Mờ Lơ Lửng (Neumorphic Glass Month Selector)
 */
@Composable
fun MonthSelector(
    year: Int,
    month: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthStr = String.format("Tháng %02d/%d", month + 1, year)

    NeumorphicGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = 3.dp,
        containerColor = CarbonSurfaceGlass.copy(alpha = 0.85f),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, SpecularBorderBrush, CircleShape)
                    .clickable { onPrev() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Tháng trước",
                    tint = TextHighContrast,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = monthStr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextHighContrast
                )
                Text(
                    text = "Sổ thu chi cá nhân",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMediumContrast
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, SpecularBorderBrush, CircleShape)
                    .clickable { onNext() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Tháng sau",
                    tint = TextHighContrast,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Thẻ Tổng Quan Số Dư Trong Tháng (Neumorphic Glass Balance Summary Card)
 */
@Composable
fun BalanceSummaryCard(
    summary: MonthSummary,
    modifier: Modifier = Modifier
) {
    NeumorphicGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = 3.dp,
        containerColor = Color.White,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Số Dư Ròng Trong Tháng",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMediumContrast
                )
                TactilePillChip(
                    text = if (summary.balance >= 0) "Thặng dư" else "Thâm hụt",
                    accentColor = if (summary.balance >= 0) NeonEmerald else NeonCoral,
                    leadingDot = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = Formatters.formatVnd(summary.balance),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (summary.balance >= 0) TextHighContrast else NeonCoral,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Dòng Thu Nhập & Chi Tiêu tách biệt
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Thu nhập
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFECFDF5))
                        .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(NeonEmerald.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Thu nhập",
                                fontSize = 11.5.sp,
                                color = TextMuted
                            )
                            Text(
                                text = Formatters.formatVnd(summary.totalIncome),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald
                            )
                        }
                    }
                }

                // Chi tiêu
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(NeonCoral.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = NeonCoral,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Chi tiêu",
                                fontSize = 11.5.sp,
                                color = TextMuted
                            )
                            Text(
                                text = Formatters.formatVnd(summary.totalExpense),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCoral
                            )
                        }
                    }
                }
            }
        }
    }
}

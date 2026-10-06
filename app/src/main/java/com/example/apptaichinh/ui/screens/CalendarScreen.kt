package com.example.apptaichinh.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptaichinh.data.model.Transaction
import com.example.apptaichinh.theme.ActiveNeonBorderBrush
import com.example.apptaichinh.theme.CarbonSurfaceGlass
import com.example.apptaichinh.theme.NeonAmber
import com.example.apptaichinh.theme.NeonAzure
import com.example.apptaichinh.theme.NeonCoral
import com.example.apptaichinh.theme.NeonEmerald
import com.example.apptaichinh.theme.SpecularBorderBrush
import com.example.apptaichinh.theme.TextHighContrast
import com.example.apptaichinh.theme.TextMediumContrast
import com.example.apptaichinh.theme.TextMuted
import com.example.apptaichinh.ui.components.Formatters
import com.example.apptaichinh.ui.components.Glyph3DIcon
import com.example.apptaichinh.ui.components.NeumorphicGlassCard
import com.example.apptaichinh.ui.components.TactilePillChip
import com.example.apptaichinh.ui.components.TransactionItemView
import com.example.apptaichinh.ui.viewmodel.FinanceViewModel
import java.util.Calendar

/**
 * Màn Hình Lịch Thu Chi Theo Phong Cách Skeuomorphic-Neumorphic & Subtle Glassmorphism (Panel 3)
 * - Lưới lịch với các ô ngày làm mờ nhẹ (Frosted Tiles) và các hạt ngọc đánh dấu giao dịch (Jewel Dots).
 * - Ngày đang chọn nổi bật với viền Neon đa sắc 3D và phản hồi xúc giác khi chạm.
 * - Widget tóm tắt hàng ngày dạng bảng lơ lửng sành điệu (Floating Console) với 3 thẻ số liệu trực quan.
 */
@Composable
fun CalendarScreen(
    viewModel: FinanceViewModel,
    onEditTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val year by viewModel.selectedYear.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val monthSummary by viewModel.monthSummary.collectAsState()

    val todayCal = remember { Calendar.getInstance() }
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    var selectedDay by remember(year, month) {
        val defaultDay = if (year == todayYear && month == todayMonth) todayDay else 1
        mutableStateOf(defaultDay)
    }

    val monthCal = remember(year, month) {
        Calendar.getInstance().apply {
            set(year, month, 1)
        }
    }
    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startDayOfWeek = (monthCal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7

    // Nhóm giao dịch theo ngày
    val transactionsByDay = remember(transactions, year, month) {
        val map = mutableMapOf<Int, MutableList<Transaction>>()
        val txCal = Calendar.getInstance()
        for (tx in transactions) {
            txCal.timeInMillis = tx.dateEpoch
            if (txCal.get(Calendar.YEAR) == year && txCal.get(Calendar.MONTH) == month) {
                val day = txCal.get(Calendar.DAY_OF_MONTH)
                map.getOrPut(day) { mutableListOf() }.add(tx)
            }
        }
        map
    }

    val selectedDayTransactions = transactionsByDay[selectedDay] ?: emptyList()
    val dayIncome = selectedDayTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val dayExpense = selectedDayTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val dayBalance = dayIncome - dayExpense

    var viewMode by remember { mutableStateOf(0) } // 0: Ngày đang chọn, 1: Cả tháng

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 95.dp, top = 8.dp, start = 14.dp, end = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. THANH CHỌN THÁNG DẠNG KÍNH (Month Selector Header)
        item {
            NeumorphicGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 4.dp,
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
                            .background(Color(0xFF161E2E))
                            .border(1.dp, SpecularBorderBrush, CircleShape)
                            .clickable { viewModel.prevMonth() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước", tint = TextHighContrast, modifier = Modifier.size(20.dp))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tháng ${String.format("%02d", month + 1)} / $year",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        if (year == todayYear && month == todayMonth) {
                            Text(
                                text = "● Tháng hiện tại",
                                fontSize = 11.sp,
                                color = NeonAzure,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (year != todayYear || month != todayMonth) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonAzure.copy(alpha = 0.15f))
                                    .clickable {
                                        viewModel.setMonth(todayYear, todayMonth)
                                        selectedDay = todayDay
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Today, contentDescription = "Về hôm nay", tint = NeonAzure, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF161E2E))
                                .border(1.dp, SpecularBorderBrush, CircleShape)
                                .clickable { viewModel.nextMonth() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau", tint = TextHighContrast, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // 2. LƯỚI LỊCH THÁNG (Calendar Grid With Frosted Tiles & Jewel Dots)
        item {
            NeumorphicGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = 6.dp,
                containerColor = CarbonSurfaceGlass.copy(alpha = 0.88f),
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Tiêu đề các thứ trong tuần
                    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysOfWeek.forEachIndexed { idx, dayName ->
                            val isSunday = idx == 6
                            Text(
                                text = dayName,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSunday) NeonCoral else TextMediumContrast
                            )
                        }
                    }

                    // Lưới các ô ngày
                    val totalCells = startDayOfWeek + daysInMonth
                    val totalRows = (totalCells + 6) / 7

                    for (rowIndex in 0 until totalRows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (colIndex in 0..6) {
                                val cellIndex = rowIndex * 7 + colIndex
                                val dayNumber = cellIndex - startDayOfWeek + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val isSelected = dayNumber == selectedDay
                                    val isToday = year == todayYear && month == todayMonth && dayNumber == todayDay
                                    val dayTx = transactionsByDay[dayNumber] ?: emptyList()
                                    val hasIncome = dayTx.any { it.type == "INCOME" }
                                    val hasExpense = dayTx.any { it.type == "EXPENSE" }

                                    val scale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.05f else 1f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                        label = "cellScale"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp)
                                            .padding(2.dp)
                                            .scale(scale)
                                            .shadow(
                                                elevation = if (isSelected) 6.dp else 1.dp,
                                                shape = RoundedCornerShape(12.dp),
                                                ambientColor = if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color.Black
                                            )
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                when {
                                                    isSelected -> Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color(0xFF1E293B),
                                                            Color(0xFF0F172A)
                                                        )
                                                    )
                                                    isToday -> Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color(0xFF162032),
                                                            Color(0xFF0D1424)
                                                        )
                                                    )
                                                    else -> Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color(0xFF121724).copy(alpha = 0.8f),
                                                            Color(0xFF0B0F18).copy(alpha = 0.9f)
                                                        )
                                                    )
                                                }
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else if (isToday) 1.dp else 0.8.dp,
                                                brush = when {
                                                    isSelected -> ActiveNeonBorderBrush
                                                    isToday -> Brush.linearGradient(listOf(NeonAzure, Color.Transparent))
                                                    else -> Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent))
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedDay = dayNumber }
                                            .padding(top = 4.dp, bottom = 4.dp),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            // Số ngày
                                            Text(
                                                text = "$dayNumber",
                                                fontSize = 12.5.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                color = when {
                                                    isSelected -> NeonAzure
                                                    colIndex == 6 -> NeonCoral
                                                    else -> TextHighContrast
                                                }
                                            )

                                            // Các hạt ngọc giao dịch (Jewel Dots)
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (hasIncome) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(NeonEmerald)
                                                            .shadow(elevation = 2.dp, shape = CircleShape)
                                                    )
                                                }
                                                if (hasExpense) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(NeonCoral)
                                                            .shadow(elevation = 2.dp, shape = CircleShape)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. WIDGET TÓM TẮT HÀNG NGÀY DẠNG BẢNG LƠ LỬNG (Floating Daily Console)
        item {
            NeumorphicGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = 6.dp,
                containerColor = CarbonSurfaceGlass.copy(alpha = 0.88f),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (viewMode == 0) "Tổng Hợp Ngày $selectedDay/${month + 1}" else "Tổng Hợp Cả Tháng ${month + 1}/$year",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )

                        // Nút chuyển chế độ dạng viên nhộng kép (Dual Pill Toggle)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0C101A))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (viewMode == 0) Color(0xFF1E293B) else Color.Transparent)
                                    .clickable { viewMode = 0 }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Ngày $selectedDay",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewMode == 0) NeonAzure else TextMuted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (viewMode == 1) Color(0xFF1E293B) else Color.Transparent)
                                    .clickable { viewMode = 1 }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Cả tháng",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewMode == 1) NeonAzure else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val curIncome = if (viewMode == 0) dayIncome else monthSummary.totalIncome
                    val curExpense = if (viewMode == 0) dayExpense else monthSummary.totalExpense
                    val curBalance = if (viewMode == 0) dayBalance else monthSummary.balance

                    // 3 Khối Thống kê Xúc giác (Thu / Chi / Dư)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Thu
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F1522).copy(alpha = 0.9f))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Thu nhập",
                                    fontSize = 11.sp,
                                    color = NeonEmerald,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = Formatters.formatVnd(curIncome),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonEmerald,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 2. Chi
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F1522).copy(alpha = 0.9f))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Chi tiêu",
                                    fontSize = 11.sp,
                                    color = NeonCoral,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = Formatters.formatVnd(curExpense),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCoral,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 3. Chênh lệch / Dư
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F1522).copy(alpha = 0.9f))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Số dư ròng",
                                    fontSize = 11.sp,
                                    color = if (curBalance >= 0) NeonAzure else NeonAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = Formatters.formatVnd(curBalance),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (curBalance >= 0) NeonAzure else NeonAmber,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. DANH SÁCH GIAO DỊCH DƯỚI LỊCH
        val displayTransactions = if (viewMode == 0) selectedDayTransactions else transactions

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (viewMode == 0) "Lịch Sử Ngày $selectedDay/${month + 1}" else "Tất Cả Giao Dịch Tháng ${month + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextHighContrast
                )

                TactilePillChip(
                    text = "${displayTransactions.size} giao dịch",
                    accentColor = NeonAzure,
                    leadingDot = false
                )
            }
        }

        if (displayTransactions.isEmpty()) {
            item {
                NeumorphicGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = 2.dp,
                    contentPadding = PaddingValues(28.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📅", fontSize = 34.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (viewMode == 0) "Chưa có giao dịch vào ngày $selectedDay/${month + 1}" else "Chưa có giao dịch nào trong tháng này",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHighContrast
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Chạm nút \"+\" hoặc Trợ Lý AI để thêm chi tiêu mới nhé!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(displayTransactions, key = { it.id }) { tx ->
                TransactionItemView(
                    transaction = tx,
                    onClick = { onEditTransaction(tx) },
                    onDelete = { viewModel.deleteTransaction(tx.id) }
                )
            }
        }
    }
}

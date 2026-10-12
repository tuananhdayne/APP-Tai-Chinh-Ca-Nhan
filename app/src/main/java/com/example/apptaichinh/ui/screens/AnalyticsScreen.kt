package com.example.apptaichinh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.apptaichinh.theme.NeonCoral
import com.example.apptaichinh.theme.NeonEmerald
import com.example.apptaichinh.ui.components.DonutChart
import com.example.apptaichinh.ui.components.IncomeVsExpenseComparison
import com.example.apptaichinh.ui.components.MonthSelector
import com.example.apptaichinh.ui.components.TactileTypeTab
import com.example.apptaichinh.ui.components.TransactionItemView
import com.example.apptaichinh.ui.viewmodel.FinanceViewModel

@Composable
fun AnalyticsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val year by viewModel.selectedYear.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()
    val summary by viewModel.monthSummary.collectAsState()
    val categoryExpenseStats by viewModel.categoryExpenseStats.collectAsState()
    val categoryIncomeStats by viewModel.categoryIncomeStats.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    // Trạng thái chọn xem thống kê: Chi Tiêu (EXPENSE) hoặc Thu Nhập (INCOME)
    var selectedType by remember { mutableStateOf("EXPENSE") }

    // Lấy top 5 khoản lớn nhất theo loại đang chọn
    val topItems = transactions
        .filter { it.type == selectedType }
        .sortedByDescending { it.amount }
        .take(5)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 115.dp, top = 8.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Month Selector
        item {
            MonthSelector(
                year = year,
                month = month,
                onPrev = { viewModel.prevMonth() },
                onNext = { viewModel.nextMonth() }
            )
        }

        // 2. Hai Tabs Lớn Chuyển Đổi Xem Thống Kê: Chi Tiêu (-) vs Thu Nhập (+)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TactileTypeTab(
                    title = "Chi Tiêu (-)",
                    isSelected = selectedType == "EXPENSE",
                    activeGradient = listOf(Color(0xFFFF3366), Color(0xFFE11D48), Color(0xFFBE123C)),
                    glowColor = NeonCoral,
                    onClick = { selectedType = "EXPENSE" },
                    modifier = Modifier.weight(1f)
                )

                TactileTypeTab(
                    title = "Thu Nhập (+)",
                    isSelected = selectedType == "INCOME",
                    activeGradient = listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF047857)),
                    glowColor = NeonEmerald,
                    onClick = { selectedType = "INCOME" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Biểu đồ tròn Donut Chart phân tích danh mục theo lựa chọn Thu / Chi
        item {
            if (selectedType == "EXPENSE") {
                DonutChart(
                    stats = categoryExpenseStats,
                    totalExpense = summary.totalExpense,
                    title = "Cơ Cấu Chi Tiêu",
                    subtitle = "Phân bổ theo từng nhóm danh mục chi tiêu",
                    centerLabel = "TỔNG CHI",
                    centerColor = NeonCoral,
                    emptyMessage = "Chưa có phát sinh chi tiêu trong tháng này",
                    iconEmoji = "📊",
                    accentColor = NeonCoral
                )
            } else {
                DonutChart(
                    stats = categoryIncomeStats,
                    totalExpense = summary.totalIncome,
                    title = "Cơ Cấu Thu Nhập",
                    subtitle = "Phân bổ theo từng nguồn thu nhập",
                    centerLabel = "TỔNG THU",
                    centerColor = NeonEmerald,
                    emptyMessage = "Chưa có phát sinh thu nhập trong tháng này",
                    iconEmoji = "💰",
                    accentColor = NeonEmerald
                )
            }
        }

        // 4. Biểu đồ so sánh Thu nhập vs Chi tiêu (Tổng quan dòng tiền)
        item {
            IncomeVsExpenseComparison(
                income = summary.totalIncome,
                expense = summary.totalExpense
            )
        }

        // 5. Top các khoản lớn nhất trong tháng (theo loại Chi hoặc Thu)
        if (topItems.isNotEmpty()) {
            item {
                Text(
                    text = if (selectedType == "EXPENSE") "Top Khoản Chi Lớn Nhất Tháng" else "Top Khoản Thu Lớn Nhất Tháng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(topItems, key = { "top_${it.id}" }) { tx ->
                TransactionItemView(
                    transaction = tx,
                    onClick = {},
                    onDelete = { viewModel.deleteTransaction(tx.id) }
                )
            }
        }
    }
}

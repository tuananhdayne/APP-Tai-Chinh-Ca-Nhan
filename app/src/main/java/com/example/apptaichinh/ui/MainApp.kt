package com.example.apptaichinh.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.widthIn
import com.example.apptaichinh.data.model.Category
import com.example.apptaichinh.data.model.CategoryBudget
import com.example.apptaichinh.data.model.Transaction
import com.example.apptaichinh.ui.components.AddTransactionSheet
import com.example.apptaichinh.ui.components.AiChatBubble
import com.example.apptaichinh.ui.components.EditBudgetDialog
import com.example.apptaichinh.ui.components.EditCategoryDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.NavigationBarItemDefaults
import com.example.apptaichinh.theme.CarbonBackground
import com.example.apptaichinh.theme.NeonAzure
import com.example.apptaichinh.theme.NeonIndigo
import com.example.apptaichinh.theme.SpecularBorderBrush
import com.example.apptaichinh.theme.TextHighContrast
import com.example.apptaichinh.theme.TextMuted
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import com.example.apptaichinh.ui.screens.AnalyticsScreen
import com.example.apptaichinh.ui.screens.BudgetScreen
import com.example.apptaichinh.ui.screens.CategoryScreen
import com.example.apptaichinh.ui.screens.ChatAssistantScreen
import com.example.apptaichinh.ui.screens.HomeScreen
import com.example.apptaichinh.ui.viewmodel.FinanceViewModel

import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ReceiptLong
import com.example.apptaichinh.ui.screens.CalendarScreen
import com.example.apptaichinh.ui.screens.ManualEntryScreen
import com.example.apptaichinh.ui.screens.MoreScreen
import com.example.apptaichinh.ui.screens.ReceiptCaptureScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val overallBudget by viewModel.overallBudget.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()

    // Dialog & Sheet States
    var showAddTxSheet by remember { mutableStateOf(false) }
    var editingTx by remember { mutableStateOf<Transaction?>(null) }
    var showEntryOptions by remember { mutableStateOf(false) }
    var showManualEntry by remember { mutableStateOf(false) }
    var showReceiptCapture by remember { mutableStateOf(false) }

    var showOverallBudgetDialog by remember { mutableStateOf(false) }
    var editingCategoryBudget by remember { mutableStateOf<CategoryBudget?>(null) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var showCategorySheet by remember { mutableStateOf(false) }

    // Trạng thái mở cửa sổ chat từ bong bóng nổi
    var showAiChatWindow by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 760.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), ambientColor = Color(0xFF64748B).copy(alpha = 0.12f))
                        .border(
                            width = 1.dp,
                            brush = SpecularBorderBrush,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    val itemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonAzure,
                        selectedTextColor = NeonAzure,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = NeonAzure.copy(alpha = 0.12f)
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                        label = { Text("Trang chủ", fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        selected = currentTab == 0,
                        onClick = { viewModel.setCurrentTab(0) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Lịch") },
                        label = { Text("Lịch", fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        selected = currentTab == 1,
                        onClick = { viewModel.setCurrentTab(1) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.PieChart, contentDescription = "Thống Kê") },
                        label = { Text("Thống Kê", fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        selected = currentTab == 2,
                        onClick = { viewModel.setCurrentTab(2) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Savings, contentDescription = "Ngân Sách") },
                        label = { Text("Ngân Sách", fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        selected = currentTab == 3,
                        onClick = { viewModel.setCurrentTab(3) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Cài Đặt") },
                        label = { Text("Cài Đặt", fontWeight = if (currentTab == 4) FontWeight.Bold else FontWeight.Normal) },
                        selected = currentTab == 4,
                        onClick = { viewModel.setCurrentTab(4) },
                        colors = itemColors
                    )
                }
            }
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = NeonAzure.copy(alpha = 0.35f),
                        spotColor = NeonIndigo.copy(alpha = 0.3f)
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                NeonAzure,
                                NeonIndigo
                            )
                        )
                    )
                    .border(1.2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .clickable {
                        showEntryOptions = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm Giao Dịch", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth >= 600.dp
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = if (isTablet) {
                        Modifier
                            .fillMaxHeight()
                            .widthIn(max = 760.dp)
                    } else {
                        Modifier.fillMaxSize()
                    }
                ) {
                    when (currentTab) {
                        0 -> HomeScreen(
                            viewModel = viewModel,
                            onEditTransaction = { tx ->
                                editingTx = tx
                                showAddTxSheet = true
                            },
                            onOpenBudgetEdit = { showOverallBudgetDialog = true },
                            onOpenChat = { showAiChatWindow = true }
                        )

                        1 -> CalendarScreen(
                            viewModel = viewModel,
                            onEditTransaction = { tx ->
                                editingTx = tx
                                showAddTxSheet = true
                            }
                        )

                        2 -> AnalyticsScreen(
                            viewModel = viewModel
                        )

                        3 -> BudgetScreen(
                            viewModel = viewModel,
                            onEditOverallBudget = { showOverallBudgetDialog = true },
                            onEditCategoryBudget = { catBudget ->
                                editingCategoryBudget = catBudget
                            }
                        )

                        4 -> MoreScreen(
                            viewModel = viewModel,
                            onOpenCategories = { showCategorySheet = true },
                            onOpenAiChat = { showAiChatWindow = true }
                        )
                    }
                }
            }

            // Bong bóng Trợ Lý AI nổi trên màn hình (Floating Chat Bubble - Tự do kéo di chuyển)
            if (!showAiChatWindow) {
                AiChatBubble(
                    containerWidth = maxWidth,
                    containerHeight = maxHeight,
                    onClick = { showAiChatWindow = true },
                    isThinking = isAiThinking
                )
            }
        }
    }

    if (showEntryOptions) {
        ModalBottomSheet(
            onDismissRequest = { showEntryOptions = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp)
                        .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    Text("Thêm giao dịch", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Chọn cách bạn muốn ghi nhận khoản thu chi", color = TextMuted)
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            showEntryOptions = false
                            showManualEntry = true
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null)
                        androidx.compose.foundation.layout.Spacer(Modifier.size(10.dp))
                        Text("Nhập thủ công")
                    }
                    androidx.compose.material3.Button(
                        onClick = {
                            showEntryOptions = false
                            showReceiptCapture = true
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null)
                        androidx.compose.foundation.layout.Spacer(Modifier.size(10.dp))
                        Text("Chụp hóa đơn")
                    }
                }
            }
        }
    }

    if (showManualEntry) {
        ModalBottomSheet(
            onDismissRequest = { showManualEntry = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = null
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.94f),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxSize().widthIn(max = 680.dp)) {
                    ManualEntryScreen(
                        viewModel = viewModel,
                        onNavigateToCalendar = {
                            showManualEntry = false
                            viewModel.setCurrentTab(1)
                        }
                    )
                }
            }
        }
    }

    if (showReceiptCapture) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.clearReceipt()
                showReceiptCapture = false
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = null
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.96f),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxSize().widthIn(max = 680.dp)) {
                    ReceiptCaptureScreen(
                        viewModel = viewModel,
                        onClose = { showReceiptCapture = false },
                        onSaved = {
                            showReceiptCapture = false
                            viewModel.setCurrentTab(0)
                        }
                    )
                }
            }
        }
    }

    // Cửa sổ Chat Assistant mở lên khi nhấn vào Bong bóng AI
    if (showAiChatWindow) {
        ModalBottomSheet(
            onDismissRequest = { showAiChatWindow = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            dragHandle = null
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxSize().widthIn(max = 680.dp)) {
                    ChatAssistantScreen(
                        viewModel = viewModel,
                        onClose = { showAiChatWindow = false }
                    )
                }
            }
        }
    }

    // Cửa sổ Quản Lý Danh Mục mở từ Tab Cài Đặt
    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxSize().widthIn(max = 680.dp)) {
                    CategoryScreen(
                        viewModel = viewModel,
                        onAddCategory = {
                            editingCategory = null
                            showAddCategoryDialog = true
                        },
                        onEditCategory = { cat ->
                            editingCategory = cat
                            showAddCategoryDialog = true
                        }
                    )
                }
            }
        }
    }

    // Modal Sheet Thêm / Sửa Giao Dịch
    if (showAddTxSheet) {
        AddTransactionSheet(
            viewModel = viewModel,
            categories = categories,
            editingTransaction = editingTx,
            onDismiss = { showAddTxSheet = false },
            onSave = { tx ->
                if (tx.id == 0L) {
                    viewModel.addTransaction(tx) {
                        showAddTxSheet = false
                    }
                } else {
                    viewModel.updateTransaction(tx) {
                        showAddTxSheet = false
                    }
                }
            }
        )
    }

    // Dialog Sửa Ngân Sách Tổng Thể
    if (showOverallBudgetDialog) {
        EditBudgetDialog(
            title = "Cài Đặt Ngân Sách Tổng Cả Tháng",
            currentAmount = overallBudget.totalBudget,
            onDismiss = { showOverallBudgetDialog = false },
            onSave = { newBudget ->
                viewModel.updateOverallBudget(newBudget)
                showOverallBudgetDialog = false
            }
        )
    }

    // Dialog Sửa Ngân Sách Cho Từng Danh Mục
    editingCategoryBudget?.let { catBudget ->
        EditBudgetDialog(
            title = "Ngân Sách Cho ${catBudget.category.icon} ${catBudget.category.name}",
            currentAmount = catBudget.budgetAmount,
            onDismiss = { editingCategoryBudget = null },
            onSave = { newBudget ->
                viewModel.updateCategoryBudget(catBudget.category.id, newBudget)
                editingCategoryBudget = null
            }
        )
    }

    // Dialog Thêm / Sửa Danh Mục
    if (showAddCategoryDialog) {
        EditCategoryDialog(
            category = editingCategory,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { cat ->
                if (cat.id == 0L) {
                    viewModel.addCategory(cat) {
                        showAddCategoryDialog = false
                    }
                } else {
                    viewModel.updateCategory(cat) {
                        showAddCategoryDialog = false
                    }
                }
            }
        )
    }
}

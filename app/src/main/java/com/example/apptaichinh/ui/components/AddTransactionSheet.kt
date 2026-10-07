package com.example.apptaichinh.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptaichinh.data.model.Category
import com.example.apptaichinh.data.model.Transaction
import com.example.apptaichinh.theme.NeonCoral
import com.example.apptaichinh.theme.NeonEmerald
import com.example.apptaichinh.ui.viewmodel.FinanceViewModel
import com.example.apptaichinh.ui.viewmodel.ParsedTransaction

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    viewModel: FinanceViewModel,
    categories: List<Category>,
    editingTransaction: Transaction? = null,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 0 = Nhập nhanh thông minh (Smart NLP), 1 = Nhập chi tiết thủ công
    var selectedMode by remember { mutableStateOf(if (editingTransaction != null) 1 else 0) }

    // State thủ công
    var txType by remember { mutableStateOf(editingTransaction?.type ?: "EXPENSE") }
    var amountText by remember { mutableStateOf(editingTransaction?.let { it.amount.toString() } ?: "") }
    var note by remember { mutableStateOf(editingTransaction?.note ?: "") }

    val filteredCategories = categories.filter { it.type == txType }
    var selectedCategory by remember {
        mutableStateOf(
            categories.find { it.id == editingTransaction?.categoryId }
                ?: filteredCategories.firstOrNull()
        )
    }

    // State nhập nhanh
    var smartInputText by remember { mutableStateOf("") }
    var parsedTx by remember { mutableStateOf<ParsedTransaction?>(null) }
    var showAddCatDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFFF8FAFC),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFCBD5E1)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingTransaction == null) "Thêm Giao Dịch Mới" else "Chỉnh Sửa Giao Dịch",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Tabs (nếu thêm mới)
            if (editingTransaction == null) {
                TabRow(
                    selectedTabIndex = selectedMode,
                    containerColor = Color(0xFFE2E8F0),
                    contentColor = Color(0xFF2563EB),
                    divider = {},
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedMode == 0,
                        onClick = { selectedMode = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedMode == 0) Color(0xFF2563EB) else Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Nhập Nhanh",
                                    fontWeight = if (selectedMode == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedMode == 0) Color(0xFF1D4ED8) else Color(0xFF64748B)
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedMode == 1,
                        onClick = { selectedMode = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedMode == 1) Color(0xFF2563EB) else Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Nhập Chi Tiết",
                                    fontWeight = if (selectedMode == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedMode == 1) Color(0xFF1D4ED8) else Color(0xFF64748B)
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            if (selectedMode == 0 && editingTransaction == null) {
                // GIAO DIỆN NHẬP NHANH THÔNG MINH (SMART NLP)
                Text(
                    text = "Gõ câu chi tiêu bằng tiếng Việt tự nhiên:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = smartInputText,
                    onValueChange = {
                        smartInputText = it
                        parsedTx = viewModel.parseQuickText(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ví dụ: Ăn trưa 45k, Đổ xăng 80k, Tiền thưởng 2tr...", color = Color(0xFF94A3B8)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // PREVIEW CARD ĐỀ XUẤT
                val parsed = parsedTx
                if (parsed != null && parsed.isValid) {
                    val isIncome = parsed.type == "INCOME"
                    val cat = categories.find { it.id == parsed.categoryId } ?: categories.firstOrNull()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isIncome) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isIncome) Color(0xFF86EFAC) else Color(0xFFFECDD3)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isIncome) "THU NHẬP (+)" else "CHI TIÊU (-)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isIncome) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )

                                Text(
                                    text = Formatters.formatVnd(parsed.amount),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isIncome) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Danh mục: ", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "${cat?.icon ?: "📦"} ${parsed.categoryName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Ghi chú: ", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = parsed.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            TactileGradientButton(
                                text = "Xác Nhận & Lưu Vào Sổ",
                                icon = Icons.Default.Check,
                                gradientColors = if (isIncome) listOf(Color(0xFF10B981), Color(0xFF059669)) else listOf(Color(0xFFFF3366), Color(0xFFE11D48)),
                                glowColor = if (isIncome) NeonEmerald else NeonCoral,
                                modifier = Modifier.fillMaxWidth(),
                                height = 48.dp,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    val newTx = Transaction(
                                        id = 0L,
                                        amount = parsed.amount,
                                        type = parsed.type,
                                        categoryId = cat?.id ?: parsed.categoryId,
                                        note = parsed.note,
                                        dateEpoch = System.currentTimeMillis()
                                    )
                                    onSave(newTx)
                                }
                            )
                        }
                    }
                } else if (smartInputText.isNotBlank()) {
                    Text(
                        text = "💡 Hãy kèm số tiền (VD: 45k, 70 nghìn, 2tr, 1500000) để tự động trích xuất",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }

            } else {
                // GIAO DIỆN NHẬP THỦ CÔNG
                // Loại Chi tiêu / Thu nhập
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TactileTypeTab(
                        title = "Chi Tiêu (-)",
                        isSelected = txType == "EXPENSE",
                        activeGradient = listOf(Color(0xFFFF3366), Color(0xFFE11D48), Color(0xFFBE123C)),
                        glowColor = NeonCoral,
                        onClick = {
                            txType = "EXPENSE"
                            selectedCategory = categories.filter { it.type == "EXPENSE" }.firstOrNull()
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    TactileTypeTab(
                        title = "Thu Nhập (+)",
                        isSelected = txType == "INCOME",
                        activeGradient = listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF047857)),
                        glowColor = NeonEmerald,
                        onClick = {
                            txType = "INCOME"
                            selectedCategory = categories.filter { it.type == "INCOME" }.firstOrNull()
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ô nhập số tiền
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input -> amountText = input.filter { it.isDigit() } },
                    label = { Text("Số tiền (VNĐ)", color = Color(0xFF475569)) },
                    placeholder = { Text("Ví dụ: 50000", color = Color(0xFF94A3B8)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedLabelColor = Color(0xFF2563EB),
                        unfocusedLabelColor = Color(0xFF475569)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                val amtVal = amountText.toLongOrNull() ?: 0L
                if (amtVal > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Số tiền: ${Formatters.formatVnd(amtVal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (txType == "EXPENSE") Color(0xFFDC2626) else Color(0xFF16A34A)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lưới chọn danh mục
                Text(
                    text = "Chọn danh mục:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredCategories.forEach { cat ->
                        val isSelected = selectedCategory?.id == cat.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = cat
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            },
                            label = { 
                                Text(
                                    text = "${cat.icon} ${cat.name}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = Color(0xFF1E293B),
                                selectedContainerColor = Color(0xFFDBEAFE),
                                selectedLabelColor = Color(0xFF1D4ED8)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0xFFCBD5E1),
                                selectedBorderColor = Color(0xFF3B82F6),
                                borderWidth = if (isSelected) 1.5.dp else 1.dp
                            )
                        )
                    }

                    // Nút thêm nhanh danh mục mới
                    FilterChip(
                        selected = false,
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            showAddCatDialog = true
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF2563EB)
                            )
                        },
                        label = { Text("Thêm mới", fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB)) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFFF1F5F9)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = Color(0xFF93C5FD),
                            borderWidth = 1.dp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ghi chú
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú (tùy chọn)", color = Color(0xFF475569)) },
                    placeholder = { Text("Ví dụ: Ăn trưa cùng bạn, Đổ xăng...", color = Color(0xFF94A3B8)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedLabelColor = Color(0xFF2563EB),
                        unfocusedLabelColor = Color(0xFF475569)
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Nút Lưu
                val canSaveManual = (amountText.toLongOrNull() ?: 0L) > 0 && selectedCategory != null
                val manualGradient = if (txType == "EXPENSE") {
                    listOf(Color(0xFFFF3366), Color(0xFFE11D48), Color(0xFFBE123C))
                } else {
                    listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF047857))
                }
                val manualGlow = if (txType == "EXPENSE") NeonCoral else NeonEmerald

                TactileGradientButton(
                    text = if (editingTransaction == null) "Lưu Giao Dịch" else "Cập Nhật Giao Dịch",
                    icon = Icons.Default.Check,
                    gradientColors = manualGradient,
                    glowColor = manualGlow,
                    enabled = canSaveManual,
                    modifier = Modifier.fillMaxWidth(),
                    height = 52.dp,
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        val amount = amountText.toLongOrNull() ?: 0L
                        val catId = selectedCategory?.id ?: 0L
                        if (amount > 0 && catId > 0) {
                            val newTx = Transaction(
                                id = editingTransaction?.id ?: 0L,
                                amount = amount,
                                type = txType,
                                categoryId = catId,
                                note = note.trim(),
                                dateEpoch = editingTransaction?.dateEpoch ?: System.currentTimeMillis()
                            )
                            onSave(newTx)
                        }
                    }
                )
            }
        }
    }

    // Dialog thêm danh mục nhanh ngay trong sheet
    if (showAddCatDialog) {
        EditCategoryDialog(
            category = null,
            initialType = txType,
            onDismiss = { showAddCatDialog = false },
            onSave = { newCat ->
                viewModel.addCategory(newCat) { created ->
                    selectedCategory = created
                    showAddCatDialog = false
                }
            }
        )
    }
}

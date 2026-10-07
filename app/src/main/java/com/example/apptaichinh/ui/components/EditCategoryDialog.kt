package com.example.apptaichinh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptaichinh.data.model.Category

@Composable
fun EditCategoryDialog(
    category: Category?,
    initialType: String = "EXPENSE",
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var type by remember { mutableStateOf(category?.type ?: initialType) }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: (if (type == "INCOME") "💵" else "🍜")) }
    var selectedColor by remember { mutableStateOf(category?.colorHex ?: (if (type == "INCOME") "#10B981" else "#EF4444")) }
    var budgetText by remember { mutableStateOf(if ((category?.budget ?: 0L) > 0) category?.budget.toString() else "") }

    val iconOptions = listOf(
        "🍜", "🏠", "🛵", "🛍️", "🎮", "📚", "💊", "💌", "🏦", "💳",
        "💵", "🎁", "💼", "📈", "💰", "☕", "🍔", "✈️", "🏋️", "💻",
        "👶", "🐶", "👗", "🧾", "⛽", "📦"
    )

    val colorOptions = listOf(
        "#EF4444", "#F97316", "#F59E0B", "#10B981", "#06B6D4",
        "#3B82F6", "#8B5CF6", "#EC4899", "#F43F5E", "#64748B",
        "#009688", "#795548", "#9C27B0", "#673AB7"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        titleContentColor = Color(0xFF0F172A),
        textContentColor = Color(0xFF1E293B),
        title = {
            Text(
                text = if (category == null) "Thêm Danh Mục Mới" else "Chỉnh Sửa Danh Mục",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Chọn loại Thu / Chi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = {
                            type = "EXPENSE"
                            if (category == null && selectedIcon == "💵") selectedIcon = "🍜"
                        },
                        label = { Text("Chi tiêu", fontWeight = if (type == "EXPENSE") FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFFF1F5F9),
                            labelColor = Color(0xFF334155),
                            selectedContainerColor = Color(0xFFFEE2E2),
                            selectedLabelColor = Color(0xFFDC2626)
                        )
                    )
                    FilterChip(
                        selected = type == "INCOME",
                        onClick = {
                            type = "INCOME"
                            if (category == null && selectedIcon == "🍜") selectedIcon = "💵"
                        },
                        label = { Text("Thu nhập", fontWeight = if (type == "INCOME") FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFFF1F5F9),
                            labelColor = Color(0xFF334155),
                            selectedContainerColor = Color(0xFFDCFCE7),
                            selectedLabelColor = Color(0xFF16A34A)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tên danh mục
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên danh mục", color = Color(0xFF475569)) },
                    placeholder = { Text(if (type == "INCOME") "Ví dụ: Lương, Thưởng, Cổ tức..." else "Ví dụ: Ăn uống, Nhà ở, Đi lại...", color = Color(0xFF94A3B8)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedLabelColor = Color(0xFF2563EB),
                        unfocusedLabelColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Chọn Biểu tượng (Icon Emoji)
                Text(
                    text = "Biểu tượng:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(iconOptions) { icon ->
                        val isSelected = icon == selectedIcon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Chọn Mã màu
                Text(
                    text = "Màu đại diện:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colorOptions) { hex ->
                        val color = Formatters.parseColor(hex)
                        val isSelected = hex == selectedColor
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) Color(0xFF0F172A) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }

                // Nếu là danh mục Chi tiêu -> Cho phép cài đặt Hạn mức ngân sách
                if (type == "EXPENSE") {
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { input -> budgetText = input.filter { it.isDigit() } },
                        label = { Text("Hạn mức ngân sách tháng (VNĐ)", color = Color(0xFF475569)) },
                        placeholder = { Text("Ví dụ: 2000000 (Để trống nếu không đặt)", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedLabelColor = Color(0xFF2563EB),
                            unfocusedLabelColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val budget = budgetText.toLongOrNull() ?: 0L
                        val newCategory = Category(
                            id = category?.id ?: 0L,
                            name = name.trim(),
                            type = type,
                            icon = selectedIcon,
                            colorHex = selectedColor,
                            budget = if (type == "EXPENSE") budget else 0L
                        )
                        onSave(newCategory)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                ),
                enabled = name.isNotBlank()
            ) {
                Text("Lưu Danh Mục", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Hủy", color = Color(0xFF64748B))
            }
        }
    )
}

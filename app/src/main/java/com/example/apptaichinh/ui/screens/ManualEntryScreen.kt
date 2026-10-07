package com.example.apptaichinh.ui.screens

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptaichinh.data.model.Category
import com.example.apptaichinh.data.model.Transaction
import com.example.apptaichinh.theme.ActiveNeonBorderBrush
import com.example.apptaichinh.theme.CarbonSurface
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
import com.example.apptaichinh.ui.components.AiLogoSize
import com.example.apptaichinh.ui.components.AiLogoView
import com.example.apptaichinh.ui.components.EditCategoryDialog
import com.example.apptaichinh.ui.components.Formatters
import com.example.apptaichinh.ui.components.Glyph3DIcon
import com.example.apptaichinh.ui.components.NeumorphicGlassCard
import com.example.apptaichinh.ui.components.TactileGradientButton
import com.example.apptaichinh.ui.components.TactileKeyButton
import com.example.apptaichinh.ui.components.TactilePillChip
import com.example.apptaichinh.ui.components.TactileQuickChip
import com.example.apptaichinh.ui.components.TactileTypeTab
import com.example.apptaichinh.ui.components.tactileBounceClickable
import com.example.apptaichinh.ui.viewmodel.FinanceViewModel
import com.example.apptaichinh.ui.viewmodel.ParsedTransaction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Màn Hình Nhập Giao Dịch Skeuomorphic-Neumorphic Hiện Đại (Panel 4)
 * - Tabs lớn xúc giác rõ ràng cho "Chi Tiêu (-)" vs "Thu Nhập (+)".
 * - Bảng điều khiển số tiền kỹ thuật số (Recessed Digital Amount Console).
 * - Bàn phím số xúc giác cao cấp (Tactile Skeuomorphic Numpad) với viền phím lấp lánh nhẹ.
 * - Lưới thẻ danh mục rực rỡ bão hòa cao với biểu tượng 3D tinh xảo.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManualEntryScreen(
    viewModel: FinanceViewModel,
    onNavigateToCalendar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val categories by viewModel.categories.collectAsState()

    // 0: Nhập thủ công xúc giác (Tactile Numpad & Grid), 1: Nhập nhanh văn bản tự nhiên (Smart NLP)
    var inputMode by remember { mutableStateOf(0) }

    // State loại giao dịch: EXPENSE hoặc INCOME
    var txType by remember { mutableStateOf("EXPENSE") }

    // State ngày chọn
    val selectedCalendar = remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDateEpoch by remember { mutableStateOf(System.currentTimeMillis()) }

    // State số tiền & ghi chú
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // Hiển thị bàn phím Numpad xúc giác hay không
    var showNumpad by remember { mutableStateOf(true) }

    // Danh mục theo loại
    val filteredCategories = categories.filter { it.type == txType }
    var selectedCategory by remember(txType, categories) {
        mutableStateOf(filteredCategories.firstOrNull())
    }

    var showAddCatDialog by remember { mutableStateOf(false) }

    // State Smart NLP
    var smartInputText by remember { mutableStateOf("") }
    var parsedTx by remember { mutableStateOf<ParsedTransaction?>(null) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                smartInputText = spokenText
                parsedTx = viewModel.parseQuickText(spokenText)
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi-VN")
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "vi-VN")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói câu chi tiêu (VD: Ăn phở 45k, Đổ xăng 80k)...")
                }
                com.example.apptaichinh.utils.SoundUtils.playMicOpenSound()
                speechLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Thiết bị chưa cài đặt nhận diện giọng nói Google", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Cần cấp quyền ghi âm để sử dụng chức năng giọng nói", Toast.LENGTH_SHORT).show()
        }
    }

    fun startVoiceSmartInput() {
        val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi-VN")
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "vi-VN")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói câu chi tiêu (VD: Ăn phở 45k, Đổ xăng 80k)...")
                }
                com.example.apptaichinh.utils.SoundUtils.playMicOpenSound()
                speechLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Thiết bị chưa cài đặt nhận diện giọng nói Google", Toast.LENGTH_LONG).show()
            }
        } else {
            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    fun openDatePicker() {
        val cal = selectedCalendar.value
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            val newCal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 12, 0, 0)
            }
            selectedCalendar.value = newCal
            selectedDateEpoch = newCal.timeInMillis
        }, y, m, d).show()
    }

    val dateFormat = remember { SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi", "VN")) }
    val formattedSelectedDate = dateFormat.format(Date(selectedDateEpoch)).replaceFirstChar { it.uppercase() }
    val amountLong = amountText.toLongOrNull() ?: 0L

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
                .padding(bottom = 95.dp, top = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. TIÊU ĐỀ & CHUYỂN CHẾ ĐỘ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ghi Chép Giao Dịch",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast
                    )
                    Text(
                        text = "Nhập nhanh thu chi hằng ngày vào sổ",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumContrast
                    )
                }

                // Switcher Pill: Bàn phím vs Trợ lý AI
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (inputMode == 0) Color.White else Color.Transparent)
                            .clickable { inputMode = 0 }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Chi tiết",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (inputMode == 0) NeonAzure else TextMuted
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (inputMode == 1) Color.White else Color.Transparent)
                            .clickable { inputMode = 1 }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AiLogoView(size = 14.dp, withGlow = false)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Gõ tắt",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (inputMode == 1) NeonAzure else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (inputMode == 0) {
                // ==========================================
                // CHẾ ĐỘ NHẬP THỦ CÔNG XÚC GIÁC (TACTILE NUMPAD)
                // ==========================================

                // 2. TABS LỚN RÕ RÀNG VỚI PHẢN ỨNG CHẠM XÚC GIÁC: CHI TIÊU (-) VS THU NHẬP (+)
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
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. THẺ NGÀY GIAO DỊCH DẠNG KÍNH
                NeumorphicGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openDatePicker() },
                    shape = RoundedCornerShape(18.dp),
                    elevation = 3.dp,
                    containerColor = CarbonSurfaceGlass.copy(alpha = 0.85f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Glyph3DIcon(icon = "📅", accentColor = NeonAzure, size = 36.dp, fontSize = 16f)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ngày giao dịch",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMediumContrast
                                )
                                Text(
                                    text = formattedSelectedDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHighContrast
                                )
                            }
                        }

                        TactilePillChip(text = "Đổi ngày", accentColor = NeonAzure, leadingDot = false)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. BẢNG ĐIỀU KHIỂN SỐ TIỀN KỸ THUẬT SỐ (Recessed Digital Amount Console)
                NeumorphicGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    elevation = 6.dp,
                    containerColor = CarbonSurfaceGlass.copy(alpha = 0.88f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Số tiền giao dịch:",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMediumContrast
                            )

                            TactilePillChip(
                                text = "VNĐ",
                                accentColor = if (txType == "EXPENSE") NeonCoral else NeonEmerald,
                                leadingDot = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Màn hình hiển thị số lõm xuống (Recessed Display)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CarbonSurfaceRecessed)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                .clickable { showNumpad = !showNumpad }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (txType == "EXPENSE") "Chi tiêu" else "Thu vào",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = if (amountLong > 0) {
                                        "${if (txType == "EXPENSE") "-" else "+"}${Formatters.formatVnd(amountLong)}"
                                    } else {
                                        "0 đ"
                                    },
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (amountLong == 0L) TextMuted else if (txType == "EXPENSE") NeonCoral else NeonEmerald,
                                    letterSpacing = (-0.5).sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Các phím gợi ý cộng nhanh số tiền dạng viên thuốc xúc giác
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(10_000L, 20_000L, 50_000L, 100_000L, 200_000L, 500_000L, 1_000_000L).forEach { addVal ->
                                TactileQuickChip(
                                    text = "+${Formatters.formatCompactVnd(addVal)}",
                                    onClick = {
                                        val cur = amountText.toLongOrNull() ?: 0L
                                        amountText = (cur + addVal).toString()
                                    }
                                )
                            }

                            if (amountLong > 0) {
                                TactileQuickChip(
                                    text = "Xóa hết",
                                    isDanger = true,
                                    icon = Icons.Default.Clear,
                                    onClick = { amountText = "" }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. BÀN PHÍM SỐ XÚC GIÁC CAO CẤP (Tactile Skeuomorphic Numpad)
                if (showNumpad) {
                    NeumorphicGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        elevation = 6.dp,
                        containerColor = CarbonSurfaceGlass.copy(alpha = 0.88f),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val keys = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("C", "0", "⌫")
                            )

                            keys.forEach { rowKeys ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowKeys.forEach { keyLabel ->
                                        TactileKeyButton(
                                            label = keyLabel,
                                            modifier = Modifier.weight(1f),
                                            isActionKey = keyLabel == "⌫",
                                            isDangerKey = keyLabel == "C",
                                            onClick = {
                                                when (keyLabel) {
                                                    "C" -> amountText = ""
                                                    "⌫" -> {
                                                        if (amountText.isNotEmpty()) {
                                                            amountText = amountText.dropLast(1)
                                                        }
                                                    }
                                                    else -> {
                                                        if (amountText.length < 12) {
                                                            amountText += keyLabel
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 6. LƯỚI THẺ DANH MỤC RỰC RỠ VỚI BIỂU TƯỢNG 3D (Vibrant Category Grid)
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
                                text = "Chọn Danh Mục ${if (txType == "EXPENSE") "Chi Tiêu" else "Thu Nhập"}:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextHighContrast
                            )

                            TactilePillChip(
                                text = "${filteredCategories.size} nhóm",
                                accentColor = NeonAzure,
                                leadingDot = false
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Lưới 4 cột các thẻ danh mục
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredCategories.forEach { cat ->
                                val isSelected = selectedCategory?.id == cat.id
                                val catColor = Formatters.parseColor(cat.colorHex)

                                val scale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.04f else 1f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                    label = "catTileScale"
                                )

                                Box(
                                    modifier = Modifier
                                        .width(76.dp)
                                        .scale(scale)
                                        .shadow(
                                            elevation = if (isSelected) 4.dp else 1.dp,
                                            shape = RoundedCornerShape(16.dp),
                                            ambientColor = if (isSelected) catColor.copy(alpha = 0.35f) else Color(0xFF64748B).copy(alpha = 0.10f)
                                        )
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSelected) {
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        catColor.copy(alpha = 0.14f),
                                                        Color.White
                                                    )
                                                )
                                            } else {
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.White,
                                                        Color(0xFFF8FAFC)
                                                    )
                                                )
                                            }
                                        )
                                        .border(
                                            width = if (isSelected) 1.6.dp else 1.dp,
                                            brush = if (isSelected) {
                                                Brush.linearGradient(listOf(catColor, catColor.copy(alpha = 0.6f)))
                                            } else {
                                                Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
                                            },
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .tactileBounceClickable {
                                            selectedCategory = cat
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Glyph3DIcon(
                                            icon = cat.icon,
                                            accentColor = catColor,
                                            size = 38.dp,
                                            fontSize = 18f
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = cat.name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) TextHighContrast else TextMediumContrast,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Nút thêm danh mục mới
                            Box(
                                modifier = Modifier
                                    .width(76.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                    .tactileBounceClickable { showAddCatDialog = true }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(NeonAzure.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Thêm danh mục", tint = NeonAzure, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "+ Thêm",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NeonAzure
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 7. Ô NHẬP GHI CHÚ DẠNG KÍNH
                NeumorphicGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = 3.dp,
                    containerColor = CarbonSurfaceGlass.copy(alpha = 0.85f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Ghi chú bổ sung (tùy chọn):",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMediumContrast
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = { Text("Ví dụ: Ăn trưa bún bò, Đổ xăng xe máy, Tiền thưởng...", color = TextMuted) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonAzure,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedTextColor = TextHighContrast,
                                unfocusedTextColor = TextHighContrast
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 8. NÚT LƯU GIAO DỊCH XÚC GIÁC (Tactile Save Button)
                val canSave = amountLong > 0 && selectedCategory != null
                val saveGradient = if (txType == "EXPENSE") {
                    listOf(Color(0xFFFF3366), Color(0xFFE11D48), Color(0xFFBE123C))
                } else {
                    listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF047857))
                }
                val saveGlow = if (txType == "EXPENSE") NeonCoral else NeonEmerald

                TactileGradientButton(
                    text = "Lưu Giao Dịch Vào Sổ",
                    icon = Icons.Default.CheckCircle,
                    gradientColors = saveGradient,
                    glowColor = saveGlow,
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth(),
                    height = 56.dp,
                    onClick = {
                        val cat = selectedCategory ?: return@TactileGradientButton
                        val newTx = Transaction(
                            id = 0L,
                            amount = amountLong,
                            type = txType,
                            categoryId = cat.id,
                            categoryName = cat.name,
                            categoryIcon = cat.icon,
                            categoryColorHex = cat.colorHex,
                            note = note.trim(),
                            dateEpoch = selectedDateEpoch
                        )

                        viewModel.addTransaction(newTx) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Đã lưu vào sổ: ${cat.icon} ${cat.name} (${Formatters.formatVnd(amountLong)})",
                                    duration = SnackbarDuration.Short
                                )
                            }
                            amountText = ""
                            note = ""
                        }
                    }
                )

            } else {
                // ==========================================
                // CHẾ ĐỘ NHẬP NHANH THÔNG MINH (SMART NLP)
                // ==========================================
                NeumorphicGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    elevation = 6.dp,
                    containerColor = CarbonSurfaceGlass.copy(alpha = 0.88f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AiLogoView(size = 32.dp, withGlow = true)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Trợ Lý Trích Xuất Giao Dịch",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHighContrast
                                )
                                Text(
                                    text = "Gõ hoặc nói câu tiếng Việt tự nhiên",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMediumContrast
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = smartInputText,
                            onValueChange = {
                                smartInputText = it
                                parsedTx = viewModel.parseQuickText(it)
                            },
                            placeholder = { Text("Ví dụ: Ăn trưa 45k, Đổ xăng 80k, Tiền thưởng 2tr...", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            maxLines = 3,
                            trailingIcon = {
                                IconButton(onClick = { startVoiceSmartInput() }) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Nói bằng giọng nói",
                                        tint = NeonAzure
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonAzure,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedTextColor = TextHighContrast,
                                unfocusedTextColor = TextHighContrast
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val parsed = parsedTx
                        if (parsed != null && parsed.isValid) {
                            val isIncome = parsed.type == "INCOME"
                            val cat = categories.find { it.id == parsed.categoryId } ?: categories.firstOrNull()

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, if (isIncome) NeonEmerald.copy(alpha = 0.4f) else NeonCoral.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TactilePillChip(
                                            text = if (isIncome) "THU NHẬP (+)" else "CHI TIÊU (-)",
                                            accentColor = if (isIncome) NeonEmerald else NeonCoral,
                                            leadingDot = true
                                        )
                                        Text(
                                            text = Formatters.formatVnd(parsed.amount),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isIncome) NeonEmerald else NeonCoral
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Danh mục: ${cat?.icon ?: "📦"} ${parsed.categoryName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextHighContrast
                                    )
                                    Text(
                                        text = "Ghi chú: ${parsed.note}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMediumContrast
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

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
                                            viewModel.addTransaction(newTx) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Đã lưu vào sổ: ${parsed.categoryName} (${Formatters.formatVnd(parsed.amount)})"
                                                    )
                                                }
                                                smartInputText = ""
                                                parsedTx = null
                                            }
                                        }
                                    )
                                }
                            }
                        } else if (smartInputText.isNotBlank()) {
                            Text(
                                text = "💡 Hãy kèm số tiền (VD: 45k, 70 nghìn, 2tr) để tự động trích xuất",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeonAmber
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Phím tắt chuyển sang xem Lịch dạng kính nổi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, SpecularBorderBrush, RoundedCornerShape(14.dp))
                    .tactileBounceClickable { onNavigateToCalendar() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = NeonAzure,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Xem Sổ Thu Chi Dạng Lịch",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextHighContrast
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
        )
    }

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

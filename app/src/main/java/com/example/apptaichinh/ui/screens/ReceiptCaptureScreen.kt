package com.example.apptaichinh.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.apptaichinh.data.ai.ReceiptDraftItem
import com.example.apptaichinh.data.model.Category
import com.example.apptaichinh.ui.components.Formatters
import com.example.apptaichinh.ui.viewmodel.FinanceViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptCaptureScreen(
    viewModel: FinanceViewModel,
    onClose: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val categories by viewModel.categories.collectAsState()
    val receiptItems by viewModel.receiptItems.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingReceipt.collectAsState()
    val error by viewModel.receiptError.collectAsState()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var preparedImage by remember { mutableStateOf<ByteArray?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && imageUri != null) {
            scope.launch {
                val capturedUri = imageUri ?: return@launch
                val result = prepareReceiptImage(context, capturedUri)
                previewBitmap = result.first
                preparedImage = result.second
                viewModel.clearReceipt()
            }
        }
    }

    fun openCamera() {
        val file = File.createTempFile("receipt_", ".jpg", context.cacheDir)
        val targetUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        imageUri = targetUri
        cameraLauncher.launch(targetUri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chụp hóa đơn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearReceipt()
                        onClose()
                    }) { Icon(Icons.Default.Close, contentDescription = "Đóng") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp, 16.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (previewBitmap == null) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 42.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(52.dp))
                            Text("Đặt hóa đơn phẳng, đủ sáng và chụp trọn bốn góc.")
                            Button(onClick = { openCamera() }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(Modifier.size(8.dp))
                                Text("Mở camera")
                            }
                        }
                    } else {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Image(
                                bitmap = previewBitmap!!.asImageBitmap(),
                                contentDescription = "Ảnh hóa đơn",
                                modifier = Modifier.fillMaxWidth().height(260.dp),
                                contentScale = ContentScale.Fit
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FilledTonalButton(onClick = { openCamera() }, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                                    Spacer(Modifier.size(6.dp))
                                    Text("Chụp lại")
                                }
                                Button(
                                    onClick = { preparedImage?.let(viewModel::analyzeReceipt) },
                                    enabled = preparedImage != null && !isAnalyzing,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isAnalyzing) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    else Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(Modifier.size(6.dp))
                                    Text(if (isAnalyzing) "Đang đọc" else "AI đọc hóa đơn")
                                }
                            }
                        }
                    }
                }
            }

            error?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (receiptItems.isNotEmpty()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dữ liệu AI nhận diện", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Kiểm tra và chỉnh sửa trước khi lưu", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            Formatters.formatVnd(receiptItems.sumOf { it.amount }),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(receiptItems, key = { it.id }) { item ->
                    ReceiptItemEditor(
                        item = item,
                        categories = categories.filter { it.type == "EXPENSE" },
                        onChange = viewModel::updateReceiptItem,
                        onDelete = { viewModel.removeReceiptItem(item.id) }
                    )
                }

                item {
                    FilledTonalButton(onClick = viewModel::addEmptyReceiptItem, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Thêm dòng")
                    }
                }
                item {
                    Button(
                        onClick = { viewModel.saveReceiptItems(onSaved) },
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Lưu ${receiptItems.size} khoản", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptItemEditor(
    item: ReceiptDraftItem,
    categories: List<Category>,
    onChange: (ReceiptDraftItem) -> Unit,
    onDelete: () -> Unit
) {
    var categoryExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val deleteInteraction = remember { MutableInteractionSource() }
    val deletePressed by deleteInteraction.collectIsPressedAsState()
    val deleteScale by animateFloatAsState(
        targetValue = if (deletePressed) 0.82f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "receiptDeleteScale"
    )
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dòng hóa đơn", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                IconButton(
                    onClick = {
                        onDelete()
                        Toast.makeText(
                            context,
                            "Đã xóa ${item.description.ifBlank { "dòng hóa đơn" }}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    interactionSource = deleteInteraction,
                    modifier = Modifier.scale(deleteScale)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa dòng",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            OutlinedTextField(
                value = item.description,
                onValueChange = { onChange(item.copy(description = it)) },
                label = { Text("Mặt hàng / nội dung") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = item.quantity.toString(),
                    onValueChange = { value -> onChange(item.copy(quantity = value.filter(Char::isDigit).toIntOrNull() ?: 1)) },
                    label = { Text("SL") },
                    modifier = Modifier.weight(0.35f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = if (item.amount == 0L) "" else item.amount.toString(),
                    onValueChange = { value -> onChange(item.copy(amount = value.filter(Char::isDigit).toLongOrNull() ?: 0L)) },
                    label = { Text("Thành tiền (đ)") },
                    modifier = Modifier.weight(0.65f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                OutlinedTextField(
                    value = item.categoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Danh mục") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text("${category.icon} ${category.name}") },
                            onClick = {
                                onChange(item.copy(categoryId = category.id, categoryName = category.name))
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

private suspend fun prepareReceiptImage(context: Context, uri: Uri): Pair<Bitmap, ByteArray> = withContext(Dispatchers.IO) {
    val source = context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) }
        ?: error("Không thể đọc ảnh vừa chụp.")
    val maxSide = 1600
    val scale = minOf(1f, maxSide.toFloat() / maxOf(source.width, source.height).toFloat())
    val resized = if (scale < 1f) {
        Bitmap.createScaledBitmap(source, (source.width * scale).toInt(), (source.height * scale).toInt(), true)
            .also { if (it !== source) source.recycle() }
    } else source
    val output = ByteArrayOutputStream()
    resized.compress(Bitmap.CompressFormat.JPEG, 82, output)
    resized to output.toByteArray()
}

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apptaichinh.data.model.Transaction
import com.example.apptaichinh.theme.CarbonSurfaceGlass
import com.example.apptaichinh.theme.NeonCoral
import com.example.apptaichinh.theme.NeonEmerald
import com.example.apptaichinh.theme.SpecularBorderBrush
import com.example.apptaichinh.theme.TextHighContrast
import com.example.apptaichinh.theme.TextMediumContrast
import com.example.apptaichinh.theme.TextMuted

/**
 * Mục Giao Dịch Dạng Kính Nổi Xúc Giác (Neumorphic Glass Transaction Item)
 */
@Composable
fun TransactionItemView(
    transaction: Transaction,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == "INCOME"
    val amountColor = if (isIncome) NeonEmerald else NeonCoral
    val catColor = Formatters.parseColor(transaction.categoryColorHex)

    NeumorphicGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = 3.dp,
        containerColor = CarbonSurfaceGlass.copy(alpha = 0.85f),
        contentPadding = PaddingValues(13.dp)
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
                // Biểu tượng 3D Glyph của danh mục
                Glyph3DIcon(
                    icon = transaction.categoryIcon,
                    accentColor = catColor,
                    size = 44.dp,
                    fontSize = 20f
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (transaction.note.isNotBlank()) transaction.note else transaction.categoryName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${transaction.categoryName} • ${Formatters.formatDate(transaction.dateEpoch)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumContrast,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (isIncome) "+" else "-"}${Formatters.formatVnd(transaction.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Xóa giao dịch",
                        tint = TextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

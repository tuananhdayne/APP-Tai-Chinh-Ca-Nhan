package com.example.apptaichinh.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import com.example.apptaichinh.MainActivity
import com.example.apptaichinh.R
import com.example.apptaichinh.data.db.FinanceDatabaseHelper
import com.example.apptaichinh.ui.components.Formatters
import java.util.Calendar

/**
 * AppWidgetProvider cho Tiện ích Sổ Thu Chi ngoài màn hình chính Android:
 * - Hiển thị Số dư ròng của tháng hiện tại (màu xanh nếu dương, màu đỏ nếu âm).
 * - Hiển thị Tổng Thu (+) và Tổng Chi (-).
 * - Hiển thị Hạn mức ngân sách còn lại.
 * - Hiển thị Giao dịch gần nhất vừa phát sinh.
 * - Nút [+ Ghi Chép] mở thẳng vào Tab 0 (Nhập vào) trong 1 chạm.
 * - Nút [⟳] làm mới số liệu tức thời từ SQLite cục bộ.
 */
class FinanceAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            updateAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.apptaichinh.widget.ACTION_REFRESH_WIDGET"
        const val EXTRA_TARGET_TAB = "EXTRA_TAB"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, FinanceAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                for (id in appWidgetIds) {
                    updateWidget(context, appWidgetManager, id)
                }
            }
        }

        /**
         * Ghim widget ra màn hình chính với 1 chạm (hỗ trợ Android 8.0+)
         */
        fun requestPinWidget(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
                val myProvider = ComponentName(context, FinanceAppWidgetProvider::class.java)
                if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                    return appWidgetManager.requestPinAppWidget(myProvider, null, null)
                }
            }
            return false
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_finance)

            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month0 = cal.get(Calendar.MONTH) // 0-indexed (0..11) cho FinanceDatabaseHelper
            val monthDisplay = month0 + 1 // 1-indexed (1..12) để hiển thị

            val dbHelper = FinanceDatabaseHelper.getInstance(context)
            val summary = dbHelper.getMonthSummary(year, month0)
            val overallBudget = dbHelper.getOverallBudget(year, month0)
            val recentTxs = dbHelper.getTransactionsByMonth(year, month0)
            val latestTx = recentTxs.firstOrNull()

            views.setTextViewText(R.id.tv_widget_month, "T$monthDisplay/$year")

            val balanceSign = if (summary.balance >= 0) "+ " else "- "
            val balanceStr = balanceSign + Formatters.formatVnd(Math.abs(summary.balance))
            views.setTextViewText(R.id.tv_widget_balance, balanceStr)
            views.setTextColor(
                R.id.tv_widget_balance,
                if (summary.balance >= 0) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
            )

            views.setTextViewText(R.id.tv_widget_income, "Thu: +" + Formatters.formatVnd(summary.totalIncome))
            views.setTextViewText(R.id.tv_widget_expense, "Chi: -" + Formatters.formatVnd(summary.totalExpense))

            // Hạn mức ngân sách
            if (overallBudget.totalBudget > 0) {
                val budgetText = if (overallBudget.isOverBudget) {
                    "Vượt: " + Formatters.formatVnd(overallBudget.totalExpense - overallBudget.totalBudget)
                } else {
                    "Còn: " + Formatters.formatVnd(overallBudget.remaining)
                }
                views.setTextViewText(R.id.tv_widget_budget_badge, budgetText)
                views.setTextColor(
                    R.id.tv_widget_budget_badge,
                    if (overallBudget.isOverBudget) Color.parseColor("#EF4444") else Color.parseColor("#38BDF8")
                )
            } else {
                views.setTextViewText(R.id.tv_widget_budget_badge, "Chưa đặt ngân sách")
                views.setTextColor(R.id.tv_widget_budget_badge, Color.parseColor("#71717A"))
            }

            // Giao dịch gần nhất
            if (latestTx != null) {
                val sign = if (latestTx.type == "INCOME") "+" else "-"
                val noteStr = if (latestTx.note.isNotBlank()) latestTx.note else latestTx.categoryName
                views.setTextViewText(
                    R.id.tv_widget_recent,
                    "⚡ Gần nhất: ${latestTx.categoryIcon} $noteStr ($sign${Formatters.formatVnd(latestTx.amount)})"
                )
            } else {
                views.setTextViewText(R.id.tv_widget_recent, "⚡ Chưa có giao dịch trong tháng $monthDisplay")
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            // 1. Chạm vào thân thẻ -> Mở app bình thường
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, flags)
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            // 2. Chạm vào nút [+ Ghi Chép] -> Mở app thẳng vào Tab 0 (ManualEntryScreen)
            val quickAddIntent = Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_TARGET_TAB, 0)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val quickAddPendingIntent = PendingIntent.getActivity(context, 1, quickAddIntent, flags)
            views.setOnClickPendingIntent(R.id.btn_widget_quick_add, quickAddPendingIntent)

            // 3. Chạm vào nút [⟳] -> Gửi Broadcast làm mới widget
            val refreshIntent = Intent(context, FinanceAppWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(context, 2, refreshIntent, flags)
            views.setOnClickPendingIntent(R.id.btn_widget_refresh, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

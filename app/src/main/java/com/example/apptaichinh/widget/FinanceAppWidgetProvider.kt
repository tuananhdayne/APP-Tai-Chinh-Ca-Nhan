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
 * - Nút [+ Ghi Chép] mở thẳng vào Tab 1 (Nhập nhanh) trong 1 chạm.
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

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_finance)

            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1

            val dbHelper = FinanceDatabaseHelper.getInstance(context)
            val summary = dbHelper.getMonthSummary(year, month)

            views.setTextViewText(R.id.tv_widget_month, "T$month/$year")

            val balanceSign = if (summary.balance >= 0) "+ " else "- "
            val balanceStr = balanceSign + Formatters.formatVnd(Math.abs(summary.balance))
            views.setTextViewText(R.id.tv_widget_balance, balanceStr)
            views.setTextColor(
                R.id.tv_widget_balance,
                if (summary.balance >= 0) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
            )

            views.setTextViewText(R.id.tv_widget_income, "Thu: +" + Formatters.formatVnd(summary.totalIncome))
            views.setTextViewText(R.id.tv_widget_expense, "Chi: -" + Formatters.formatVnd(summary.totalExpense))

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

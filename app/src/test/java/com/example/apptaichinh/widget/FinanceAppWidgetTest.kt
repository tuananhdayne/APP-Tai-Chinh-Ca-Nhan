package com.example.apptaichinh.widget

import com.example.apptaichinh.ui.components.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanceAppWidgetTest {

    @Test
    fun testWidgetActionConstants() {
        assertEquals("com.example.apptaichinh.widget.ACTION_REFRESH_WIDGET", FinanceAppWidgetProvider.ACTION_REFRESH_WIDGET)
        assertEquals("EXTRA_TAB", FinanceAppWidgetProvider.EXTRA_TARGET_TAB)
    }

    @Test
    fun testWidgetBalanceFormatting_Positive() {
        val balance = 10750000L
        val sign = if (balance >= 0) "+ " else "- "
        val formatted = sign + Formatters.formatVnd(Math.abs(balance))
        assertTrue(formatted.startsWith("+ "))
        assertTrue(formatted.contains("10.750.000"))
        assertTrue(formatted.endsWith("₫"))
    }

    @Test
    fun testWidgetBalanceFormatting_Negative() {
        val balance = -1500000L
        val sign = if (balance >= 0) "+ " else "- "
        val formatted = sign + Formatters.formatVnd(Math.abs(balance))
        assertTrue(formatted.startsWith("- "))
        assertTrue(formatted.contains("1.500.000"))
        assertTrue(formatted.endsWith("₫"))
    }

    @Test
    fun testWidgetIncomeExpenseFormatting() {
        val income = 15000000L
        val expense = 4250000L

        val incomeStr = "Thu: +" + Formatters.formatVnd(income)
        val expenseStr = "Chi: -" + Formatters.formatVnd(expense)

        assertEquals("Thu: +15.000.000 ₫", incomeStr)
        assertEquals("Chi: -4.250.000 ₫", expenseStr)
    }
}

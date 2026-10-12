package com.example.apptaichinh.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun testFormatCalendarAmount_Zero() {
        assertEquals("", Formatters.formatCalendarAmount(0L, isIncome = true))
        assertEquals("", Formatters.formatCalendarAmount(0L, isIncome = false))
    }

    @Test
    fun testFormatCalendarAmount_UnderOneThousand() {
        assertEquals("+500", Formatters.formatCalendarAmount(500L, isIncome = true))
        assertEquals("-500", Formatters.formatCalendarAmount(500L, isIncome = false))
    }

    @Test
    fun testFormatCalendarAmount_Thousands() {
        assertEquals("+50k", Formatters.formatCalendarAmount(50_000L, isIncome = true))
        assertEquals("-120k", Formatters.formatCalendarAmount(120_000L, isIncome = false))
        assertEquals("+500k", Formatters.formatCalendarAmount(500_000L, isIncome = true))
    }

    @Test
    fun testFormatCalendarAmount_Millions() {
        assertEquals("+1tr", Formatters.formatCalendarAmount(1_000_000L, isIncome = true))
        assertEquals("-1,5tr", Formatters.formatCalendarAmount(1_500_000L, isIncome = false))
        assertEquals("+19tr", Formatters.formatCalendarAmount(19_000_000L, isIncome = true))
        assertEquals("-5,8tr", Formatters.formatCalendarAmount(5_800_000L, isIncome = false))
    }

    @Test
    fun testFormatCalendarAmount_Billions() {
        assertEquals("+1B", Formatters.formatCalendarAmount(1_000_000_000L, isIncome = true))
        assertEquals("-2,5B", Formatters.formatCalendarAmount(2_500_000_000L, isIncome = false))
    }
}

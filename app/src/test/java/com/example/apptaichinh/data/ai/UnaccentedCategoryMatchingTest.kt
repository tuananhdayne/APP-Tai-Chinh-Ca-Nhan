package com.example.apptaichinh.data.ai

import com.example.apptaichinh.data.ai.tools.LocalToolExecutor
import com.example.apptaichinh.data.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kiểm thử toàn diện tính năng nhận diện danh mục tiếng Việt không dấu,
 * ưu tiên danh mục sẵn có và chỉ fallback về 'Khác' khi nội dung hoàn toàn xa lạ.
 */
class UnaccentedCategoryMatchingTest {

    private val defaultExpenseCategories = listOf(
        Category(1, "Ăn uống", "EXPENSE", "🍜", "#EF4444", 5000000L),
        Category(2, "Đi lại", "EXPENSE", "🛵", "#3B82F6", 2000000L),
        Category(3, "Nhà ở", "EXPENSE", "🏠", "#10B981", 4000000L),
        Category(4, "Mua sắm", "EXPENSE", "🛍️", "#F59E0B", 3000000L),
        Category(5, "Giải trí", "EXPENSE", "🎮", "#8B5CF6", 1500000L),
        Category(6, "Sức khỏe", "EXPENSE", "🩺", "#EC4899", 1000000L),
        Category(7, "Giáo dục", "EXPENSE", "📚", "#6366F1", 1000000L),
        Category(8, "Hiếu hỉ", "EXPENSE", "🎁", "#14B8A6", 1000000L),
        Category(9, "Tiết kiệm", "EXPENSE", "💰", "#F97316", 2000000L),
        Category(10, "Trả nợ", "EXPENSE", "💳", "#64748B", 1000000L),
        Category(11, "Khác", "EXPENSE", "📦", "#94A3B8", 1000000L)
    )

    private val defaultIncomeCategories = listOf(
        Category(12, "Lương", "INCOME", "💵", "#10B981", 0L),
        Category(13, "Thưởng", "INCOME", "🎁", "#F59E0B", 0L),
        Category(14, "Làm thêm", "INCOME", "💼", "#3B82F6", 0L),
        Category(15, "Đầu tư", "INCOME", "📈", "#8B5CF6", 0L),
        Category(16, "Khác", "INCOME", "📦", "#94A3B8", 0L)
    )

    @Test
    fun testRemoveDiacritics() {
        assertEquals("an pho", LocalToolExecutor.removeDiacritics("Ăn phở"))
        assertEquals("do xang", LocalToolExecutor.removeDiacritics("Đổ xăng"))
        assertEquals("tien nha", LocalToolExecutor.removeDiacritics("Tiền nhà"))
        assertEquals("kham rang", LocalToolExecutor.removeDiacritics("Khám răng"))
        assertEquals("di lai", LocalToolExecutor.removeDiacritics("Đi lại"))
        assertEquals("luong", LocalToolExecutor.removeDiacritics("Lương"))
    }

    @Test
    fun testAnPhoMapsToAnUongNotKhac() {
        // Kiểm tra trọng tâm: "an pho" và "an pho 40k" BẮT BUỘC về "Ăn uống"
        val (match1, isFallback1) = LocalToolExecutor.matchBestCategory("an pho", "EXPENSE", defaultExpenseCategories)
        assertNotNull(match1)
        assertEquals("Ăn uống", match1?.name)
        assertFalse("Không được coi là fallback", isFallback1)

        val (match2, isFallback2) = LocalToolExecutor.matchBestCategory("an pho 40k", "EXPENSE", defaultExpenseCategories)
        assertNotNull(match2)
        assertEquals("Ăn uống", match2?.name)
        assertFalse("Không được coi là fallback", isFallback2)
    }

    @Test
    fun testCommonFoodUnaccentedPhrases() {
        val testCases = listOf(
            "pho bo 45k",
            "com tam 35k",
            "bun dau 50k",
            "banh mi 20k",
            "tra sua 30k",
            "an sang 25k",
            "an trua 40k",
            "an toi 60k",
            "di cho 150k"
        )
        for (input in testCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertEquals("Input '$input' phải khớp Ăn uống", "Ăn uống", match?.name)
            assertFalse("Input '$input' không được fallback", isFallback)
        }
    }

    @Test
    fun testTransportationUnaccentedPhrases() {
        val testCases = listOf(
            "do xang 50k",
            "xang xe 70k",
            "gui xe 5k",
            "di grab 45k",
            "ve xe 120k",
            "xe bus 7k",
            "sua xe 80k"
        )
        for (input in testCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertEquals("Input '$input' phải khớp Đi lại", "Đi lại", match?.name)
            assertFalse("Input '$input' không được fallback", isFallback)
        }
    }

    @Test
    fun testHousingUnaccentedPhrases() {
        val testCases = listOf(
            "tien nha 2tr",
            "tien tro 1500k",
            "tien dien 500k",
            "tien nuoc 100k",
            "tien wifi 220k",
            "tien phong 1tr8"
        )
        for (input in testCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertEquals("Input '$input' phải khớp Nhà ở", "Nhà ở", match?.name)
            assertFalse("Input '$input' không được fallback", isFallback)
        }
    }

    @Test
    fun testShoppingAndHealthUnaccentedPhrases() {
        val (matchAo, isFbAo) = LocalToolExecutor.matchBestCategory("mua ao 150k", "EXPENSE", defaultExpenseCategories)
        assertEquals("Mua sắm", matchAo?.name)
        assertFalse(isFbAo)

        val (matchThuoc, isFbThuoc) = LocalToolExecutor.matchBestCategory("kham rang 200k", "EXPENSE", defaultExpenseCategories)
        assertEquals("Sức khỏe", matchThuoc?.name)
        assertFalse(isFbThuoc)

        val (matchKham, isFbKham) = LocalToolExecutor.matchBestCategory("mua thuoc ha sot 50k", "EXPENSE", defaultExpenseCategories)
        assertEquals("Sức khỏe", matchKham?.name)
        assertFalse(isFbKham)
    }

    @Test
    fun testEntertainmentUnaccentedPhrases() {
        val (matchCafe, isFbCafe) = LocalToolExecutor.matchBestCategory("uong cafe 35k", "EXPENSE", defaultExpenseCategories)
        assertTrue("uong cafe phải là Giải trí hoặc Ăn uống", matchCafe?.name == "Giải trí" || matchCafe?.name == "Ăn uống")
        assertFalse("uong cafe tuyệt đối không phải Khác", isFbCafe)

        val (matchPhim, isFbPhim) = LocalToolExecutor.matchBestCategory("xem phim rap 100k", "EXPENSE", defaultExpenseCategories)
        assertEquals("Giải trí", matchPhim?.name)
        assertFalse(isFbPhim)
    }

    @Test
    fun testIncomeUnaccentedPhrases() {
        val (matchLuong, isFbLuong) = LocalToolExecutor.matchBestCategory("nhan luong thang 10 15tr", "INCOME", defaultIncomeCategories)
        assertEquals("Lương", matchLuong?.name)
        assertFalse(isFbLuong)

        val (matchThuong, isFbThuong) = LocalToolExecutor.matchBestCategory("thuong kpi 2tr", "INCOME", defaultIncomeCategories)
        assertEquals("Thưởng", matchThuong?.name)
        assertFalse(isFbThuong)

        val (matchFreelance, isFbFreelance) = LocalToolExecutor.matchBestCategory("lam them freelance 1tr", "INCOME", defaultIncomeCategories)
        assertEquals("Làm thêm", matchFreelance?.name)
        assertFalse(isFbFreelance)

        val (matchDauTu, isFbDauTu) = LocalToolExecutor.matchBestCategory("lai co phieu 3tr", "INCOME", defaultIncomeCategories)
        assertEquals("Đầu tư", matchDauTu?.name)
        assertFalse(isFbDauTu)
    }

    @Test
    fun testFallBackToKhacOnlyWhenCompletelyUnrelated() {
        val (matchUnrelated, isFbUnrelated) = LocalToolExecutor.matchBestCategory("phi thuyen khong gian 100tr", "EXPENSE", defaultExpenseCategories)
        assertEquals("Khác", matchUnrelated?.name)
        assertTrue("Phải là fallback vì không khớp bất kỳ danh mục nào", isFbUnrelated)

        // Khi người dùng chỉ định rõ "khác"
        val (matchExplicit, isFbExplicit) = LocalToolExecutor.matchBestCategory("chi phi khac 50k", "EXPENSE", defaultExpenseCategories)
        assertEquals("Khác", matchExplicit?.name)
        assertFalse("Người dùng nói đích danh 'khac' nên không phải ngẫu nhiên fallback", isFbExplicit)
    }
}

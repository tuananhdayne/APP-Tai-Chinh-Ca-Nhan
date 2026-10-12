package com.example.apptaichinh.data.ai

import com.example.apptaichinh.data.ai.tools.LocalToolExecutor
import com.example.apptaichinh.data.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Bộ kiểm thử toàn diện & đa dạng (Diverse Real-World Test Suite)
 * Kiểm tra khả năng nhận diện tiếng Việt (có dấu, không dấu, viết tắt, đảo ngữ, tiếng lóng)
 * Ưu tiên tuyệt đối danh mục có sẵn trong hệ thống và chỉ fallback về 'Khác' khi nội dung hoàn toàn xa lạ.
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

    // =========================================================================
    // 1. KIỂM THỬ CHUẨN HÓA BỎ DẤU TIẾNG VIỆT (removeDiacritics)
    // =========================================================================

    @Test
    fun testRemoveDiacriticsAccuracy() {
        assertEquals("an pho", LocalToolExecutor.removeDiacritics("Ăn phở"))
        assertEquals("do xang", LocalToolExecutor.removeDiacritics("Đổ xăng"))
        assertEquals("tien nha", LocalToolExecutor.removeDiacritics("Tiền nhà"))
        assertEquals("kham rang", LocalToolExecutor.removeDiacritics("Khám răng"))
        assertEquals("di lai", LocalToolExecutor.removeDiacritics("Đi lại"))
        assertEquals("luong thang", LocalToolExecutor.removeDiacritics("Lương tháng"))
        assertEquals("suc khoe", LocalToolExecutor.removeDiacritics("Sức khỏe"))
        assertEquals("giai tri", LocalToolExecutor.removeDiacritics("Giải trí"))
    }

    // =========================================================================
    // 2. KIỂM THỬ ĐA DẠNG: DANH MỤC ĂN UỐNG (Food & Beverage)
    // =========================================================================

    @Test
    fun testDiverseFoodAndBeverageInputs() {
        val testCases = listOf(
            "an pho 40k",
            "an pho bo tai gau 55k",
            "bat pho 50k",
            "com tam suon bi cha 45k",
            "com rang dua bo 40k",
            "com van phong 35k",
            "bun cha ha noi 50k",
            "bun rieu cua 35k",
            "bun dau mam tom 45k",
            "bun bo hue 50k",
            "banh mi pate 20k",
            "banh mi chao 35k",
            "banh cuon nong 30k",
            "tra sua tran chau duong den 35k",
            "tra dao cam sa 30k",
            "tra tac 15k",
            "nuoc mia 10k",
            "an trua voi dong nghiep 50k",
            "an toi gia dinh 200k",
            "an sang xoi xeo 20k",
            "an lau hai san 350k",
            "di an buffet nuong 299k",
            "an oc luoc 80k",
            "di cho mua rau thit 150k",
            "mua thuc pham sieu thi winmart 300k",
            "an vat banh trang tron 25k",
            "pizza hut 150k",
            "kfc ga ran 120k",
            "lotteria burger 65k",
            "mi tom trung 15k",
            "banh trang nuong 20k"
        )

        for (input in testCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertNotNull("Không tìm thấy danh mục cho '$input'", match)
            assertEquals("Input '$input' bắt buộc phải về 'Ăn uống'", "Ăn uống", match?.name)
            assertFalse("Input '$input' không được là fallback Khác", isFallback)
        }
    }

    // =========================================================================
    // 3. KIỂM THỬ ĐA DẠNG: DANH MỤC ĐI LẠI (Transportation)
    // =========================================================================

    @Test
    fun testDiverseTransportationInputs() {
        val testCases = listOf(
            "do xang 50k",
            "do xang xe may 60k",
            "xang xe 70k",
            "di grab bike di lam 25k",
            "dat grab car ra san bay 150k",
            "be bike 20k",
            "be car 80k",
            "di taxi mai linh 90k",
            "xanh sm taxi 75k",
            "gui xe may o vincom 5k",
            "ve gui xe benh vien 10k",
            "ve xe thang 100k",
            "ve xe khach ve que 180k",
            "ve tau hoa 350k",
            "ve may bay khu hoi 1tr5",
            "sua xe may thay sam lop 120k",
            "thay nhot xe 90k",
            "rua xe may 25k",
            "bao duong xe 250k",
            "va lop xe 20k",
            "phi cau duong bot 35k",
            "ve qua tram thu phi cao toc 50k",
            "di xe bus tuyen 32 7k",
            "ve thang xe buyt 100k"
        )

        for (input in testCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertNotNull("Không tìm thấy danh mục cho '$input'", match)
            assertEquals("Input '$input' bắt buộc phải về 'Đi lại'", "Đi lại", match?.name)
            assertFalse("Input '$input' không được là fallback Khác", isFallback)
        }
    }

    // =========================================================================
    // 4. KIỂM THỬ ĐA DẠNG: DANH MỤC NHÀ Ở (Housing & Living)
    // =========================================================================

    @Test
    fun testDiverseHousingInputs() {
        val testCases = listOf(
            "tien nha thang nay 2tr5",
            "dong tien phong tro 1tr8",
            "tra tien thue nha 3tr",
            "dong tien dien thang 10 450k",
            "tien nuoc sinh hoat 85k",
            "hoa don dien nuoc 600k",
            "tien mang wifi fpt 220k",
            "cuoc internet vnpt 180k",
            "nap tien wifi viettel 250k",
            "phi dich vu chung cu 500k",
            "tien rac thang nay 30k",
            "doi binh gas petrolimex 350k",
            "thay bong den led phong khach 45k",
            "sua ong nuoc bi ro ri 80k",
            "nap the dien thoai viettel 50k",
            "cuoc dien thoai vina 100k"
        )

        for (input in testCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertNotNull("Không tìm thấy danh mục cho '$input'", match)
            assertEquals("Input '$input' bắt buộc phải về 'Nhà ở'", "Nhà ở", match?.name)
            assertFalse("Input '$input' không được là fallback Khác", isFallback)
        }
    }

    // =========================================================================
    // 5. KIỂM THỬ ĐA DẠNG: DANH MỤC MUA SẮM (Shopping)
    // =========================================================================

    @Test
    fun testDiverseShoppingInputs() {
        val testCases = listOf(
            "mua ao thun polo 180k",
            "mua quan jean tren shopee 250k",
            "san sale lazada doi giay 400k",
            "mua dep to ong 30k",
            "mua vay xinh di tiec 350k",
            "mua dam 400k",
            "mua son moi 200k",
            "kem duong da my pham 300k",
            "nuoc hoa nam 650k",
            "mua ban phim co 500k",
            "chuot may tinh logitech 250k",
            "tai nghe bluetooth 350k",
            "mua op lung dien thoai 45k",
            "day cap sac type c 60k",
            "cu sac nhanh 120k",
            "mua do gia dung noi com dien 450k",
            "mua chao chong dinh 150k"
        )

        for (input in testCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertNotNull("Không tìm thấy danh mục cho '$input'", match)
            assertEquals("Input '$input' bắt buộc phải về 'Mua sắm'", "Mua sắm", match?.name)
            assertFalse("Input '$input' không được là fallback Khác", isFallback)
        }
    }

    // =========================================================================
    // 6. KIỂM THỬ ĐA DẠNG: DANH MỤC GIẢI TRÍ (Entertainment)
    // =========================================================================

    @Test
    fun testDiverseEntertainmentInputs() {
        val testCases = listOf(
            "uong cafe sang 25k",
            "di ca phe trung voi ban 45k",
            "cafe highland 55k",
            "starbucks 85k",
            "di xem phim cgv 120k",
            "ve rap chieu phim lotte 90k",
            "mua bap nuoc xem phim 80k",
            "di hat karaoke voi cong ty 350k",
            "tien phong hat karaoke 200k",
            "di uong bia hoi voi dong nghiep 150k",
            "tien nhau cuoi tuan 250k",
            "di choi bi a 60k",
            "choi bowling 100k",
            "nap game lien quan 50k",
            "nap the garena 100k",
            "nap steam mua game 200k",
            "di du lich da nang 2tr",
            "nghi duong resort 1tr5"
        )

        for (input in testCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertNotNull("Không tìm thấy danh mục cho '$input'", match)
            assertEquals("Input '$input' phải là 'Giải trí'", "Giải trí", match?.name)
            assertFalse("Input '$input' không được là fallback Khác", isFallback)
        }
    }

    // =========================================================================
    // 7. KIỂM THỬ ĐA DẠNG: DANH MỤC SỨC KHỎE (Health & Medical)
    // =========================================================================

    @Test
    fun testDiverseHealthcareInputs() {
        val testCases = listOf(
            "mua thuoc panadol ha sot 30k",
            "mua thuoc cam cum o pharmacity 45k",
            "thuoc khang sinh hieu thuoc long chau 80k",
            "di kham rang nha khoa 200k",
            "nho rang khon 800k",
            "lay cao rang 150k",
            "tram rang sau 250k",
            "di kham benh vien bach mai 300k",
            "tien kham tong quat 500k",
            "xet nghiem mau 250k",
            "mua vitamin c va thuc pham chuc nang 220k",
            "mua khau trang y te 35k",
            "di kham mat o vien mat 150k",
            "cat kinh can 450k"
        )

        for (input in testCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertNotNull("Không tìm thấy danh mục cho '$input'", match)
            assertEquals("Input '$input' bắt buộc phải về 'Sức khỏe'", "Sức khỏe", match?.name)
            assertFalse("Input '$input' không được là fallback Khác", isFallback)
        }
    }

    // =========================================================================
    // 8. KIỂM THỬ ĐA DẠNG: DANH MỤC GIÁO DỤC (Education)
    // =========================================================================

    @Test
    fun testDiverseEducationInputs() {
        val testCases = listOf(
            "dong hoc phi ky 1 3tr",
            "nop tien hoc cho con 1tr5",
            "dong quy lop 100k",
            "mua sach giao khoa lop 10 250k",
            "mua giao trinh dai hoc 120k",
            "mua sach lap trinh 180k",
            "dang ky khoa hoc tieng anh ielts 2tr",
            "khoa hoc toeic 1tr",
            "khoa hoc online 500k",
            "thue gia su day kem toan 400k",
            "mua but vo compa thuoc ke 45k"
        )

        for (input in testCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertNotNull("Không tìm thấy danh mục cho '$input'", match)
            assertEquals("Input '$input' bắt buộc phải về 'Giáo dục'", "Giáo dục", match?.name)
            assertFalse("Input '$input' không được là fallback Khác", isFallback)
        }
    }

    // =========================================================================
    // 9. KIỂM THỬ ĐA DẠNG: DANH MỤC HIẾU HỈ (Social & Events)
    // =========================================================================

    @Test
    fun testDiverseSocialAndGiftsInputs() {
        val testCases = listOf(
            "mung cuoi ban than 500k",
            "tien phong bi dam cuoi 1tr",
            "di an cuoi 500k",
            "di vieng dam ma 300k",
            "phung vieng dam tang 200k",
            "mua qua sinh nhat tang nguoi yeu 350k",
            "tien qua sinh nhat 200k",
            "mung thoi noi chau 200k",
            "mung day thang 200k",
            "mua gio hoa qua tham nguoi om 250k",
            "tham benh 200k",
            "li xi tet cho cac chau 500k",
            "qua bieu tet bo me 1tr"
        )

        for (input in testCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertNotNull("Không tìm thấy danh mục cho '$input'", match)
            assertEquals("Input '$input' bắt buộc phải về 'Hiếu hỉ'", "Hiếu hỉ", match?.name)
            assertFalse("Input '$input' không được là fallback Khác", isFallback)
        }
    }

    // =========================================================================
    // 10. KIỂM THỬ ĐA DẠNG: TIẾT KIỆM & TRẢ NỢ (Savings & Debt)
    // =========================================================================

    @Test
    fun testDiverseSavingsAndDebtInputs() {
        val savingsCases = listOf(
            "gui tiet kiem online 5tr",
            "mo so tiet kiem ngan hang 10tr",
            "gui ngan hang tich luy 2tr",
            "bo heo dat 500k"
        )
        for (input in savingsCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertEquals("Input '$input' phải là 'Tiết kiệm'", "Tiết kiệm", match?.name)
            assertFalse(isFallback)
        }

        val debtCases = listOf(
            "tra no the tin dung 2tr",
            "thanh toan sao ke the credit 3tr",
            "tra gop dien thoai thang nay 800k",
            "tra tien vay fe credit 1tr",
            "tra tien muon ban 500k",
            "dong tien lai vay 300k"
        )
        for (input in debtCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertEquals("Input '$input' phải là 'Trả nợ'", "Trả nợ", match?.name)
            assertFalse(isFallback)
        }
    }

    // =========================================================================
    // 11. KIỂM THỬ ĐA DẠNG: CÁC NGUỒN THU NHẬP (Income Varieties)
    // =========================================================================

    @Test
    fun testDiverseIncomeInputs() {
        // Lương
        val salaryCases = listOf(
            "nhan luong thang 10 15tr",
            "cong ty ban luong 12tr",
            "payroll ting ting 18tr",
            "chuyen khoan luong cung 10tr"
        )
        for (input in salaryCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "INCOME", defaultIncomeCategories)
            assertEquals("Input '$input' phải là Lương", "Lương", match?.name)
            assertFalse(isFallback)
        }

        // Thưởng
        val bonusCases = listOf(
            "thuong kpi quy 3 5tr",
            "thuong nong du an 2tr",
            "thuong tet duong lich 3tr",
            "nhan hoa hong 1tr5"
        )
        for (input in bonusCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "INCOME", defaultIncomeCategories)
            assertEquals("Input '$input' phải là Thưởng", "Thưởng", match?.name)
            assertFalse(isFallback)
        }

        // Làm thêm
        val sideJobCases = listOf(
            "lam them du an freelance 3tr",
            "chay grab kiem duoc 300k",
            "tien cong lam them 500k",
            "thu lao viet bai 400k"
        )
        for (input in sideJobCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "INCOME", defaultIncomeCategories)
            assertEquals("Input '$input' phải là Làm thêm", "Làm thêm", match?.name)
            assertFalse(isFallback)
        }

        // Đầu tư
        val investCases = listOf(
            "lai co phieu vinamilk 2tr",
            "chot loi chung khoan 5tr",
            "lai tien gui tiet kiem 800k",
            "co tuc tien mat 1tr2"
        )
        for (input in investCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "INCOME", defaultIncomeCategories)
            assertEquals("Input '$input' phải là Đầu tư", "Đầu tư", match?.name)
            assertFalse(isFallback)
        }
    }

    // =========================================================================
    // 12. KIỂM THỬ ĐẢO NGỮ, TIỀN TỐ, TIẾNG LÓNG (Inverted & Slang)
    // =========================================================================

    @Test
    fun testDiverseSlangAndInvertedFormats() {
        val pairs = listOf(
            "40k an pho" to ("Ăn uống" to 40000L),
            "50 cành đổ xăng" to ("Đi lại" to 50000L),
            "2 củ tiền nhà" to ("Nhà ở" to 2000000L),
            "100k mua áo" to ("Mua sắm" to 100000L),
            "15tr tiền lương" to ("Lương" to 15000000L),
            "300k khám răng" to ("Sức khỏe" to 300000L),
            "500k mừng cưới" to ("Hiếu hỉ" to 500000L)
        )

        for ((input, expected) in pairs) {
            val (expectedCat, expectedAmount) = expected
            val amount = LocalToolExecutor.extractAmountFromText(input)
            assertEquals("Trích xuất số tiền sai cho '$input'", expectedAmount, amount)

            val type = if (expectedCat == "Lương") "INCOME" else "EXPENSE"
            val targetCats = if (type == "INCOME") defaultIncomeCategories else defaultExpenseCategories
            val (cat, isFallback) = LocalToolExecutor.matchBestCategory(input, type, targetCats)
            assertEquals("Sai danh mục cho '$input'", expectedCat, cat?.name)
            assertFalse(isFallback)
        }
    }

    // =========================================================================
    // 13. KIỂM THỬ LỆNH GHÉP ĐA GIAO DỊCH (Multi-item Batch Commands)
    // =========================================================================

    @Test
    fun testDiverseMultiItemBatchCommands() {
        // Lệnh 3 khoản không dấu
        val input3 = "an sang 30k, do xang 50k, uong cafe 25k"
        val clauses3 = LocalToolExecutor.splitMultiItemText(input3)
        assertEquals(3, clauses3.size)
        assertEquals(30000L, LocalToolExecutor.extractAmountFromText(clauses3[0]))
        assertEquals(50000L, LocalToolExecutor.extractAmountFromText(clauses3[1]))
        assertEquals(25000L, LocalToolExecutor.extractAmountFromText(clauses3[2]))
        assertEquals("Ăn uống", LocalToolExecutor.matchBestCategory(clauses3[0], "EXPENSE", defaultExpenseCategories).first?.name)
        assertEquals("Đi lại", LocalToolExecutor.matchBestCategory(clauses3[1], "EXPENSE", defaultExpenseCategories).first?.name)

        // Lệnh 5 khoản viết liền không dấu phẩy
        val input5 = "sang an pho 45k do xang 50k trua an com 40k chieu mua sach 100k toi di nhau 200k"
        val clauses5 = LocalToolExecutor.splitMultiItemText(input5)
        assertEquals(5, clauses5.size)

        // Lệnh hỗn hợp Thu nhập và Chi tiêu
        val inputMixed = "nhan luong 15tr, tra tien nha 3tr, dong tien dien 400k va gui tiet kiem 5tr"
        val clausesMixed = LocalToolExecutor.splitMultiItemText(inputMixed)
        assertEquals(4, clausesMixed.size)
        assertTrue("Khoản 1 phải là INCOME", LocalToolExecutor.isIncomeIntent(clausesMixed[0]))
        assertFalse("Khoản 2 phải là EXPENSE", LocalToolExecutor.isIncomeIntent(clausesMixed[1]))
        assertFalse("Khoản 3 phải là EXPENSE", LocalToolExecutor.isIncomeIntent(clausesMixed[2]))
        assertFalse("Khoản 4 phải là EXPENSE", LocalToolExecutor.isIncomeIntent(clausesMixed[3]))
    }

    // =========================================================================
    // 14. KIỂM THỬ ĐỘ BỀN VỚI KÝ TỰ ĐẶC BIỆT & VIẾT HOA (Punctuation & Case)
    // =========================================================================

    @Test
    fun testPunctuationAndCapitalizationRobustness() {
        val variations = listOf(
            "AN PHO 40K",
            "An Pho Bo 45k",
            "an   pho   40k",
            "an pho, 40k",
            "an pho: 40k",
            "An Pho! 40k",
            "DO XANG 50K",
            "Do Xang! 50k",
            "do xang - 50k",
            "TIEN NHA 2TR",
            "Tien Nha 2Tr."
        )

        for (input in variations) {
            val (cat, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertNotNull("Không match được cho '$input'", cat)
            assertTrue("'$input' không được là 'Khác'", cat?.name != "Khác")
            assertFalse(isFallback)
        }
    }

    // =========================================================================
    // 15. KIỂM THỬ CÁC TRƯỜNG HỢP XA LẠ THỰC SỰ THÌ MỚI FALLBACK VỀ KHÁC
    // =========================================================================

    @Test
    fun testRealWorldAmbiguousOutliersFallbackToKhac() {
        // Nội dung hoàn toàn xa lạ
        val outliers = listOf(
            "phi thuyen khong gian 100tr",
            "chi tieu mot khoan 200k",
            "khoan tien bi mat 200k",
            "chi 50k viec rieng"
        )
        for (input in outliers) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertEquals("Khoản xa lạ '$input' phải về 'Khác'", "Khác", match?.name)
            assertTrue("'$input' phải có cờ isFallback=true", isFallback)
        }

        // Người dùng nói đích danh từ "khác"
        val explicitCases = listOf(
            "chi khac 50k",
            "khoan khac 100k",
            "chi phi khac 35k"
        )
        for (input in explicitCases) {
            val (match, isFallback) = LocalToolExecutor.matchBestCategory(input, "EXPENSE", defaultExpenseCategories)
            assertEquals("Người dùng chỉ định '$input' phải về 'Khác'", "Khác", match?.name)
            assertFalse("'$input' không phải fallback ngẫu nhiên", isFallback)
        }
    }
}

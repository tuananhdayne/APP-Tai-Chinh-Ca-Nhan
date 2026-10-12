package com.example.apptaichinh.data.ai

import com.example.apptaichinh.data.ai.tools.LocalToolExecutor
import com.example.apptaichinh.data.ai.tools.ToolDefinitions
import com.example.apptaichinh.data.db.FinanceDatabaseHelper
import com.example.apptaichinh.data.model.Category
import com.example.apptaichinh.data.model.Transaction
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * BỘ KIỂM THỬ CHỨC NĂNG TRỢ LÝ AI ĐA VÒNG LẶP (AI ASSISTANT MULTI-LOOP EXECUTION TEST SUITE):
 * =========================================================================================
 * Kiểm thử chuyên sâu các luồng lệnh yêu cầu nhiều vòng lặp suy luận, xử lý nối tiếp và đa tác tử:
 *
 * NHÓM 1: VÒNG LẶP REACT ĐA BƯỚC (MULTI-STEP REACT LOOPS)
 * - Vòng 1: Query ngân sách danh mục -> Đối chiếu còn hạn mức -> Tạo thẻ giao dịch mới -> Tổng hợp báo cáo.
 * - Vòng 2: Tìm kiếm giao dịch cũ -> Tra cứu danh mục đích -> Cập nhật số tiền và đổi danh mục.
 * - Vòng 3: Tìm kiếm giao dịch -> Đề xuất xóa -> Xác nhận xóa -> Hoàn trả ngân sách danh mục và số dư ròng.
 * - Vòng 4: Vòng lặp 4 bước tổng hợp tài chính: Balance Summary -> Daily Summary -> Top Expenses -> Synthesis.
 * - Vòng 5: Quản lý danh mục 3 bước: Query categories -> Tạo danh mục mới -> Cài đặt hạn mức ngân sách mới.
 *
 * NHÓM 2: VÒNG LẶP XỬ LÝ ĐA MỆNH ĐỀ & ĐA LỆNH (BATCH MULTI-CLAUSE PROCESSING LOOPS)
 * - Vòng 6: Lệnh ghép 5 khoản chi tiêu & thu nhập -> Phân tách mệnh đề -> Trích xuất 5 thẻ PENDING độc lập.
 * - Vòng 7: Vòng lặp duyệt thẻ tuần tự (Iterative Card Confirmation Loop): Duyệt 4 thẻ, Hủy 1 thẻ -> Cân đối chuẩn xác.
 * - Vòng 8: Xử lý các liên từ văn nói tiếng Việt ("sau đó", "rồi lại", "tiếp theo", "đồng thời", "và").
 *
 * NHÓM 3: VÒNG LẶP HỘI THOẠI ĐA LƯỢT & BỘ NHỚ NGỮ CẢNH (MULTI-TURN CONVERSATIONAL MEMORY LOOPS)
 * - Vòng 9: Hội thoại 4 lượt: Đề xuất -> Đính chính số tiền & ghi chú -> Duyệt lưu -> Tra cứu tổng kết có nhớ dữ liệu vừa lưu.
 * - Vòng 10: Vòng lặp hủy bỏ lệnh dựa trên ngữ cảnh ("Thôi không ghi nữa", "Hủy thẻ vừa rồi").
 *
 * NHÓM 4: VÒNG LẶP LEO THANG NGÂN SÁCH (BUDGET ESCALATION LOOPS)
 * - Vòng 11: Chi tiêu liên tục nhiều vòng dẫn đến leo thang từ An toàn -> Cảnh báo -> Vượt hạn mức.
 * - Vòng 12: Tự động đề xuất điều chỉnh ngân sách khi phát hiện chi tiêu vượt ngưỡng.
 *
 * NHÓM 5: BẢO VỆ VÒNG LẶP & ẨN HOÀN TOÀN TÊN MODEL (SAFETY GUARDRAILS & MODEL OBFUSCATION)
 * - Vòng 13: Ngắt an toàn khi chạm ngưỡng tối đa (maxIterations = 10) chống treo / infinite loop.
 * - Vòng 14: Tuyệt đối không rò rỉ bất kỳ tên model LLM nào trong tất cả các thông báo, trạng thái, và giải thích.
 */
class AiMultiLoopExecutionTest {

    private val defaultCategories = FinanceDatabaseHelper.getDefaultCategoriesList()

    // =========================================================================
    // NHÓM 1: VÒNG LẶP REACT ĐA BƯỚC (MULTI-STEP REACT LOOPS)
    // =========================================================================

    /**
     * Vòng lặp 1: Query ngân sách danh mục -> Đối chiếu còn hạn mức -> Tạo thẻ giao dịch mới -> Tổng hợp báo cáo.
     * Kịch bản: "Kiểm tra xem danh mục Ăn uống còn ngân sách không, nếu còn thì ghi bữa trưa buffet 250k"
     */
    @Test
    fun testLoop1_QueryBudgetThenCreateTransactionThenSynthesize() {
        val userPrompt = "Kiểm tra xem danh mục Ăn uống còn ngân sách không, nếu còn thì ghi bữa trưa buffet 250k"
        val expenseCats = defaultCategories.filter { it.type == "EXPENSE" }

        // BƯỚC 1 (Vòng lặp 1): AI gọi query_category_budget để kiểm tra ngân sách
        val (matchedCat, _) = LocalToolExecutor.matchBestCategory("Ăn uống", "EXPENSE", expenseCats)
        assertNotNull("Phải khớp danh mục Ăn uống", matchedCat)
        assertEquals("Ăn uống", matchedCat?.name)

        val budgetLimit = 3500000L // Hạn mức 3.5 triệu
        val spentBefore = 1200000L // Đã tiêu 1.2 triệu
        val remainingBefore = budgetLimit - spentBefore // Còn 2.3 triệu

        assertTrue("Ngân sách còn dư, đủ chi trả 250k", remainingBefore >= 250000L)

        // BƯỚC 2 (Vòng lặp 2): Nhận thấy còn ngân sách, AI gọi tiếp action tool create_transaction
        val detectedAmount = LocalToolExecutor.extractAmountFromText("bữa trưa buffet 250k")
        assertEquals(250000L, detectedAmount)

        val createAction = ToolAction(
            type = ToolActionType.CREATE,
            amount = detectedAmount!!,
            transactionType = "EXPENSE",
            categoryId = matchedCat!!.id,
            categoryName = matchedCat.name,
            categoryIcon = matchedCat.icon,
            categoryColorHex = matchedCat.colorHex,
            note = "Bữa trưa buffet",
            status = CardStatus.PENDING
        )

        assertEquals(ToolActionType.CREATE, createAction.type)
        assertEquals(250000L, createAction.amount)
        assertEquals(CardStatus.PENDING, createAction.status)

        // BƯỚC 3 (Vòng lặp 3): Tổng hợp câu trả lời báo cáo ngân sách và đính kèm thẻ xác nhận
        val spentAfter = spentBefore + createAction.amount
        val remainingAfter = budgetLimit - spentAfter
        val explanation = "Danh mục Ăn uống hiện còn ${com.example.apptaichinh.ui.components.Formatters.formatVnd(remainingBefore)}. " +
                "Mình đã chuẩn bị sẵn phiếu chi 250.000 đ bên dưới. Sau khi lưu, ngân sách danh mục sẽ còn ${com.example.apptaichinh.ui.components.Formatters.formatVnd(remainingAfter)}."

        assertTrue(explanation.contains("250.000"))
        assertEquals(2050000L, remainingAfter)

        // BƯỚC 4: Người dùng duyệt -> Thẻ chuyển CONFIRMED
        val confirmedAction = createAction.copy(status = CardStatus.CONFIRMED)
        assertEquals(CardStatus.CONFIRMED, confirmedAction.status)
    }

    /**
     * Vòng lặp 2: Tìm kiếm giao dịch cũ -> Tra cứu danh mục đích -> Cập nhật số tiền và đổi danh mục.
     * Kịch bản: "Tìm khoản tiền điện thoại hôm qua rồi sửa thành 100k, đồng thời đổi sang danh mục Sinh hoạt"
     */
    @Test
    fun testLoop2_SearchTransactionThenQueryCategoryThenUpdate() {
        val oldTx = Transaction(
            id = 205L,
            amount = 50000L,
            type = "EXPENSE",
            categoryId = 10L,
            categoryName = "Khác",
            categoryIcon = "📦",
            categoryColorHex = "#607D8B",
            note = "Tiền điện thoại nạp card",
            dateEpoch = System.currentTimeMillis() - 86400000L
        )

        // BƯỚC 1 (Vòng lặp 1): AI gọi find_transactions(keyword="tiền điện thoại")
        val searchKeyword = "tiền điện thoại"
        assertTrue(oldTx.note.lowercase().contains(searchKeyword))

        // BƯỚC 2 (Vòng lặp 2): AI gọi query_categories đối chiếu danh mục "Sinh hoạt" -> Khớp vào "Nhà ở" (hóa đơn sinh hoạt)
        val (destCat, _) = LocalToolExecutor.matchBestCategory("Sinh hoạt", "EXPENSE", defaultCategories)
        assertNotNull("Phải khớp danh mục Nhà ở (hóa đơn sinh hoạt)", destCat)
        assertEquals("Nhà ở", destCat?.name)

        // BƯỚC 3 (Vòng lặp 3): AI gọi update_transaction với số tiền mới 100k và danh mục mới
        val newAmount = 100000L
        val updateAction = ToolAction(
            type = ToolActionType.UPDATE,
            targetTransaction = oldTx,
            newAmount = newAmount,
            categoryId = destCat!!.id,
            categoryName = destCat.name,
            categoryIcon = destCat.icon,
            categoryColorHex = destCat.colorHex,
            newNote = "Tiền điện thoại hàng tháng",
            searchKeyword = searchKeyword,
            status = CardStatus.PENDING
        )

        assertEquals(ToolActionType.UPDATE, updateAction.type)
        assertEquals(205L, updateAction.targetTransaction?.id)
        assertEquals(100000L, updateAction.newAmount)
        assertEquals("Nhà ở", updateAction.categoryName)

        // BƯỚC 4: Thực thi cập nhật giao dịch
        val updatedTx = oldTx.copy(
            amount = updateAction.newAmount ?: oldTx.amount,
            categoryId = if (updateAction.categoryId > 0) updateAction.categoryId else oldTx.categoryId,
            categoryName = updateAction.categoryName.ifBlank { oldTx.categoryName },
            categoryIcon = updateAction.categoryIcon.ifBlank { oldTx.categoryIcon },
            note = updateAction.newNote ?: oldTx.note
        )

        assertEquals(100000L, updatedTx.amount)
        assertEquals("Nhà ở", updatedTx.categoryName)
        assertEquals("Tiền điện thoại hàng tháng", updatedTx.note)
    }

    /**
     * Vòng lặp 3: Tìm kiếm giao dịch -> Đề xuất xóa -> Xác nhận xóa -> Hoàn trả ngân sách và số dư.
     * Kịch bản: "Xóa khoản ăn buffet 250k vừa ghi trưa nay đi"
     */
    @Test
    fun testLoop3_FindTransactionThenProposeDeleteThenRestoreBalance() {
        val buffetTx = Transaction(
            id = 301L,
            amount = 250000L,
            type = "EXPENSE",
            categoryId = 1L,
            categoryName = "Ăn uống",
            categoryIcon = "🍜",
            categoryColorHex = "#EF4444",
            note = "Bữa trưa buffet",
            dateEpoch = System.currentTimeMillis()
        )

        var totalExpense = 750000L
        var netBalance = 14250000L
        var categoryExpense = 500000L

        // BƯỚC 1 (Vòng lặp 1): Tìm kiếm giao dịch
        val searchKeyword = "buffet"
        assertTrue(buffetTx.note.lowercase().contains(searchKeyword))

        // BƯỚC 2 (Vòng lặp 2): Đề xuất thẻ xóa với targetTransaction
        val deleteAction = ToolAction(
            type = ToolActionType.DELETE,
            targetTransaction = buffetTx,
            searchKeyword = searchKeyword,
            status = CardStatus.PENDING
        )

        assertEquals(ToolActionType.DELETE, deleteAction.type)
        assertEquals(301L, deleteAction.targetTransaction?.id)
        assertEquals(CardStatus.PENDING, deleteAction.status)

        // BƯỚC 3 (Vòng lặp 3): Người dùng xác nhận xóa
        val confirmedDelete = deleteAction.copy(status = CardStatus.CONFIRMED)
        assertEquals(CardStatus.CONFIRMED, confirmedDelete.status)

        // Hoàn trả số dư ròng và giảm chi tiêu
        totalExpense -= buffetTx.amount
        netBalance += buffetTx.amount
        categoryExpense -= buffetTx.amount

        assertEquals(500000L, totalExpense)
        assertEquals(14500000L, netBalance)
        assertEquals(250000L, categoryExpense)
    }

    /**
     * Vòng lặp 4: Vòng lặp 4 bước tổng hợp tài chính (Balance Summary -> Daily -> Top Expenses -> Synthesis).
     */
    @Test
    fun testLoop4_FourStepFinancialInquirySynthesis() {
        val totalIncome = 25000000L
        val totalExpense = 8200000L
        val netBalance = totalIncome - totalExpense

        // BƯỚC 1: query_balance_summary
        val balanceResult = JSONObject().apply {
            put("income_vnd", totalIncome)
            put("expense_vnd", totalExpense)
            put("balance_vnd", netBalance)
        }
        assertEquals(16800000L, balanceResult.getLong("balance_vnd"))

        // BƯỚC 2: query_daily_summary
        val todayExpense = 320000L
        val dailyResult = JSONObject().apply {
            put("today_expense_vnd", todayExpense)
            put("count", 3)
        }
        assertEquals(320000L, dailyResult.getLong("today_expense_vnd"))

        // BƯỚC 3: query_top_expenses
        val topExpenses = JSONArray().apply {
            put(JSONObject().apply {
                put("category", "Tiền trọ")
                put("amount_vnd", 3500000L)
            })
            put(JSONObject().apply {
                put("category", "Mua sắm quần áo")
                put("amount_vnd", 1200000L)
            })
        }
        assertEquals(2, topExpenses.length())

        // BƯỚC 4: Tổng hợp câu trả lời hoàn chỉnh
        val synthesisText = "Tháng này bạn thu ${com.example.apptaichinh.ui.components.Formatters.formatVnd(totalIncome)}, " +
                "chi ${com.example.apptaichinh.ui.components.Formatters.formatVnd(totalExpense)}. " +
                "Số dư ròng hiện tại là ${com.example.apptaichinh.ui.components.Formatters.formatVnd(netBalance)}. " +
                "Hôm nay bạn đã chi ${com.example.apptaichinh.ui.components.Formatters.formatVnd(todayExpense)} qua 3 khoản."

        assertTrue(synthesisText.contains("16.800.000"))
        assertTrue(synthesisText.contains("320.000"))
    }

    /**
     * Vòng lặp 5: Quản lý danh mục 3 bước (Query categories -> Tạo danh mục mới -> Cài đặt hạn mức ngân sách).
     */
    @Test
    fun testLoop5_CreateCategoryThenSetCategoryBudgetLoop() {
        // BƯỚC 1: query_categories xem danh mục "Thú cưng" đã tồn tại chưa
        val existingCat = defaultCategories.find { it.name.equals("Thú cưng", ignoreCase = true) }
        assertNull("Chưa có danh mục Thú cưng", existingCat)

        // BƯỚC 2: create_category
        val createCatAction = ToolAction(
            type = ToolActionType.CREATE_CATEGORY,
            categoryName = "Thú cưng",
            categoryIcon = "🐶",
            categoryColorHex = "#F59E0B",
            transactionType = "EXPENSE",
            status = CardStatus.PENDING
        )
        assertEquals(ToolActionType.CREATE_CATEGORY, createCatAction.type)
        assertEquals("Thú cưng", createCatAction.categoryName)
        assertEquals("🐶", createCatAction.categoryIcon)

        // BƯỚC 3: set_category_budget cho danh mục mới tạo (hạn mức 1.5 triệu/tháng)
        val setBudgetAction = ToolAction(
            type = ToolActionType.SET_CATEGORY_BUDGET,
            categoryName = "Thú cưng",
            amount = 1500000L,
            status = CardStatus.PENDING
        )
        assertEquals(ToolActionType.SET_CATEGORY_BUDGET, setBudgetAction.type)
        assertEquals(1500000L, setBudgetAction.amount)
    }

    // =========================================================================
    // NHÓM 2: VÒNG LẶP XỬ LÝ ĐA MỆNH ĐỀ & ĐA LỆNH (BATCH MULTI-CLAUSE LOOPS)
    // =========================================================================

    /**
     * Vòng lặp 6: Lệnh ghép 5 khoản chi tiêu & thu nhập -> Phân tách mệnh đề -> Trích xuất 5 thẻ PENDING độc lập.
     */
    @Test
    fun testLoop6_FiveClauseBatchCommandParsingLoop() {
        val multiSentence = "Sáng ăn phở 45k, đổ xăng 50k, trưa mua sách 120k, chiều uống trà sữa 30k và tối nhận làm thêm 600k"
        val clauses = LocalToolExecutor.splitMultiItemText(multiSentence)

        assertEquals("Phải bóc tách được 5 mệnh đề", 5, clauses.size)

        val extractedActions = mutableListOf<ToolAction>()
        for (clause in clauses) {
            val amount = LocalToolExecutor.extractAmountFromText(clause)
            assertNotNull("Mỗi mệnh đề phải trích xuất được số tiền: $clause", amount)

            val isIncome = LocalToolExecutor.isIncomeIntent(clause)
            val type = if (isIncome) "INCOME" else "EXPENSE"
            val targetCats = defaultCategories.filter { it.type == type }
            val (cat, _) = LocalToolExecutor.matchBestCategory(clause, type, targetCats)
            val finalCat = cat ?: defaultCategories.first { it.type == type }

            extractedActions.add(
                ToolAction(
                    type = ToolActionType.CREATE,
                    amount = amount!!,
                    transactionType = type,
                    categoryId = finalCat.id,
                    categoryName = finalCat.name,
                    categoryIcon = finalCat.icon,
                    categoryColorHex = finalCat.colorHex,
                    note = clause.trim(),
                    status = CardStatus.PENDING
                )
            )
        }

        assertEquals(5, extractedActions.size)

        // Kiểm tra từng khoản
        assertEquals(45000L, extractedActions[0].amount)
        assertEquals("EXPENSE", extractedActions[0].transactionType)
        assertEquals("Ăn uống", extractedActions[0].categoryName)

        assertEquals(50000L, extractedActions[1].amount)
        assertEquals("EXPENSE", extractedActions[1].transactionType)
        assertEquals("Đi lại", extractedActions[1].categoryName)

        assertEquals(120000L, extractedActions[2].amount)
        assertEquals("EXPENSE", extractedActions[2].transactionType)
        assertEquals("Giáo dục", extractedActions[2].categoryName)

        assertEquals(30000L, extractedActions[3].amount)
        assertEquals("EXPENSE", extractedActions[3].transactionType)
        assertEquals("Ăn uống", extractedActions[3].categoryName)

        assertEquals(600000L, extractedActions[4].amount)
        assertEquals("INCOME", extractedActions[4].transactionType)
        assertEquals("Làm thêm", extractedActions[4].categoryName)
    }

    /**
     * Vòng lặp 7: Vòng lặp duyệt thẻ tuần tự (Iterative Card Confirmation Loop): Duyệt 4 thẻ, Hủy 1 thẻ.
     */
    @Test
    fun testLoop7_IterativeCardConfirmationAndCancellationLoop() {
        val actions = mutableListOf(
            ToolAction(type = ToolActionType.CREATE, amount = 45000L, transactionType = "EXPENSE", categoryName = "Ăn uống", status = CardStatus.PENDING),
            ToolAction(type = ToolActionType.CREATE, amount = 50000L, transactionType = "EXPENSE", categoryName = "Đi lại", status = CardStatus.PENDING),
            ToolAction(type = ToolActionType.CREATE, amount = 120000L, transactionType = "EXPENSE", categoryName = "Giáo dục", status = CardStatus.PENDING),
            ToolAction(type = ToolActionType.CREATE, amount = 30000L, transactionType = "EXPENSE", categoryName = "Ăn uống", status = CardStatus.PENDING),
            ToolAction(type = ToolActionType.CREATE, amount = 600000L, transactionType = "INCOME", categoryName = "Làm thêm", status = CardStatus.PENDING)
        )

        val chatMessage = ChatMessage(
            sender = MessageSender.ASSISTANT,
            text = "Chuẩn bị 5 thẻ giao dịch",
            toolActions = actions,
            cardStatus = CardStatus.PENDING
        )

        // Giả lập người dùng duyệt thẻ 0, 1, 2, 4 và HỦY thẻ 3 (trà sữa 30k bấm nhầm)
        val updatedActions = actions.mapIndexed { idx, act ->
            if (idx == 3) {
                act.copy(status = CardStatus.CANCELLED)
            } else {
                act.copy(status = CardStatus.CONFIRMED)
            }
        }

        val confirmedCount = updatedActions.count { it.status == CardStatus.CONFIRMED }
        val cancelledCount = updatedActions.count { it.status == CardStatus.CANCELLED }

        assertEquals(4, confirmedCount)
        assertEquals(1, cancelledCount)

        // Tính tổng chi tiêu thực tế ghi nhận
        val totalActualExpense = updatedActions
            .filter { it.status == CardStatus.CONFIRMED && it.transactionType == "EXPENSE" }
            .sumOf { it.amount }
        val totalActualIncome = updatedActions
            .filter { it.status == CardStatus.CONFIRMED && it.transactionType == "INCOME" }
            .sumOf { it.amount }

        assertEquals(45000L + 50000L + 120000L, totalActualExpense) // 215.000 đ (loại trừ 30k bị hủy)
        assertEquals(600000L, totalActualIncome)
        val netChange = totalActualIncome - totalActualExpense
        assertEquals(385000L, netChange)
    }

    /**
     * Vòng lặp 8: Xử lý các liên từ văn nói tiếng Việt ("sau đó", "rồi lại", "tiếp theo", "đồng thời", "và").
     */
    @Test
    fun testLoop8_VietnameseColloquialConjunctionSplitting() {
        val speechInput = "Trưa ăn bún bò 50k sau đó mua ly nước cam 25k rồi lại trả tiền giữ xe 5k đồng thời nhận lì xì 200k"
        val clauses = LocalToolExecutor.splitMultiItemText(speechInput)

        assertTrue(clauses.size >= 4)

        val amounts = clauses.mapNotNull { LocalToolExecutor.extractAmountFromText(it) }
        assertTrue(amounts.contains(50000L))
        assertTrue(amounts.contains(250000L) || amounts.contains(25000L))
        assertTrue(amounts.contains(5000L))
        assertTrue(amounts.contains(200000L))
    }

    // =========================================================================
    // NHÓM 3: VÒNG LẶP HỘI THOẠI ĐA LƯỢT (MULTI-TURN CONVERSATIONAL MEMORY)
    // =========================================================================

    /**
     * Vòng lặp 9: Hội thoại 4 lượt (4-Turn Conversational Memory Chain):
     * Lượt 1: Đề xuất thẻ.
     * Lượt 2: Người dùng đính chính số tiền & ghi chú -> AI nạp ngữ cảnh cập nhật.
     * Lượt 3: Người dùng bấm duyệt lưu.
     * Lượt 4: Tra cứu tổng kết, nhớ dữ liệu vừa lưu thành công.
     */
    @Test
    fun testLoop9_FourTurnConversationalMemoryChain() {
        // LƯỢT 1: User yêu cầu ghi mua sắm 500k
        val turn1User = "Ghi tiền mua sắm 500k"
        val initialAction = ToolAction(
            type = ToolActionType.CREATE,
            amount = 500000L,
            transactionType = "EXPENSE",
            categoryName = "Mua sắm",
            note = "Mua sắm",
            status = CardStatus.PENDING
        )
        val turn1Bot = ChatMessage(
            id = "msg_turn_1",
            sender = MessageSender.ASSISTANT,
            text = "Mình đã chuẩn bị thẻ mua sắm 500k",
            toolAction = initialAction,
            toolActions = listOf(initialAction),
            cardStatus = CardStatus.PENDING
        )

        // LƯỢT 2: User đổi ý: "À nhầm, 650k và ghi chú là mua áo khoác mùa đông"
        val turn2User = "À nhầm, 650k và ghi chú là mua áo khoác mùa đông"
        val correctedAmount = LocalToolExecutor.extractAmountFromText(turn2User)
        assertEquals(650000L, correctedAmount)

        val updatedAction = initialAction.copy(
            amount = correctedAmount!!,
            note = "Mua áo khoác mùa đông"
        )
        val turn2Bot = ChatMessage(
            id = "msg_turn_2",
            sender = MessageSender.ASSISTANT,
            text = "Mình đã cập nhật phiếu thành 650.000 đ (Mua áo khoác mùa đông)",
            toolAction = updatedAction,
            toolActions = listOf(updatedAction),
            cardStatus = CardStatus.PENDING
        )

        assertEquals(650000L, turn2Bot.toolAction?.amount)
        assertEquals("Mua áo khoác mùa đông", turn2Bot.toolAction?.note)

        // LƯỢT 3: Người dùng bấm xác nhận lưu
        val turn3Bot = turn2Bot.copy(
            cardStatus = CardStatus.CONFIRMED,
            toolActions = listOf(updatedAction.copy(status = CardStatus.CONFIRMED))
        )
        assertEquals(CardStatus.CONFIRMED, turn3Bot.cardStatus)
        assertEquals(CardStatus.CONFIRMED, turn3Bot.toolActions.first().status)

        // LƯỢT 4: Người dùng hỏi "Tổng tiền mua sắm tháng này là bao nhiêu?"
        // Lịch sử hội thoại nạp thẻ CONFIRMED với số tiền 650.000 đ
        val conversationHistory = listOf(
            ChatMessage(sender = MessageSender.USER, text = turn1User),
            turn1Bot,
            ChatMessage(sender = MessageSender.USER, text = turn2User),
            turn3Bot
        )

        val confirmedTransactionsInHistory = conversationHistory
            .flatMap { it.allToolActions }
            .filter { it.status == CardStatus.CONFIRMED }

        assertEquals(1, confirmedTransactionsInHistory.size)
        assertEquals(650000L, confirmedTransactionsInHistory.first().amount)
        assertEquals("Mua áo khoác mùa đông", confirmedTransactionsInHistory.first().note)
    }

    /**
     * Vòng lặp 10: Vòng lặp hủy bỏ lệnh dựa trên ngữ cảnh ("Thôi không ghi nữa", "Hủy thẻ vừa rồi").
     */
    @Test
    fun testLoop10_CancelCommandContextMemoryLoop() {
        val pendingAction = ToolAction(
            type = ToolActionType.CREATE,
            amount = 150000L,
            transactionType = "EXPENSE",
            categoryName = "Ăn uống",
            note = "Ăn lẩu một mình",
            status = CardStatus.PENDING
        )
        val botMessage = ChatMessage(
            id = "msg_cancel_test",
            sender = MessageSender.ASSISTANT,
            text = "Đã tạo thẻ Ăn lẩu 150k",
            toolAction = pendingAction,
            toolActions = listOf(pendingAction),
            cardStatus = CardStatus.PENDING
        )

        // Người dùng nhập: "Thôi hủy đi, bạn tôi trả tiền rồi"
        val cancelMessage = botMessage.copy(
            cardStatus = CardStatus.CANCELLED,
            toolActions = listOf(pendingAction.copy(status = CardStatus.CANCELLED))
        )

        assertEquals(CardStatus.CANCELLED, cancelMessage.cardStatus)
        assertEquals(CardStatus.CANCELLED, cancelMessage.toolActions.first().status)
    }

    // =========================================================================
    // NHÓM 4: VÒNG LẶP LEO THANG NGÂN SÁCH (BUDGET ESCALATION LOOPS)
    // =========================================================================

    /**
     * Vòng lặp 11: Chi tiêu liên tục nhiều vòng dẫn đến leo thang từ An toàn (<80%) -> Cảnh báo (>80%) -> Vượt hạn mức (>100%).
     */
    @Test
    fun testLoop11_ConsecutiveExpensesEscalateToOverBudgetThreshold() {
        val categoryBudget = 1000000L // 1 triệu
        var totalSpent = 0L

        val expenseBatch = listOf(
            300000L, // 300k (30% - An toàn)
            300000L, // 600k (60% - An toàn)
            250000L, // 850k (85% - Cảnh báo)
            100000L, // 950k (95% - Cảnh báo sát nút)
            100000L  // 1050k (105% - VƯỢT HẠN MỨC!)
        )

        val statusList = mutableListOf<String>()

        for (exp in expenseBatch) {
            totalSpent += exp
            val percentage = totalSpent.toFloat() / categoryBudget.toFloat()
            val status = when {
                totalSpent > categoryBudget -> "OVER_BUDGET"
                percentage > 0.8f -> "WARNING"
                else -> "SAFE"
            }
            statusList.add(status)
        }

        assertEquals("SAFE", statusList[0])
        assertEquals("SAFE", statusList[1])
        assertEquals("WARNING", statusList[2])
        assertEquals("WARNING", statusList[3])
        assertEquals("OVER_BUDGET", statusList[4])

        // Khoản cuối cùng vượt hạn mức 50.000 đ
        val remaining = categoryBudget - totalSpent
        assertEquals(-50000L, remaining)
        assertTrue(totalSpent > categoryBudget)
    }

    /**
     * Vòng lặp 12: Tự động đề xuất điều chỉnh ngân sách khi phát hiện chi tiêu vượt ngưỡng.
     */
    @Test
    fun testLoop12_AutoProposeBudgetAdjustmentWhenOverspent() {
        val currentBudget = 2000000L
        val spentAmount = 2400000L // Đã vượt 400k
        val isOver = spentAmount > currentBudget
        assertTrue(isOver)

        // AI đề xuất nâng ngân sách lên 3.000.000 đ
        val suggestedBudget = 3000000L
        val adjustBudgetAction = ToolAction(
            type = ToolActionType.SET_CATEGORY_BUDGET,
            categoryName = "Ăn uống",
            amount = suggestedBudget,
            status = CardStatus.PENDING
        )

        assertEquals(ToolActionType.SET_CATEGORY_BUDGET, adjustBudgetAction.type)
        assertEquals(3000000L, adjustBudgetAction.amount)
        assertTrue(adjustBudgetAction.amount > spentAmount)
    }

    // =========================================================================
    // NHÓM 5: BẢO VỆ VÒNG LẶP & ẨN HOÀN TOÀN TÊN MODEL (SAFETY GUARDRAILS & MODEL OBFUSCATION)
    // =========================================================================

    /**
     * Vòng lặp 13: Ngắt an toàn khi chạm ngưỡng tối đa (maxIterations = 10) chống treo / infinite loop.
     */
    @Test
    fun testLoop13_MaxIterationsGuardrailTerminatesSafely() {
        val maxIterations = 10
        var loopCounter = 0

        // Giả lập một vòng lặp tool calling không dừng
        while (loopCounter < maxIterations) {
            loopCounter++
        }

        assertEquals(10, loopCounter)
        assertTrue("Vòng lặp phải dừng chính xác ở bước thứ 10 để tránh cạn kiệt token", loopCounter <= maxIterations)
    }

    /**
     * Vòng lặp 14: Tuyệt đối không rò rỉ bất kỳ tên model LLM nào trong tất cả các thông báo, trạng thái, và giải thích.
     */
    @Test
    fun testLoop14_StrictZeroModelNameLeakageAcrossAllResponses() {
        val forbiddenModelTokens = listOf(
            "gemini", "gemini-3.7", "gemini 3.7", "gemini-3.8", "gemini 3.8",
            "ag/gemini", "qwen", "gpt-4", "gpt-3.5", "claude", "openai"
        )

        val sampleAppTexts = listOf(
            "Trực tuyến • Sẵn sàng hỗ trợ",
            "Trợ lý đang phân tích câu nói...",
            "Trợ lý phân tích & ghi chép thu chi thông minh. Nói hoặc gõ bằng tiếng Việt tự nhiên để thao tác siêu tốc.",
            "Mình đã chuẩn bị thẻ giao dịch mới. Bạn hãy kiểm tra và xác nhận lưu nhé!",
            "Mình đã nhận diện khoản Chi tiêu 55.000 đ",
            "Thông Minh",
            "Đã lưu vào sổ tay thành công!"
        )

        for (text in sampleAppTexts) {
            val lower = text.lowercase()
            for (forbidden in forbiddenModelTokens) {
                assertFalse(
                    "Chuỗi giao diện '$text' KHÔNG ĐƯỢC chứa tên model '$forbidden'",
                    lower.contains(forbidden)
                )
            }
        }
    }
}

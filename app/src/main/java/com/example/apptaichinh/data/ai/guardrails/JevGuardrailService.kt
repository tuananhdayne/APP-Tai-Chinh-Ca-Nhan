package com.example.apptaichinh.data.ai.guardrails

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class GuardrailDecision(
    val isAllowed: Boolean,
    val isOffTopic: Boolean,
    val reason: String = "",
    val cannedResponse: String = JevGuardrailService.DEFAULT_OFF_TOPIC_REPLY
)

/**
 * Service tích hợp mô hình Jev (System One của TypeSafe AI - https://api.typesafe.ai/v1/systemone)
 * làm Input Guardrail để phát hiện các chủ đề ngoài lề (off-topic) với độ trễ thấp và độ chính xác cao.
 *
 * Nếu phát hiện người dùng hỏi lạc đề (viết code, làm thơ, thời tiết, giải toán...),
 * hệ thống sẽ trả về câu trả lời mặc định ngay lập tức mà KHÔNG GỌI LLM chính, giúp tiết kiệm 100% token.
 */
class JevGuardrailService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()
) {

    companion object {
        const val DEFAULT_ENDPOINT = "https://api.typesafe.ai/v1/systemone"
        const val DEFAULT_MODEL = "jev-latest"
        const val DEFAULT_OFF_TOPIC_REPLY =
            "Xin chào! Mình là Trợ lý Quản lý Chi tiêu và Tài chính Cá nhân. " +
            "Mình chỉ có thể hỗ trợ bạn ghi chép thu chi, kiểm tra ngân sách và quản lý các giao dịch thôi nhé. " +
            "Bạn hãy thử nhắn: 'Ăn trưa 35k' hoặc 'Tháng này đã tiêu bao nhiêu?' nha! 💰"

        // Câu hỏi phân loại chuẩn theo schema TypeSafe AI SystemOne
        fun buildJevRequestBody(userMessage: String, model: String): String {
            val root = JSONObject()
            root.put("state", userMessage)
            root.put("model", model.ifBlank { DEFAULT_MODEL })

            val questions = JSONObject()

            // 1. Phân loại theo Choice (Categorical decision)
            val topicQuestion = JSONObject().apply {
                put("type", "choice")
                put(
                    "instructions",
                    "Classify whether this user input is relevant to a personal finance manager (tracking expenses, income, money, budgets, transactions, savings, or greetings/assistant capabilities) or if it is off-topic (unrelated queries like coding, storytelling, weather, sports, general knowledge, math homework, etc.)."
                )
                val criteria = JSONObject().apply {
                    put("finance_or_greeting", "Relevant to personal finance management, money tracking, expenses, income, budget, transactions, or polite greetings/bot intro.")
                    put("off_topic", "Completely unrelated topics such as writing code, poetry, storytelling, weather, homework, sports, general non-finance questions.")
                }
                put("criteria", criteria)
            }
            questions.put("topic", topicQuestion)

            root.put("questions", questions)
            return root.toString()
        }

        /**
         * Phân tích phản hồi JSON từ TypeSafe AI SystemOne
         */
        fun parseJevResponse(responseBody: String): GuardrailDecision {
            try {
                val json = JSONObject(responseBody)
                val answers = json.optJSONObject("answers") ?: return GuardrailDecision(isAllowed = true, isOffTopic = false, reason = "No answers object")

                val topicAnswer = answers.optJSONObject("topic")
                if (topicAnswer != null) {
                    val choice = topicAnswer.optString("choice", "")
                    if (choice.equals("off_topic", ignoreCase = true)) {
                        val confidence = topicAnswer.optDouble("confidence", 1.0)
                        return GuardrailDecision(
                            isAllowed = false,
                            isOffTopic = true,
                            reason = "Jev classified as off_topic (confidence: $confidence)"
                        )
                    }

                    // Hỗ trợ nếu Jev trả về dạng noul probability
                    if (topicAnswer.has("noul")) {
                        val prob = topicAnswer.optDouble("noul", 1.0)
                        if (prob < 0.35) {
                            return GuardrailDecision(
                                isAllowed = false,
                                isOffTopic = true,
                                reason = "Jev noul probability too low ($prob)"
                            )
                        }
                    }
                }

                return GuardrailDecision(
                    isAllowed = true,
                    isOffTopic = false,
                    reason = "Allowed by Jev"
                )
            } catch (e: Exception) {
                // Fail-safe: Nếu phân tích lỗi, cho phép tiếp tục để không làm gián đoạn người dùng
                return GuardrailDecision(isAllowed = true, isOffTopic = false, reason = "Parse error: ${e.message}")
            }
        }
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Kiểm tra tin nhắn người dùng qua TypeSafe AI Jev Guardrail.
     * Trả về GuardrailDecision:
     * - isOffTopic = true: Chủ đề ngoài lề -> Trả lời câu mặc định ngay, không gọi LLM chính.
     * - isAllowed = true: Thuộc chủ đề tài chính / chào hỏi -> Được phép gọi LLM chính.
     */
    suspend fun checkInputGuardrail(
        userMessage: String,
        endpointUrl: String = DEFAULT_ENDPOINT,
        apiKey: String,
        model: String = DEFAULT_MODEL
    ): GuardrailDecision = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext GuardrailDecision(isAllowed = true, isOffTopic = false, reason = "No API key configured")
        }

        try {
            val targetUrl = endpointUrl.trim().ifBlank { DEFAULT_ENDPOINT }
            val requestBodyStr = buildJevRequestBody(userMessage, model)
            val requestBody = requestBodyStr.toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url(targetUrl)
                .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "AppTaiChinh-JevGuardrail/1.0")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    // Fail-open: Nếu server Jev lỗi (5xx/4xx), không chặn người dùng
                    return@withContext GuardrailDecision(
                        isAllowed = true,
                        isOffTopic = false,
                        reason = "Jev HTTP error ${response.code}: $bodyStr"
                    )
                }

                return@withContext parseJevResponse(bodyStr)
            }
        } catch (e: Exception) {
            // Fail-open khi timeout hoặc mạng chập chờn
            return@withContext GuardrailDecision(
                isAllowed = true,
                isOffTopic = false,
                reason = "Jev network exception: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    /**
     * Kiểm tra ping kết nối tới máy chủ Jev Guardrail
     */
    suspend fun pingJev(
        endpointUrl: String = DEFAULT_ENDPOINT,
        apiKey: String,
        model: String = DEFAULT_MODEL
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IOException("Vui lòng nhập Jev API Key."))
        }

        try {
            val targetUrl = endpointUrl.trim().ifBlank { DEFAULT_ENDPOINT }
            val testPayload = buildJevRequestBody("Xin chào, hôm nay tôi ăn trưa 35k", model)
            val request = Request.Builder()
                .url(targetUrl)
                .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                .addHeader("Content-Type", "application/json")
                .post(testPayload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success("Kết nối thành công tới Jev Guardrail (Mã 200)!")
                } else {
                    Result.failure(IOException("Lỗi kết nối Jev (Mã ${response.code}): $bodyStr"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

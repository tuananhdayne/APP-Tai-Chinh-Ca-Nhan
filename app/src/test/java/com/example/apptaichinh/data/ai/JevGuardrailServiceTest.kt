package com.example.apptaichinh.data.ai

import com.example.apptaichinh.data.ai.guardrails.JevGuardrailService
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JevGuardrailServiceTest {

    @Test
    fun testBuildJevRequestBody_StructureMatchesTypeSafeAiSchema() {
        val userMessage = "Làm thơ về mùa thu"
        val model = "jev-latest"
        val requestJsonStr = JevGuardrailService.buildJevRequestBody(userMessage, model)

        val json = JSONObject(requestJsonStr)
        assertEquals("Làm thơ về mùa thu", json.getString("state"))
        assertEquals("jev-latest", json.getString("model"))

        val questions = json.getJSONObject("questions")
        val topic = questions.getJSONObject("topic")
        assertEquals("choice", topic.getString("type"))
        assertTrue("Cần có instructions", topic.getString("instructions").isNotBlank())

        val criteria = topic.getJSONObject("criteria")
        assertTrue(criteria.has("finance_or_greeting"))
        assertTrue(criteria.has("off_topic"))
    }

    @Test
    fun testParseJevResponse_OffTopicDetected() {
        val offTopicResponse = """
            {
              "answers": {
                "topic": {
                  "type": "choice",
                  "choice": "off_topic",
                  "confidence": 0.97,
                  "probabilities": {
                    "finance_or_greeting": 0.03,
                    "off_topic": 0.97
                  }
                }
              }
            }
        """.trimIndent()

        val decision = JevGuardrailService.parseJevResponse(offTopicResponse)
        assertTrue("Phải nhận diện là off_topic", decision.isOffTopic)
        assertFalse("Không được cho qua LLM chính", decision.isAllowed)
        assertEquals(JevGuardrailService.DEFAULT_OFF_TOPIC_REPLY, decision.cannedResponse)
    }

    @Test
    fun testParseJevResponse_FinanceOrGreetingAllowed() {
        val onTopicResponse = """
            {
              "answers": {
                "topic": {
                  "type": "choice",
                  "choice": "finance_or_greeting",
                  "confidence": 0.99,
                  "probabilities": {
                    "finance_or_greeting": 0.99,
                    "off_topic": 0.01
                  }
                }
              }
            }
        """.trimIndent()

        val decision = JevGuardrailService.parseJevResponse(onTopicResponse)
        assertFalse("Không phải off_topic", decision.isOffTopic)
        assertTrue("Được phép chuyển sang LLM chính", decision.isAllowed)
    }

    @Test
    fun testParseJevResponse_NoulProbabilityLow_Blocks() {
        val lowNoulResponse = """
            {
              "answers": {
                "topic": {
                  "type": "noul",
                  "noul": 0.15
                }
              }
            }
        """.trimIndent()

        val decision = JevGuardrailService.parseJevResponse(lowNoulResponse)
        assertTrue("Noul thấp phải bị coi là off_topic", decision.isOffTopic)
        assertFalse(decision.isAllowed)
    }

    @Test
    fun testParseJevResponse_MalformedJson_FailsOpen() {
        val malformed = "Không phải JSON hợp lệ"
        val decision = JevGuardrailService.parseJevResponse(malformed)
        // Fail-open: Khi lỗi phân tích, không chặn đứng trải nghiệm người dùng
        assertTrue(decision.isAllowed)
        assertFalse(decision.isOffTopic)
    }

    @Test
    fun testCheckInputGuardrail_MockHttpOffTopic_ReturnsCannedResponse() = kotlinx.coroutines.runBlocking {
        val mockResponseJson = """
            {
              "answers": {
                "topic": {
                  "type": "choice",
                  "choice": "off_topic",
                  "confidence": 0.99
                }
              }
            }
        """.trimIndent()

        val mockClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                okhttp3.Response.Builder()
                    .request(chain.request())
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(okhttp3.ResponseBody.Companion.run { mockResponseJson.toByteArray().let { bytes -> okhttp3.ResponseBody.create(null, bytes) } })
                    .build()
            }
            .build()

        val service = JevGuardrailService(mockClient)
        val decision = service.checkInputGuardrail(
            userMessage = "Viết code python cho game rắn săn mồi",
            apiKey = "test-api-key"
        )

        assertTrue("Phải chặn khi là off_topic", decision.isOffTopic)
        assertFalse("Không được cho qua LLM", decision.isAllowed)
        assertEquals(JevGuardrailService.DEFAULT_OFF_TOPIC_REPLY, decision.cannedResponse)
    }

    @Test
    fun testCheckInputGuardrail_MockHttpOnTopic_AllowsPassThrough() = kotlinx.coroutines.runBlocking {
        val mockResponseJson = """
            {
              "answers": {
                "topic": {
                  "type": "choice",
                  "choice": "finance_or_greeting",
                  "confidence": 0.95
                }
              }
            }
        """.trimIndent()

        val mockClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                okhttp3.Response.Builder()
                    .request(chain.request())
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(okhttp3.ResponseBody.Companion.run { mockResponseJson.toByteArray().let { bytes -> okhttp3.ResponseBody.create(null, bytes) } })
                    .build()
            }
            .build()

        val service = JevGuardrailService(mockClient)
        val decision = service.checkInputGuardrail(
            userMessage = "Trưa nay ăn bún chả 40k",
            apiKey = "test-api-key"
        )

        assertFalse("Không được đánh dấu là off_topic", decision.isOffTopic)
        assertTrue("Phải cho phép gọi LLM", decision.isAllowed)
    }

    @Test
    fun testLiveJevApiWithUserKey_OffTopicAndFinancialIntent() = kotlinx.coroutines.runBlocking {
        val apiKey = com.example.apptaichinh.data.db.FinanceDatabaseHelper.DEFAULT_JEV_API_KEY
        if (apiKey.isBlank()) return@runBlocking

        val liveService = JevGuardrailService()

        // 1. Test tin nhắn tài chính hợp lệ -> Phải cho phép
        val financeDecision = liveService.checkInputGuardrail(
            userMessage = "Ăn bún bò Huế 45k",
            apiKey = apiKey
        )
        assertFalse("Câu tài chính không được coi là off_topic", financeDecision.isOffTopic)
        assertTrue("Câu tài chính phải được phép đi tiếp", financeDecision.isAllowed)

        // 2. Test tin nhắn ngoài lề (off-topic) -> Phải chặn và trả lời mặc định
        val offTopicDecision = liveService.checkInputGuardrail(
            userMessage = "Viết code Python cho game rắn săn mồi",
            apiKey = apiKey
        )
        assertTrue("Câu hỏi code phải bị chặn là off_topic", offTopicDecision.isOffTopic)
        assertFalse("Không được cho câu hỏi code đi tiếp", offTopicDecision.isAllowed)
        assertEquals(JevGuardrailService.DEFAULT_OFF_TOPIC_REPLY, offTopicDecision.cannedResponse)
    }
}

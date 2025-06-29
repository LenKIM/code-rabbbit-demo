package io.agistep.loopers.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.agistep.loopers.dto.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KakaoTalkIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var webTestClient: WebTestClient

    private val objectMapper = jacksonObjectMapper()

    companion object {
        private lateinit var mockWebServer: MockWebServer

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            mockWebServer = MockWebServer()
            mockWebServer.start()
            
            registry.add("notification-hub.base-url") { mockWebServer.url("/").toString().trimEnd('/') }
            registry.add("notification-hub.app-key") { "test-app-key" }
            registry.add("notification-hub.secret-key") { "test-secret-key" }
            registry.add("notification-hub.sender-key") { "test-sender-key" }
        }
    }

    @BeforeEach
    fun setUp() {
        webTestClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .responseTimeout(Duration.ofSeconds(30))
            .build()
    }

    @AfterEach
    fun tearDown() {
        // mockWebServer는 companion object에서 관리되므로 여기서 종료하지 않음
    }

    @Test
    fun `should send alimtalk through full integration`() {
        // Given
        val request = KakaoTalkSendRequest(
            templateCode = "INTEGRATION_TEST",
            recipientNo = "01012345678",
            templateParameter = mapOf("name" to "통합테스트", "amount" to "5000")
        )
        
        val mockResponse = NotificationHubResponse(
            header = ResponseHeader(
                resultCode = 0,
                resultMessage = "SUCCESS",
                isSuccessful = true
            ),
            body = ResponseBody(
                data = SendResponseData(
                    requestId = "integration-test-id",
                    statusCode = "SUCCESS",
                    sendResultList = listOf(
                        SendResult(
                            recipientSeq = 1,
                            recipientNo = "01012345678",
                            resultCode = 0,
                            resultMessage = "SUCCESS"
                        )
                    )
                )
            )
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200)
        )

        // When & Then
        webTestClient.post()
            .uri("/api/v1/kakaotalk/send")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(request))
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.header.isSuccessful").isEqualTo(true)
            .jsonPath("$.header.resultCode").isEqualTo(0)
            .jsonPath("$.body.data.requestId").isEqualTo("integration-test-id")
            .jsonPath("$.body.data.sendResultList[0].recipientNo").isEqualTo("01012345678")
            .jsonPath("$.body.data.sendResultList[0].resultCode").isEqualTo(0)
        
        // Verify the request was made to mock server
        val recordedRequest = mockWebServer.takeRequest()
        assert(recordedRequest.path!!.contains("/alimtalk/v2.2/appkeys/test-app-key/messages"))
        assert(recordedRequest.method == "POST")
    }

    @Test
    fun `should send bulk alimtalk through full integration`() {
        // Given
        val request = BulkSendRequest(
            templateCode = "BULK_INTEGRATION_TEST",
            recipients = listOf(
                BulkRecipient(
                    recipientNo = "01012345678",
                    templateParameter = mapOf("name" to "사용자1")
                ),
                BulkRecipient(
                    recipientNo = "01087654321",
                    templateParameter = mapOf("name" to "사용자2")
                )
            )
        )
        
        val mockResponse = NotificationHubResponse(
            header = ResponseHeader(
                resultCode = 0,
                resultMessage = "SUCCESS",
                isSuccessful = true
            ),
            body = ResponseBody(
                data = SendResponseData(
                    requestId = "bulk-integration-test-id",
                    statusCode = "SUCCESS",
                    sendResultList = listOf(
                        SendResult(1, "01012345678", 0, "SUCCESS"),
                        SendResult(2, "01087654321", 0, "SUCCESS")
                    )
                )
            )
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200)
        )

        // When & Then
        webTestClient.post()
            .uri("/api/v1/kakaotalk/send/bulk")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(request))
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.header.isSuccessful").isEqualTo(true)
            .jsonPath("$.body.data.requestId").isEqualTo("bulk-integration-test-id")
            .jsonPath("$.body.data.sendResultList").isArray
            .jsonPath("$.body.data.sendResultList.length()").isEqualTo(2)
    }

    @Test
    fun `should handle error response in integration test`() {
        // Given
        val request = KakaoTalkSendRequest(
            templateCode = "INVALID_TEMPLATE",
            recipientNo = "01012345678"
        )
        
        val errorResponse = NotificationHubResponse(
            header = ResponseHeader(
                resultCode = -1000,
                resultMessage = "Template not found",
                isSuccessful = false
            )
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setBody(objectMapper.writeValueAsString(errorResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200)
        )

        // When & Then
        webTestClient.post()
            .uri("/api/v1/kakaotalk/send")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(request))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.header.isSuccessful").isEqualTo(false)
            .jsonPath("$.header.resultCode").isEqualTo(-1000)
            .jsonPath("$.header.resultMessage").isEqualTo("Template not found")
    }
}

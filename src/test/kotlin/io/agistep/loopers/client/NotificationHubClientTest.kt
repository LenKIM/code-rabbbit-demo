package io.agistep.loopers.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.agistep.loopers.config.NotificationHubProperties
import io.agistep.loopers.dto.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NotificationHubClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var notificationHubClient: NotificationHubClient
    private lateinit var properties: NotificationHubProperties
    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        properties = NotificationHubProperties(
            baseUrl = mockWebServer.url("/").toString().trimEnd('/'),
            appKey = "test-app-key",
            secretKey = "test-secret-key",
            senderKey = "test-sender-key",
            timeout = Duration.ofSeconds(30)
        )
        
        val webClient = WebClient.builder()
            .baseUrl(properties.baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
            
        notificationHubClient = NotificationHubClient(webClient, properties)
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should send alimtalk successfully`() = runBlocking {
        // Given
        val request = NotificationHubSendRequest(
            senderKey = "test-sender-key",
            templateCode = "TEST_TEMPLATE",
            recipientList = listOf(
                Recipient(
                    recipientNo = "01012345678",
                    templateParameter = mapOf("name" to "테스트")
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
                    requestId = "test-request-id",
                    statusCode = "SUCCESS"
                )
            )
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        )

        // When
        val response = notificationHubClient.sendAlimTalk(request)

        // Then
        assertNotNull(response)
        assertTrue(response.header.isSuccessful)
        assertEquals(0, response.header.resultCode)
        assertEquals("SUCCESS", response.header.resultMessage)
        assertEquals("test-request-id", response.body?.data?.requestId)
        
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertTrue(recordedRequest.path!!.contains("/alimtalk/v2.2/appkeys/${properties.appKey}/messages"))
    }

    @Test
    fun `should handle error response`() = runBlocking {
        // Given
        val request = NotificationHubSendRequest(
            senderKey = "test-sender-key",
            templateCode = "INVALID_TEMPLATE",
            recipientList = listOf(
                Recipient(recipientNo = "01012345678")
            )
        )
        
        val errorResponse = NotificationHubResponse(
            header = ResponseHeader(
                resultCode = -1000,
                resultMessage = "Invalid template code",
                isSuccessful = false
            )
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setBody(objectMapper.writeValueAsString(errorResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        )

        // When
        val response = notificationHubClient.sendAlimTalk(request)

        // Then
        assertNotNull(response)
        assertEquals(false, response.header.isSuccessful)
        assertEquals(-1000, response.header.resultCode)
        assertEquals("Invalid template code", response.header.resultMessage)
    }
}

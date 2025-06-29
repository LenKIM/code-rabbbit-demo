package io.agistep.loopers.dto

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KakaoTalkDtoTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `should validate correct phone number`() {
        // Given
        val request = KakaoTalkSendRequest(
            templateCode = "TEST_TEMPLATE",
            recipientNo = "01012345678",
            templateParameter = mapOf("name" to "테스트")
        )

        // When
        val violations = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `should invalidate incorrect phone number format`() {
        // Given
        val request = KakaoTalkSendRequest(
            templateCode = "TEST_TEMPLATE",
            recipientNo = "123456789", // 잘못된 형식
            templateParameter = mapOf("name" to "테스트")
        )

        // When
        val violations = validator.validate(request)

        // Then
        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("올바른 휴대폰 번호 형식이 아닙니다", violation.message)
        assertEquals("recipientNo", violation.propertyPath.toString())
    }

    @Test
    fun `should invalidate empty template code`() {
        // Given
        val request = KakaoTalkSendRequest(
            templateCode = "", // 빈 값
            recipientNo = "01012345678",
            templateParameter = mapOf("name" to "테스트")
        )

        // When
        val violations = validator.validate(request)

        // Then
        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("templateCode", violation.propertyPath.toString())
    }

    @Test
    fun `should validate phone number with 9 digits`() {
        // Given
        val request = KakaoTalkSendRequest(
            templateCode = "TEST_TEMPLATE",
            recipientNo = "0101234567", // 9자리
            templateParameter = mapOf("name" to "테스트")
        )

        // When
        val violations = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `should validate phone number with 10 digits`() {
        // Given
        val request = KakaoTalkSendRequest(
            templateCode = "TEST_TEMPLATE",
            recipientNo = "01012345678", // 10자리
            templateParameter = mapOf("name" to "테스트")
        )

        // When
        val violations = validator.validate(request)

        // Then
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `should create notification hub send request correctly`() {
        // Given
        val recipients = listOf(
            Recipient(
                recipientNo = "01012345678",
                templateParameter = mapOf("name" to "사용자1"),
                resendParameter = ResendParameter(isResend = true)
            )
        )

        // When
        val request = NotificationHubSendRequest(
            senderKey = "test-sender-key",
            templateCode = "TEST_TEMPLATE",
            requestDate = "2024-01-01 12:00:00",
            recipientList = recipients
        )

        // Then
        assertEquals("test-sender-key", request.senderKey)
        assertEquals("TEST_TEMPLATE", request.templateCode)
        assertEquals(1, request.recipientList.size)
        assertEquals("01012345678", request.recipientList[0].recipientNo)
        assertEquals("사용자1", request.recipientList[0].templateParameter["name"])
        assertEquals(true, request.recipientList[0].resendParameter?.isResend)
    }
}

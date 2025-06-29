package io.agistep.loopers.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class KakaoTalkSendRequest(
    @field:NotBlank
    val templateCode: String,
    
    @field:Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
    val recipientNo: String,
    
    val templateParameter: Map<String, String> = emptyMap(),
    
    val resendParameter: ResendParameter? = null
)

data class ResendParameter(
    val isResend: Boolean = false,
    val resendType: String? = null,
    val resendTitle: String? = null,
    val resendContent: String? = null
)

data class NotificationHubSendRequest(
    val senderKey: String,
    val templateCode: String,
    val requestDate: String? = null,
    val senderGroupingKey: String? = null,
    val recipientList: List<Recipient>
)

data class Recipient(
    val recipientNo: String,
    val templateParameter: Map<String, String> = emptyMap(),
    val resendParameter: ResendParameter? = null,
    val recipientGroupingKey: String? = null
)

data class NotificationHubResponse(
    @JsonProperty("header")
    val header: ResponseHeader,
    
    @JsonProperty("body")
    val body: ResponseBody? = null
)

data class ResponseHeader(
    val resultCode: Int,
    val resultMessage: String,
    val isSuccessful: Boolean
)

data class ResponseBody(
    val data: SendResponseData? = null
)

data class SendResponseData(
    val requestId: String,
    val statusCode: String,
    val senderGroupingKey: String? = null,
    val sendResultList: List<SendResult>? = null
)

data class SendResult(
    val recipientSeq: Int,
    val recipientNo: String,
    val resultCode: Int,
    val resultMessage: String,
    val recipientGroupingKey: String? = null
)

data class BulkSendRequest(
    @field:NotBlank
    val templateCode: String,
    val recipients: List<BulkRecipient>
)

data class BulkRecipient(
    @field:Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
    val recipientNo: String,
    val templateParameter: Map<String, String> = emptyMap(),
    val resendParameter: ResendParameter? = null
)

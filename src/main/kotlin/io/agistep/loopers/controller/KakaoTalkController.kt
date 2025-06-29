package io.agistep.loopers.controller

import io.agistep.loopers.dto.BulkRecipient
import io.agistep.loopers.dto.KakaoTalkSendRequest
import io.agistep.loopers.dto.NotificationHubResponse
import io.agistep.loopers.service.KakaoTalkService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/kakaotalk")
class KakaoTalkController(
    private val kakaoTalkService: KakaoTalkService
) {

    @PostMapping("/send")
    suspend fun sendAlimTalk(
        @Valid @RequestBody request: KakaoTalkSendRequest
    ): ResponseEntity<NotificationHubResponse> {
        val response = kakaoTalkService.sendAlimTalk(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/send/bulk")
    suspend fun sendBulkAlimTalk(
        @RequestBody request: BulkSendRequest
    ): ResponseEntity<NotificationHubResponse> {
        val response = kakaoTalkService.sendBulkAlimTalk(
            templateCode = request.templateCode,
            recipients = request.recipients
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/send/bulk")
    suspend fun sendBulkAlimTalk2(
        @RequestBody request: BulkSendRequest
    ): ResponseEntity<NotificationHubResponse> {
        val response = kakaoTalkService.sendBulkAlimTalk(
            templateCode = request.templateCode,
            recipients = request.recipients
        )
        return ResponseEntity.ok(response)
    }
}

data class BulkSendRequest(
    val templateCode: String,
    val recipients: List<BulkRecipient>
)

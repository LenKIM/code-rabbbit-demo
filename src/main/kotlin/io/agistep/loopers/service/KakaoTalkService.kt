package io.agistep.loopers.service

import io.agistep.loopers.client.NotificationHubClient
import io.agistep.loopers.config.NotificationHubProperties
import io.agistep.loopers.dto.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class KakaoTalkService(
    private val notificationHubClient: NotificationHubClient,
    private val properties: NotificationHubProperties
) {
    private val logger = LoggerFactory.getLogger(KakaoTalkService::class.java)
    
    suspend fun sendAlimTalk(request: KakaoTalkSendRequest): NotificationHubResponse {
        logger.info("Processing alimtalk send request for recipient: ${request.recipientNo}")
        
        val notificationRequest = NotificationHubSendRequest(
            senderKey = properties.senderKey,
            templateCode = request.templateCode,
            requestDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            senderGroupingKey = generateGroupingKey(),
            recipientList = listOf(
                Recipient(
                    recipientNo = request.recipientNo,
                    templateParameter = request.templateParameter,
                    resendParameter = request.resendParameter,
                    recipientGroupingKey = generateGroupingKey()
                )
            )
        )
        
        return notificationHubClient.sendAlimTalk(notificationRequest)
    }
    
    suspend fun sendBulkAlimTalk(
        templateCode: String,
        recipients: List<BulkRecipient>
    ): NotificationHubResponse {
        logger.info("Processing bulk alimtalk send request for ${recipients.size} recipients")
        
        val notificationRequest = NotificationHubSendRequest(
            senderKey = properties.senderKey,
            templateCode = templateCode,
            requestDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            senderGroupingKey = generateGroupingKey(),
            recipientList = recipients.map { recipient ->
                Recipient(
                    recipientNo = recipient.recipientNo,
                    templateParameter = recipient.templateParameter,
                    resendParameter = recipient.resendParameter,
                    recipientGroupingKey = generateGroupingKey()
                )
            }
        )
        
        return notificationHubClient.sendAlimTalk(notificationRequest)
    }
    
    private fun generateGroupingKey(): String {
        return UUID.randomUUID().toString().replace("-", "").take(20)
    }
}

package io.agistep.loopers.client

import io.agistep.loopers.config.NotificationHubProperties
import io.agistep.loopers.dto.NotificationHubResponse
import io.agistep.loopers.dto.NotificationHubSendRequest
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class NotificationHubClient(
    private val webClient: WebClient,
    private val properties: NotificationHubProperties
) {
    private val logger = LoggerFactory.getLogger(NotificationHubClient::class.java)

    suspend fun sendAlimTalk(request: NotificationHubSendRequest): NotificationHubResponse {
        logger.info("Sending alimtalk message to NotificationHub. Recipients: ${request.recipientList.size}")
        
        return try {
            webClient.post()
                .uri("/alimtalk/v2.2/appkeys/${properties.appKey}/messages")
                .bodyValue(request)
                .retrieve()
                .bodyToMono<NotificationHubResponse>()
                .doOnSuccess { response ->
                    logger.info("Successfully sent alimtalk. RequestId: ${response.body?.data?.requestId}")
                }
                .doOnError { error ->
                    logger.error("Failed to send alimtalk: ${error.message}", error)
                }
                .awaitSingle()
        } catch (e: Exception) {
            logger.error("Error occurred while sending alimtalk", e)
            throw NotificationHubException("Failed to send alimtalk message", e)
        }
    }
}

class NotificationHubException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

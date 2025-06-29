package io.agistep.loopers.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import java.time.Duration

@ConfigurationProperties(prefix = "notification-hub")
data class NotificationHubProperties @ConstructorBinding constructor(
    val baseUrl: String,
    val appKey: String,
    val secretKey: String,
    val senderKey: String,
    val timeout: Duration = Duration.ofSeconds(30)
)

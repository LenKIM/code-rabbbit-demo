package io.agistep.loopers.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@Configuration
@EnableConfigurationProperties(NotificationHubProperties::class)
class NotificationHubConfig {

    @Bean
    fun notificationHubWebClient(properties: NotificationHubProperties): WebClient {
        val auth = Base64.getEncoder().encodeToString("${properties.appKey}:${properties.secretKey}".toByteArray())
        
        return WebClient.builder()
            .baseUrl(properties.baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic $auth")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}

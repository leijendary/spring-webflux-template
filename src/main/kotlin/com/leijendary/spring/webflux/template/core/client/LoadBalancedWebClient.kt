package com.leijendary.spring.webflux.template.core.client

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class LoadBalancedWebClient(private val webClientBuilder: WebClient.Builder) {
    fun createClient(name: String): WebClient {
        return webClientBuilder
            .baseUrl("http://$name")
            .build()
    }
}
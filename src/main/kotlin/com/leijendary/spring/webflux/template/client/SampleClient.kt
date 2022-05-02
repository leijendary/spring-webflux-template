package com.leijendary.spring.webflux.template.client

import com.leijendary.spring.webflux.template.core.client.LoadBalancedWebClient
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component

@Component
class SampleClient(loadBalancedWebClient: LoadBalancedWebClient) {
    private val webClient = loadBalancedWebClient.createClient("google")

    suspend fun homepage(): String {
        return webClient
            .get()
            .exchangeToMono { it.bodyToMono(String::class.java) }
            .awaitSingle()
    }
}
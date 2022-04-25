package com.leijendary.spring.webflux.template.client

import com.leijendary.spring.webflux.template.core.client.LoadBalancedWebClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SampleClient(loadBalancedWebClient: LoadBalancedWebClient) {
    private val webClient = loadBalancedWebClient.createClient("google")

    fun homepage(): Mono<String> {
        return webClient
            .get()
            .exchangeToMono { it.bodyToMono(String::class.java) }
    }
}
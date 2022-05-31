package com.leijendary.spring.webflux.template.core.filter

import com.leijendary.spring.webflux.template.core.util.EXCHANGE_CONTEXT_KEY
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class RequestContextFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain
            .filter(exchange)
            .contextWrite { it.put(EXCHANGE_CONTEXT_KEY, exchange) }
    }
}
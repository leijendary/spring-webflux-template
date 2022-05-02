package com.leijendary.spring.webflux.template.core.filter

import com.leijendary.spring.webflux.template.core.config.properties.AuthProperties
import com.leijendary.spring.webflux.template.core.util.*
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@Order(HIGHEST_PRECEDENCE)
class RequestContextFilter(private val authProperties: AuthProperties) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain
            .filter(exchange)
            .contextWrite {
                val request = exchange.request
                val headers = request.headers
                val userId = headers.getFirst(HEADER_USER_ID) ?: authProperties.system.principal
                val traceId = headers.getFirst(HEADER_TRACE_ID) ?: request.id

                it
                    .put(CONTEXT_EXCHANGE, exchange)
                    .put(CONTEXT_USER_ID, userId)
                    .put(CONTEXT_TRACE_ID, traceId)
            }
    }
}
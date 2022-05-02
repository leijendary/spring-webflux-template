package com.leijendary.spring.webflux.template.core.filter

import com.leijendary.spring.webflux.template.core.util.HEADER_TRACE_ID
import com.leijendary.spring.webflux.template.core.util.RequestContext.traceId
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

@Component
class TraceIdClientFilter : ExchangeFilterFunction {
    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        return mono { traceId() }
            .mapNotNull { request.headers().set(HEADER_TRACE_ID, it) }
            .flatMap { next.exchange(request) }
    }
}
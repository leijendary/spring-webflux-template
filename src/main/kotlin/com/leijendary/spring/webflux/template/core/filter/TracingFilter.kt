package com.leijendary.spring.webflux.template.core.filter

import org.springframework.cloud.sleuth.Tracer
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class TracingFilter(private val tracer: Tracer) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val headers = exchange.response.headers
        val context = tracer.currentSpan()?.context()
        context?.run {
            headers["X-B3-TraceId"] = traceId()
            headers["X-B3-SpanId"] = spanId()

            val sampled = if (sampled()) "1" else "0"

            headers["X-B3-Sampled"] = sampled

            val parentId = parentId()
            parentId?.run {
                headers["X-B3-ParentId"] = this
            }
        }

        return chain.filter(exchange)
    }
}
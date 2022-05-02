package com.leijendary.spring.webflux.template.core.util

import com.leijendary.spring.webflux.template.core.extension.locale
import com.leijendary.spring.webflux.template.core.extension.timeZone
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import java.time.ZoneId
import java.util.*

const val HEADER_TRACE_ID = "X-Trace-ID"
const val HEADER_USER_ID = "X-User-ID"
const val CONTEXT_USER_ID = "UserID"
const val CONTEXT_TRACE_ID = "TraceID"
const val CONTEXT_EXCHANGE: String = "ServerWebExchange"

object RequestContext {
    val currentExchange: Mono<ServerWebExchange>
        get() = Mono
            .deferContextual {
                it.getOrEmpty<ServerWebExchange>(CONTEXT_EXCHANGE)
                    .orElse(null)
                    .toMono()
            }
            .switchIfEmpty { Mono.empty() }

    val currentRequest: Mono<ServerHttpRequest>
        get() = currentExchange
            .map { it.request }
            .switchIfEmpty { Mono.empty() }

    val userId: Mono<String>
        get() = Mono
            .deferContextual {
                it.getOrEmpty<String>(CONTEXT_USER_ID)
                    .orElse(null)
                    .toMono()
            }
            .switchIfEmpty { Mono.empty() }

    val traceId: Mono<String>
        get() = Mono
            .deferContextual {
                it.getOrEmpty<String>(CONTEXT_TRACE_ID)
                    .orElse(null)
                    .toMono()
            }
            .switchIfEmpty { Mono.empty() }

    val locale: Mono<Locale>
        get() = currentExchange
            .mapNotNull { it.locale() }
            .defaultIfEmpty(Locale.getDefault())

    val language: Mono<String>
        get() = locale.map { it.language }

    val timeZone: Mono<TimeZone>
        get() = currentExchange
            .map { it.timeZone() }
            .defaultIfEmpty(TimeZone.getDefault())

    val zoneId: Mono<ZoneId>
        get() = timeZone.map { it.toZoneId() }
}
package com.leijendary.spring.webflux.template.core.util

import com.leijendary.spring.webflux.template.core.extension.locale
import com.leijendary.spring.webflux.template.core.extension.timeZone
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers.boundedElastic
import reactor.kotlin.core.publisher.toMono
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

const val HEADER_USER_ID = "X-User-ID"
const val HEADER_SCOPE = "X-Scope"

object RequestContext {
    suspend fun exchange(): ServerWebExchange? = Mono
        .deferContextual {
            it.getOrEmpty<ServerWebExchange>(ServerWebExchange::class.java)
                .orElse(null)
                .toMono()
        }
        .subscribeOn(boundedElastic())
        .awaitSingle()

    suspend fun request(): ServerHttpRequest? = exchange()?.request

    suspend fun uri(): URI? = request()?.uri

    suspend fun locale(): Locale = exchange()?.locale() ?: Locale.getDefault()

    suspend fun language(): String = locale().language

    suspend fun timeZone(): TimeZone = exchange()?.timeZone() ?: TimeZone.getDefault()

    suspend fun zoneId(): ZoneId = timeZone().toZoneId()

    suspend fun now(): LocalDateTime = LocalDateTime.now(zoneId())
}
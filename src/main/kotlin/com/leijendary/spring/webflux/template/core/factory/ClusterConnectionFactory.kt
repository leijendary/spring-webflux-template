package com.leijendary.spring.webflux.template.core.factory

import com.leijendary.spring.webflux.template.core.extension.logger
import com.leijendary.spring.webflux.template.core.factory.ClusterConnectionFactory.ConnectionMode.READ_ONLY
import com.leijendary.spring.webflux.template.core.factory.ClusterConnectionFactory.ConnectionMode.READ_WRITE
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.context.Context

private const val CONTEXT_KEY = "CONNECTION_TYPE"

class ClusterConnectionFactory : AbstractRoutingConnectionFactory() {
    private val log = logger()

    companion object {
        fun readOnlyContext(context: Context): Context {
            return context.put(CONTEXT_KEY, READ_ONLY)
        }
    }

    enum class ConnectionMode {
        READ_WRITE,
        READ_ONLY
    }

    override fun determineCurrentLookupKey(): Mono<Any> {
        return Mono
            .deferContextual { Mono.just(it) }
            .filter { it.hasKey(CONTEXT_KEY) }
            .mapNotNull {
                it.getOrDefault(CONTEXT_KEY, READ_WRITE)
                    .toMono()
                    .doOnNext { mode -> log.debug("Database connection mode use: $mode") }
            }
    }
}
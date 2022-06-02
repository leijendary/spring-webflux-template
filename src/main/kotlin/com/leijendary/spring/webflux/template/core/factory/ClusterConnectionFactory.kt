package com.leijendary.spring.webflux.template.core.factory

import com.leijendary.spring.webflux.template.core.extension.logger
import com.leijendary.spring.webflux.template.core.factory.ClusterConnectionFactory.ConnectionMode.READ_ONLY
import com.leijendary.spring.webflux.template.core.factory.ClusterConnectionFactory.ConnectionMode.READ_WRITE
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory
import reactor.core.publisher.Mono
import reactor.util.context.Context

class ClusterConnectionFactory : AbstractRoutingConnectionFactory() {
    private val log = logger()

    companion object {
        fun readOnlyContext(context: Context): Context {
            return context.put(ConnectionMode::class.java, READ_ONLY)
        }
    }

    enum class ConnectionMode {
        READ_WRITE,
        READ_ONLY
    }

    override fun determineCurrentLookupKey(): Mono<Any> {
        return Mono
            .deferContextual { Mono.just(it) }
            .mapNotNull { context ->
                context
                    .getOrDefault(ConnectionMode::class.java, READ_WRITE)
                    .also { log.debug("Database connection mode in use: $it") }
            }
    }
}
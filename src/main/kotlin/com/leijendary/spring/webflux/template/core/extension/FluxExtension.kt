package com.leijendary.spring.webflux.template.core.extension

import com.leijendary.spring.webflux.template.core.factory.ClusterConnectionFactory
import reactor.core.publisher.Flux

fun <T> Flux<T>.readOnlyContext(): Flux<T> {
    return this.contextWrite { ClusterConnectionFactory.readOnlyContext(it) }
}
package com.leijendary.spring.webflux.template.core.extension

import com.leijendary.spring.webflux.template.core.factory.ClusterConnectionFactory
import reactor.core.publisher.Mono

fun <T> Mono<T>.readOnlyContext(): Mono<T> {
    return this.contextWrite { ClusterConnectionFactory.readOnlyContext(it) }
}
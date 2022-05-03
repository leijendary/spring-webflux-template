package com.leijendary.spring.webflux.template.core.util

import kotlinx.coroutines.reactor.awaitSingle
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers.boundedElastic
import java.util.*
import java.util.UUID.randomUUID

object ReactiveUUID {
    suspend fun v4(): UUID = Mono
        .fromCallable { randomUUID() }
        .subscribeOn(boundedElastic())
        .awaitSingle()
}
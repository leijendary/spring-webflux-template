package com.leijendary.spring.webflux.template.core.extension

import com.leijendary.spring.webflux.template.core.util.EmitHandler.failureHandler
import reactor.core.publisher.Sinks

fun <T> Sinks.Many<T>.emit(t: T) {
    this.emitNext(t!!, failureHandler())
}
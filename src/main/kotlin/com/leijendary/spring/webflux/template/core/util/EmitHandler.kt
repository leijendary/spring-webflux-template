package com.leijendary.spring.webflux.template.core.util

import com.leijendary.spring.webflux.template.core.extension.logger
import reactor.core.publisher.SignalType
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.EmitFailureHandler

object EmitHandler {
    private val log = logger()

    val emitFailureHandler = EmitFailureHandler { signalType: SignalType, emitResult: Sinks.EmitResult ->
        val isFailure = emitResult.isFailure

        if (isFailure) {
            log.warn("Sink emission failure signal type {} and result {}. Retrying...", signalType, emitResult)
        }

        isFailure
    }
}
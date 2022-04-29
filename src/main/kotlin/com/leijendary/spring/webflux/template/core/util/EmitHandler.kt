package com.leijendary.spring.webflux.template.core.util

import com.leijendary.spring.webflux.template.core.extension.logger
import reactor.core.publisher.SignalType
import reactor.core.publisher.Sinks.EmitFailureHandler
import reactor.core.publisher.Sinks.EmitResult
import reactor.core.publisher.Sinks.EmitResult.FAIL_NON_SERIALIZED

object EmitHandler {
    private val log = logger()

    val emitFailureHandler = EmitFailureHandler { signalType: SignalType, emitResult: EmitResult ->
        log.warn("Sink emission failure signal type {} and result {}", signalType, emitResult)

        emitResult == FAIL_NON_SERIALIZED
    }
}
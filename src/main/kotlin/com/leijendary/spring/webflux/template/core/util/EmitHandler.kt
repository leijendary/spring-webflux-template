package com.leijendary.spring.webflux.template.core.util

import reactor.core.publisher.Sinks.EmitFailureHandler
import reactor.core.publisher.Sinks.EmitResult.FAIL_NON_SERIALIZED

object EmitHandler {
    val emitFailureHandler = EmitFailureHandler { _, emitResult -> emitResult == FAIL_NON_SERIALIZED }
}
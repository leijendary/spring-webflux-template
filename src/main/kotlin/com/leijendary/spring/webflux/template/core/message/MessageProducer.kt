package com.leijendary.spring.webflux.template.core.message

import com.leijendary.spring.webflux.template.core.extension.logger
import org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.SignalType
import reactor.core.publisher.Sinks.EmitFailureHandler
import reactor.core.publisher.Sinks.EmitResult

abstract class MessageProducer<V> {
    private val log = logger()

    val failureHandler = EmitFailureHandler { signalType: SignalType, emitResult: EmitResult ->
        val isFailure = emitResult.isFailure

        if (isFailure) {
            log.warn("Sink emission failure signal type {} and result {}. Retrying...", signalType, emitResult)
        }

        isFailure
    }

    fun messageWithKey(key: String, value: V): Message<V> {
        return MessageBuilder
            .withPayload(value!!)
            .setHeader(MESSAGE_KEY, key)
            .build()
    }

    fun message(value: V): Message<V> {
        return MessageBuilder
            .withPayload(value!!)
            .build()
    }
}
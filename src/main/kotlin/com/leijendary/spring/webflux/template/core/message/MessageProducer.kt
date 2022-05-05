package com.leijendary.spring.webflux.template.core.message

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers.boundedElastic

abstract class MessageProducer<V> {
    suspend fun message(value: V): Message<V> {
        return message(null, value)
    }

    suspend fun message(key: String?, value: V): Message<V> {
        return Mono
            .fromCallable {
                var builder = MessageBuilder.withPayload(value!!)

                key?.let {
                    builder = builder.setHeader(MESSAGE_KEY, it.toByteArray())
                }

                builder.build()
            }
            .subscribeOn(boundedElastic())
            .awaitSingle()
    }
}
package com.leijendary.spring.webflux.template.core.message

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers.boundedElastic
import java.util.concurrent.Callable

abstract class MessageProducer<V> {
    suspend fun message(key: String?, value: V): Message<V> {
        val callable = Callable<Message<V>> {
            var builder = MessageBuilder.withPayload(value!!)

            if (key != null) {
                builder = builder.setHeader(MESSAGE_KEY, key)
            }

            builder.build()
        }

        return Mono
            .fromCallable(callable)
            .subscribeOn(boundedElastic())
            .awaitSingle()
    }

    suspend fun message(value: V): Message<V> {
        return message(null, value)
    }
}
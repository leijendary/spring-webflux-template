package com.leijendary.spring.webflux.template.core.message

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder

abstract class MessageProducer<V> {
    suspend fun messageWithKey(key: String, value: V): Message<V> {
        return mono {
            MessageBuilder
                .withPayload(value!!)
                .setHeader(MESSAGE_KEY, key)
                .build()
        }.awaitSingle()
    }

    suspend fun message(value: V): Message<V> {
        return mono {
            MessageBuilder
                .withPayload(value!!)
                .build()
        }.awaitSingle()
    }
}
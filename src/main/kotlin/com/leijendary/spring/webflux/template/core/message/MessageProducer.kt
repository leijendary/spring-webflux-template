package com.leijendary.spring.webflux.template.core.message

import org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder

abstract class MessageProducer<V> {
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
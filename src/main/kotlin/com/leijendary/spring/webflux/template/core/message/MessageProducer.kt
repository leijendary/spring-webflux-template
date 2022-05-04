package com.leijendary.spring.webflux.template.core.message

import org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder

abstract class MessageProducer<V> {
    fun message(key: String?, value: V): Message<V> {
        var builder = MessageBuilder.withPayload(value!!)

        if (key != null) {
            builder = builder.setHeader(MESSAGE_KEY, key)
        }

        return builder.build()
    }

    fun message(value: V): Message<V> {
        return message(null, value)
    }
}
package com.leijendary.spring.webflux.template.message

import com.leijendary.spring.webflux.template.core.extension.emit
import com.leijendary.spring.webflux.template.core.message.MessageProducer
import com.leijendary.spring.webflux.template.data.SampleMessage
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks.many
import java.util.function.Supplier

@Component
class SampleMessageProducer : MessageProducer<SampleMessage>() {
    private val createBuffer = many().multicast().onBackpressureBuffer<Message<SampleMessage>>()
    private val updateBuffer = many().multicast().onBackpressureBuffer<Message<SampleMessage>>()
    private val deleteBuffer = many().multicast().onBackpressureBuffer<Message<SampleMessage>>()

    @Bean
    fun sampleCreate(): Supplier<Flux<Message<SampleMessage>>> {
        return Supplier<Flux<Message<SampleMessage>>> {
            createBuffer.asFlux()
        }
    }

    @Bean
    fun sampleUpdate(): Supplier<Flux<Message<SampleMessage>>> {
        return Supplier<Flux<Message<SampleMessage>>> {
            updateBuffer.asFlux()
        }
    }

    @Bean
    fun sampleDelete(): Supplier<Flux<Message<SampleMessage>>> {
        return Supplier<Flux<Message<SampleMessage>>> {
            deleteBuffer.asFlux()
        }
    }

    suspend fun create(sampleMessage: SampleMessage) {
        val message = message(sampleMessage)

        createBuffer.emit(message)
    }

    suspend fun update(sampleMessage: SampleMessage) {
        val message = message(sampleMessage)

        updateBuffer.emit(message)
    }

    suspend fun delete(sampleMessage: SampleMessage) {
        val message = message(sampleMessage)

        deleteBuffer.emit(message)
    }
}
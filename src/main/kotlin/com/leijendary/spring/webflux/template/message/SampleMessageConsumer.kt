package com.leijendary.spring.webflux.template.message

import com.leijendary.spring.webflux.template.core.extension.logger
import com.leijendary.spring.webflux.template.data.SampleMessage
import org.apache.kafka.streams.kstream.KStream
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class SampleMessageConsumer {
    private val log = logger()

    @Bean
    fun sampleCreated(): Consumer<KStream<String?, SampleMessage>> {
        return Consumer<KStream<String?, SampleMessage>> { stream: KStream<String?, SampleMessage> ->
            stream.foreach { _: String?, _: SampleMessage -> }
        }
    }

    @Bean
    fun sampleUpdated(): Consumer<KStream<String?, SampleMessage>> {
        return Consumer<KStream<String?, SampleMessage>> { stream: KStream<String?, SampleMessage> ->
            stream.foreach { _: String?, _: SampleMessage -> }
        }
    }

    @Bean
    fun sampleDeleted(): Consumer<KStream<String?, SampleMessage>> {
        return Consumer<KStream<String?, SampleMessage>> { stream: KStream<String?, SampleMessage> ->
            stream.foreach { _: String?, _: SampleMessage -> }
        }
    }
}
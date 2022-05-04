package com.leijendary.spring.webflux.template.message

import com.leijendary.spring.webflux.template.core.extension.AnyUtil.toJson
import com.leijendary.spring.webflux.template.core.extension.logger
import com.leijendary.spring.webflux.template.data.SampleMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
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
            stream.foreach { key: String?, value: SampleMessage ->
                CoroutineScope(Default).launch {
                    log.info("Created: {}, {}", key, value.toJson())
                }
            }
        }
    }

    @Bean
    fun sampleUpdated(): Consumer<KStream<String?, SampleMessage>> {
        return Consumer<KStream<String?, SampleMessage>> { stream: KStream<String?, SampleMessage> ->
            stream.foreach { key: String?, value: SampleMessage ->
                CoroutineScope(Default).launch {
                    log.info("Updated: {}, {}", key, value.toJson())
                }
            }
        }
    }

    @Bean
    fun sampleDeleted(): Consumer<KStream<String?, SampleMessage>> {
        return Consumer<KStream<String?, SampleMessage>> { stream: KStream<String?, SampleMessage> ->
            stream.foreach { key: String?, value: SampleMessage ->
                CoroutineScope(Default).launch {
                    log.info("Deleted: {}, {}", key, value.toJson())
                }
            }
        }
    }
}
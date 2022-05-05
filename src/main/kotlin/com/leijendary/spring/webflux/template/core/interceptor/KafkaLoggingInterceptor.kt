package com.leijendary.spring.webflux.template.core.interceptor

import com.leijendary.spring.webflux.template.core.extension.logger
import org.apache.kafka.clients.consumer.ConsumerInterceptor
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.ProducerInterceptor
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.springframework.stereotype.Component

@Component
class KafkaLoggingInterceptor : ProducerInterceptor<String, Any>, ConsumerInterceptor<String, Any> {
    private val log = logger()

    override fun onSend(record: ProducerRecord<String, Any>): ProducerRecord<String, Any> {
        val topic = record.topic()
        val partition = record.partition()
        val key = record.key()
        val payload = String(record.value() as ByteArray)

        log.info("Sent to topic '$topic' on partition '$partition' with key '$key' and payload '$payload'")

        return record
    }

    override fun onConsume(records: ConsumerRecords<String, Any>): ConsumerRecords<String, Any> {
        records.forEach {
            val topic = it.topic()
            val partition = it.partition()
            val key = it.key()
            val payload = String(it.value() as ByteArray)

            log.info("Received from topic '$topic' on partition '$partition' with key '$key' and payload '$payload'")
        }

        return records
    }

    override fun configure(configs: MutableMap<String, *>) {}

    override fun onAcknowledgement(metadata: RecordMetadata, exception: Exception?) {}

    override fun onCommit(offsets: MutableMap<TopicPartition, OffsetAndMetadata>) {}

    override fun close() {}
}
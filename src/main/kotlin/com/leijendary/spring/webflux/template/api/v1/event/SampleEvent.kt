package com.leijendary.spring.webflux.template.api.v1.event

import com.leijendary.spring.webflux.template.api.v1.data.CACHE_KEY
import com.leijendary.spring.webflux.template.api.v1.data.SampleResponse
import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.api.v1.search.SampleSearch
import com.leijendary.spring.webflux.template.core.cache.ReactiveRedisCache
import com.leijendary.spring.webflux.template.core.extension.emit
import com.leijendary.spring.webflux.template.core.extension.logger
import com.leijendary.spring.webflux.template.message.SampleMessageProducer
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import reactor.core.publisher.Sinks.many
import reactor.core.scheduler.Schedulers.boundedElastic

@Component
class SampleEvent(
    private val reactiveRedisCache: ReactiveRedisCache,
    private val sampleMessageProducer: SampleMessageProducer,
    private val sampleSearch: SampleSearch,
) {
    private val log = logger()
    private val createBuffer = many().multicast().onBackpressureBuffer<SampleResponse>()
    private val updateBuffer = many().multicast().onBackpressureBuffer<SampleResponse>()
    private val deleteBuffer = many().multicast().onBackpressureBuffer<SampleResponse>()

    companion object {
        private val MAPPER: SampleMapper = SampleMapper.INSTANCE
    }

    init {
        createBuffer
            .asFlux()
            .flatMap { mono { createConsumer(it) } }
            .onErrorContinue { t, _ -> errorConsumer(t) }
            .subscribeOn(boundedElastic())
            .subscribe()

        updateBuffer
            .asFlux()
            .flatMap { mono { updateConsumer(it) } }
            .onErrorContinue { t, _ -> errorConsumer(t) }
            .subscribeOn(boundedElastic())
            .subscribe()

        deleteBuffer
            .asFlux()
            .flatMap { mono { deleteConsumer(it) } }
            .onErrorContinue { t, _ -> errorConsumer(t) }
            .subscribeOn(boundedElastic())
            .subscribe()
    }

    suspend fun create(sampleResponse: SampleResponse) {
        createBuffer.emit(sampleResponse)
    }

    suspend fun update(sampleResponse: SampleResponse) {
        updateBuffer.emit(sampleResponse)
    }

    suspend fun delete(sampleResponse: SampleResponse) {
        deleteBuffer.emit(sampleResponse)
    }

    private suspend fun createConsumer(sampleResponse: SampleResponse) {
        val id = sampleResponse.id

        reactiveRedisCache.set("$CACHE_KEY:$id", sampleResponse)

        val message = MAPPER.toMessage(sampleResponse)

        sampleMessageProducer.create(message)

        sampleSearch.save(sampleResponse)
    }

    private suspend fun updateConsumer(sampleResponse: SampleResponse) {
        val id = sampleResponse.id

        reactiveRedisCache.set("$CACHE_KEY:$id", sampleResponse)

        val message = MAPPER.toMessage(sampleResponse)

        sampleMessageProducer.update(message)

        sampleSearch.update(sampleResponse)
    }

    private suspend fun deleteConsumer(sampleResponse: SampleResponse) {
        val id = sampleResponse.id

        reactiveRedisCache.delete("$CACHE_KEY:$id")

        val message = MAPPER.toMessage(sampleResponse)

        sampleMessageProducer.delete(message)

        sampleSearch.delete(id)
    }

    private fun errorConsumer(t: Throwable) = log.error("Event threw an exception", t)
}
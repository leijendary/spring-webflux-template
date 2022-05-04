package com.leijendary.spring.webflux.template.api.v1.event

import com.leijendary.spring.webflux.template.api.v1.data.CACHE_KEY
import com.leijendary.spring.webflux.template.api.v1.data.SampleResponse
import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.api.v1.search.SampleSearch
import com.leijendary.spring.webflux.template.core.cache.ReactiveRedisCache
import com.leijendary.spring.webflux.template.core.util.EmitHandler.emitFailureHandler
import com.leijendary.spring.webflux.template.message.SampleMessageProducer
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks.many
import reactor.core.scheduler.Schedulers.boundedElastic

@Component
class SampleEvent(
    private val reactiveRedisCache: ReactiveRedisCache,
    private val sampleMessageProducer: SampleMessageProducer,
    private val sampleSearch: SampleSearch,
) {
    private val createBuffer = many().multicast().onBackpressureBuffer<SampleResponse>()
    private val updateBuffer = many().multicast().onBackpressureBuffer<SampleResponse>()
    private val deleteBuffer = many().multicast().onBackpressureBuffer<SampleResponse>()

    companion object {
        private val MAPPER: SampleMapper = SampleMapper.INSTANCE
    }

    init {
        createBuffer
            .asFlux()
            .subscribeOn(boundedElastic())
            .flatMap { mono { createConsumer(it) } }
            .subscribe()

        updateBuffer
            .asFlux()
            .subscribeOn(boundedElastic())
            .flatMap { mono { updateConsumer(it) } }
            .subscribe()

        deleteBuffer
            .asFlux()
            .subscribeOn(boundedElastic())
            .flatMap { mono { deleteConsumer(it) } }
            .subscribe()
    }

    suspend fun create(sampleResponse: SampleResponse) {
        createBuffer.emitNext(sampleResponse, emitFailureHandler)
    }

    suspend fun update(sampleResponse: SampleResponse) {
        updateBuffer.emitNext(sampleResponse, emitFailureHandler)
    }

    suspend fun delete(sampleResponse: SampleResponse) {
        deleteBuffer.emitNext(sampleResponse, emitFailureHandler)
    }

    private suspend fun createConsumer(sampleResponse: SampleResponse) {
        val id = sampleResponse.id

        reactiveRedisCache.set("$CACHE_KEY:$id", sampleResponse)

        val message = Mono
            .just(MAPPER.toMessage(sampleResponse))
            .subscribeOn(boundedElastic())
            .awaitSingle()

        sampleMessageProducer.create(message)

        sampleSearch.save(sampleResponse)
    }

    private suspend fun updateConsumer(sampleResponse: SampleResponse) {
        val id = sampleResponse.id

        reactiveRedisCache.set("$CACHE_KEY:$id", sampleResponse)

        val message = Mono
            .just(MAPPER.toMessage(sampleResponse))
            .subscribeOn(boundedElastic())
            .awaitSingle()

        sampleMessageProducer.update(message)

        sampleSearch.update(sampleResponse)
    }

    private suspend fun deleteConsumer(sampleResponse: SampleResponse) {
        val id = sampleResponse.id

        reactiveRedisCache.delete("$CACHE_KEY:$id")

        val message = Mono
            .just(MAPPER.toMessage(sampleResponse))
            .subscribeOn(boundedElastic())
            .awaitSingle()

        sampleMessageProducer.delete(message)

        sampleSearch.delete(id)
    }
}
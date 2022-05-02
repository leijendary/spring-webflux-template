package com.leijendary.spring.webflux.template.event

import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.api.v1.search.SampleSearch
import com.leijendary.spring.webflux.template.core.cache.ReactiveRedisCache
import com.leijendary.spring.webflux.template.core.util.EmitHandler.emitFailureHandler
import com.leijendary.spring.webflux.template.entity.CACHE_KEY
import com.leijendary.spring.webflux.template.entity.SampleTable
import com.leijendary.spring.webflux.template.message.SampleMessageProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import reactor.core.publisher.Sinks.many
import reactor.core.scheduler.Schedulers.boundedElastic

@Component
class SampleEvent(
    private val reactiveRedisCache: ReactiveRedisCache,
    private val sampleMessageProducer: SampleMessageProducer,
    private val sampleSearch: SampleSearch,
) {
    private val createBuffer = many().multicast().onBackpressureBuffer<SampleTable>()
    private val updateBuffer = many().multicast().onBackpressureBuffer<SampleTable>()
    private val deleteBuffer = many().multicast().onBackpressureBuffer<SampleTable>()

    companion object {
        private val MAPPER: SampleMapper = SampleMapper.INSTANCE
    }

    init {
        createBuffer
            .asFlux()
            .subscribeOn(boundedElastic())
            .subscribe {
                CoroutineScope(Unconfined).launch {
                    createConsumer(it)
                }
            }

        updateBuffer
            .asFlux()
            .subscribeOn(boundedElastic())
            .subscribe {
                CoroutineScope(Unconfined).launch {
                    updateConsumer(it)
                }
            }

        deleteBuffer
            .asFlux()
            .subscribeOn(boundedElastic())
            .subscribe {
                CoroutineScope(Unconfined).launch {
                    deleteConsumer(it)
                }
            }
    }

    suspend fun create(sampleTable: SampleTable) {
        createBuffer.emitNext(sampleTable, emitFailureHandler)
    }

    suspend fun update(sampleTable: SampleTable) {
        updateBuffer.emitNext(sampleTable, emitFailureHandler)
    }

    suspend fun delete(sampleTable: SampleTable) {
        deleteBuffer.emitNext(sampleTable, emitFailureHandler)
    }

    private suspend fun createConsumer(sampleTable: SampleTable) {
        val id = sampleTable.id

        reactiveRedisCache.set("$CACHE_KEY$id", sampleTable)

        val message = MAPPER.toMessage(sampleTable)

        sampleMessageProducer.create(message)

        sampleSearch.save(sampleTable)
    }

    private suspend fun updateConsumer(sampleTable: SampleTable) {
        val id = sampleTable.id

        reactiveRedisCache.set("$CACHE_KEY$id", sampleTable)

        val message = MAPPER.toMessage(sampleTable)

        sampleMessageProducer.update(message)

        sampleSearch.update(sampleTable)
    }

    private suspend fun deleteConsumer(sampleTable: SampleTable) {
        val id = sampleTable.id

        reactiveRedisCache.delete("$CACHE_KEY$id")

        val message = MAPPER.toMessage(sampleTable)

        sampleMessageProducer.delete(message)

        sampleSearch.delete(id)
    }
}
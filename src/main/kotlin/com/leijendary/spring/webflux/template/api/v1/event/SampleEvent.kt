package com.leijendary.spring.webflux.template.api.v1.event

import com.leijendary.spring.webflux.template.api.v1.data.CACHE_KEY
import com.leijendary.spring.webflux.template.api.v1.data.SampleResponse
import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.api.v1.search.SampleSearch
import com.leijendary.spring.webflux.template.core.cache.ReactiveRedisCache
import com.leijendary.spring.webflux.template.message.SampleMessageProducer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class SampleEvent(
    private val reactiveRedisCache: ReactiveRedisCache,
    private val sampleMessageProducer: SampleMessageProducer,
    private val sampleSearch: SampleSearch,
) {
    companion object {
        private val MAPPER: SampleMapper = SampleMapper.INSTANCE
    }

    suspend fun create(sampleResponse: SampleResponse) = coroutineScope {
        val id = sampleResponse.id

        launch {
            reactiveRedisCache.set("$CACHE_KEY:$id", sampleResponse)

            val sampleMessage = MAPPER.toMessage(sampleResponse)

            sampleMessageProducer.create(sampleMessage)

            sampleSearch.save(sampleResponse)
        }
    }

    suspend fun update(sampleResponse: SampleResponse) = coroutineScope {
        val id = sampleResponse.id

        launch {
            reactiveRedisCache.set("$CACHE_KEY:$id", sampleResponse)

            val sampleMessage = MAPPER.toMessage(sampleResponse)

            sampleMessageProducer.update(sampleMessage)

            sampleSearch.update(sampleResponse)
        }
    }

    suspend fun delete(sampleResponse: SampleResponse) = coroutineScope {
        val id = sampleResponse.id

        launch {
            reactiveRedisCache.delete("$CACHE_KEY:$id")

            val sampleMessage = MAPPER.toMessage(sampleResponse)

            sampleMessageProducer.delete(sampleMessage)

            sampleSearch.delete(id)
        }
    }
}
package com.leijendary.spring.webflux.template.api.v1.event

import com.leijendary.spring.webflux.template.api.v1.data.CACHE_KEY
import com.leijendary.spring.webflux.template.api.v1.data.SampleResponse
import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.api.v1.search.SampleSearch
import com.leijendary.spring.webflux.template.core.cache.ReactiveRedisCache
import com.leijendary.spring.webflux.template.message.SampleMessageProducer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
        val cache = async {
            reactiveRedisCache.set("$CACHE_KEY$id", sampleResponse)
        }
        val kafka = async {
            val message = MAPPER.toMessage(sampleResponse)

            sampleMessageProducer.create(message)
        }
        val search = async {
            sampleSearch.save(sampleResponse)
        }

        awaitAll(cache, kafka, search)
    }

    suspend fun update(sampleResponse: SampleResponse) = coroutineScope {
        val id = sampleResponse.id
        val cache = async {
            reactiveRedisCache.set("$CACHE_KEY$id", sampleResponse)
        }
        val search = async {
            sampleSearch.update(sampleResponse)
        }
        val kafka = async {
            val message = MAPPER.toMessage(sampleResponse)

            sampleMessageProducer.update(message)
        }

        awaitAll(cache, search, kafka)
    }

    suspend fun delete(sampleResponse: SampleResponse) = coroutineScope {
        val id = sampleResponse.id
        val cache = async {
            reactiveRedisCache.delete("$CACHE_KEY$id")
        }
        val kafka = async {
            val message = MAPPER.toMessage(sampleResponse)

            sampleMessageProducer.delete(message)
        }
        val search = async {
            sampleSearch.delete(id)
        }

        awaitAll(cache, kafka, search)
    }
}
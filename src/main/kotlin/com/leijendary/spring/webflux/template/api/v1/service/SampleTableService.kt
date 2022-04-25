package com.leijendary.spring.webflux.template.api.v1.service

import com.leijendary.spring.webflux.template.api.v1.data.CACHE_KEY
import com.leijendary.spring.webflux.template.api.v1.data.SampleListResponse
import com.leijendary.spring.webflux.template.api.v1.data.SampleRequest
import com.leijendary.spring.webflux.template.api.v1.data.SampleResponse
import com.leijendary.spring.webflux.template.api.v1.event.SampleEvent
import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.api.v1.search.SampleSearch
import com.leijendary.spring.webflux.template.core.cache.ReactiveRedisCache
import com.leijendary.spring.webflux.template.core.data.Seek
import com.leijendary.spring.webflux.template.core.data.Seekable
import com.leijendary.spring.webflux.template.core.exception.ResourceNotFoundException
import com.leijendary.spring.webflux.template.repository.SampleTableRepository
import com.leijendary.spring.webflux.template.repository.SampleTableTranslationRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

@Service
class SampleTableService(
    private val reactiveRedisCache: ReactiveRedisCache,
    private val sampleEvent: SampleEvent,
    private val sampleSearch: SampleSearch,
    private val sampleTableRepository: SampleTableRepository,
    private val sampleTableTranslationRepository: SampleTableTranslationRepository,
    private val transactionalOperator: TransactionalOperator
) {
    companion object {
        private val MAPPER: SampleMapper = SampleMapper.INSTANCE
        private val SOURCE = listOf("data", "SampleTable", "id")
    }

    suspend fun seek(query: String, seekable: Seekable): Seek<SampleListResponse> {
        return sampleTableRepository
            .seek(query, seekable)
            .transform { MAPPER.toListResponse(it) }
    }

    suspend fun create(sampleRequest: SampleRequest): SampleResponse {
        var sampleTable = MAPPER.toEntity(sampleRequest)
        var sampleTableTranslations = MAPPER.toEntity(sampleRequest.translations!!)

        transactionalOperator.executeAndAwait {
            sampleTable = sampleTableRepository.save(sampleTable)
            sampleTableTranslations = sampleTableTranslationRepository
                .save(sampleTable.id, sampleTableTranslations)
                .toSet(mutableSetOf())
        }

        val response = MAPPER.toResponse(sampleTable)
        response.translations = MAPPER.toResponse(sampleTableTranslations)

        sampleEvent.create(response)

        return response
    }

    suspend fun get(id: UUID): SampleResponse {
        val key = "$CACHE_KEY$id"
        val cache = reactiveRedisCache.get(key, SampleResponse::class)

        if (cache != null) {
            return cache
        }

        val sampleTable = sampleTableRepository.get(id) ?: throw ResourceNotFoundException(SOURCE, id)
        val sampleTableTranslations = sampleTableTranslationRepository
            .get(id)
            .toSet(mutableSetOf())

        val response = MAPPER.toResponse(sampleTable)
        response.translations = MAPPER.toResponse(sampleTableTranslations)

        this.reactiveRedisCache.set(key, response)

        return response
    }

    suspend fun update(id: UUID, sampleRequest: SampleRequest): SampleResponse {
        var sampleTable = sampleTableRepository.get(id) ?: throw ResourceNotFoundException(SOURCE, id)

        MAPPER.update(sampleRequest, sampleTable)

        var sampleTableTranslations = MAPPER.toEntity(sampleRequest.translations!!)
        val oldTranslations = sampleTableTranslationRepository
            .get(id)
            .toSet(mutableSetOf())

        transactionalOperator.executeAndAwait {
            sampleTable = sampleTableRepository.update(sampleTable)
            sampleTableTranslations = sampleTableTranslationRepository
                .update(id, oldTranslations, sampleTableTranslations)
                .toSet(mutableSetOf())
        }

        val response = MAPPER.toResponse(sampleTable)
        response.translations = MAPPER.toResponse(sampleTableTranslations)

        sampleEvent.update(response)

        return response
    }

    suspend fun delete(id: UUID) = coroutineScope {
        val sampleTable = sampleTableRepository.get(id) ?: throw ResourceNotFoundException(SOURCE, id)

        sampleTableRepository.delete(id)

        val sampleTableTranslations = sampleTableTranslationRepository
            .get(id)
            .toSet(mutableSetOf())

        val response = MAPPER.toResponse(sampleTable)
        response.translations = MAPPER.toResponse(sampleTableTranslations)

        sampleEvent.delete(response)
    }

    suspend fun reindex(): Int {
        var count = 0;

        sampleTableRepository
            .all()
            .collect { list ->
                val responses = list.map {
                    val translations = sampleTableTranslationRepository
                        .get(it.id)
                        .toSet(mutableSetOf())

                    val response = MAPPER.toResponse(it)
                    response.translations = MAPPER.toResponse(translations)

                    count++

                    response
                }

                sampleSearch
                    .save(responses)
                    .collect()
            }

        return count
    }
}
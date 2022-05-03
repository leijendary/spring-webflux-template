package com.leijendary.spring.webflux.template.api.v1.service

import com.leijendary.spring.webflux.template.api.v1.data.CACHE_KEY
import com.leijendary.spring.webflux.template.api.v1.data.SampleListResponse
import com.leijendary.spring.webflux.template.api.v1.data.SampleRequest
import com.leijendary.spring.webflux.template.api.v1.data.SampleResponse
import com.leijendary.spring.webflux.template.api.v1.event.SampleEvent
import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.api.v1.search.SampleSearch
import com.leijendary.spring.webflux.template.core.cache.ReactiveRedisCache
import com.leijendary.spring.webflux.template.core.config.properties.R2dbcBatchProperties
import com.leijendary.spring.webflux.template.core.data.Seek
import com.leijendary.spring.webflux.template.core.data.Seekable
import com.leijendary.spring.webflux.template.core.exception.ResourceNotFoundException
import com.leijendary.spring.webflux.template.core.factory.ClusterConnectionFactory.Companion.readOnlyContext
import com.leijendary.spring.webflux.template.core.factory.SeekFactory
import com.leijendary.spring.webflux.template.core.util.ReactiveUUID
import com.leijendary.spring.webflux.template.repository.SampleTableRepository
import com.leijendary.spring.webflux.template.repository.SampleTableTranslationRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import reactor.core.scheduler.Schedulers.parallel
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.*

@Service
class SampleTableService(
    private val reactiveRedisCache: ReactiveRedisCache,
    private val r2dbcBatchProperties: R2dbcBatchProperties,
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

    suspend fun seek(query: String? = "", seekable: Seekable): Seek<SampleListResponse> {
        val flux = SeekFactory
            .from(seekable)
            ?.let { sampleTableRepository.seek(query, it.createdAt, it.rowId, seekable.limit) }
            ?: sampleTableRepository.query(query, seekable.limit)

        return flux
            .contextWrite { readOnlyContext(it) }
            .asFlow()
            .toList(mutableListOf())
            .let { SeekFactory.create(it, seekable) }
            .transform { MAPPER.toListResponse(it) }
    }

    suspend fun create(sampleRequest: SampleRequest): SampleResponse {
        var sampleTable = MAPPER.toEntity(sampleRequest)
        var sampleTableTranslations = MAPPER.toEntity(sampleRequest.translations!!)
        val id = ReactiveUUID.v4()

        sampleTable.id = id

        transactionalOperator.executeAndAwait {
            sampleTable = sampleTableRepository
                .save(sampleTable)
                .awaitSingle()
            sampleTableTranslations = sampleTableTranslationRepository
                .save(id, sampleTableTranslations)
                .asFlow()
                .toSet(mutableSetOf())
        }

        val response = MAPPER.toResponse(sampleTable)
        response.translations = MAPPER.toResponse(sampleTableTranslations)

        sampleEvent.create(response)

        return response
    }

    suspend fun get(id: UUID): SampleResponse {
        val key = "$CACHE_KEY:$id"
        val cache = reactiveRedisCache.get(key, SampleResponse::class)

        if (cache != null) {
            return cache
        }

        val sampleTable = sampleTableRepository
            .get(id)
            .contextWrite { readOnlyContext(it) }
            .switchIfEmpty { throw ResourceNotFoundException(SOURCE, id) }
            .awaitSingle()
        val sampleTableTranslations = sampleTableTranslationRepository
            .findByReferenceId(id)
            .contextWrite { readOnlyContext(it) }
            .asFlow()
            .toSet(mutableSetOf())

        val response = MAPPER.toResponse(sampleTable)
        response.translations = MAPPER.toResponse(sampleTableTranslations)

        this.reactiveRedisCache.set(key, response)

        return response
    }

    suspend fun update(id: UUID, sampleRequest: SampleRequest): SampleResponse {
        var sampleTable = sampleTableRepository
            .get(id)
            .contextWrite { readOnlyContext(it) }
            .switchIfEmpty { throw ResourceNotFoundException(SOURCE, id) }
            .awaitSingle()

        MAPPER.update(sampleRequest, sampleTable)

        var sampleTableTranslations = MAPPER.toEntity(sampleRequest.translations!!)
        val oldTranslations = sampleTableTranslationRepository
            .findByReferenceId(id)
            .contextWrite { readOnlyContext(it) }
            .asFlow()
            .toSet(mutableSetOf())

        transactionalOperator.executeAndAwait {
            sampleTable = sampleTableRepository
                .save(sampleTable)
                .awaitSingle()
            sampleTableTranslations = sampleTableTranslationRepository
                .save(id, oldTranslations, sampleTableTranslations)
                .asFlow()
                .toSet(mutableSetOf())
        }

        val response = MAPPER.toResponse(sampleTable)
        response.translations = MAPPER.toResponse(sampleTableTranslations)

        sampleEvent.update(response)

        return response
    }

    suspend fun delete(id: UUID) {
        val sampleTable = sampleTableRepository
            .get(id)
            .contextWrite { readOnlyContext(it) }
            .switchIfEmpty { throw ResourceNotFoundException(SOURCE, id) }
            .awaitSingle()

        sampleTableRepository
            .softDelete(sampleTable)
            .awaitSingle()

        val sampleTableTranslations = sampleTableTranslationRepository
            .findByReferenceId(id)
            .contextWrite { readOnlyContext(it) }
            .asFlow()
            .toSet(mutableSetOf())

        val response = MAPPER.toResponse(sampleTable)
        response.translations = MAPPER.toResponse(sampleTableTranslations)

        sampleEvent.delete(response)
    }

    suspend fun reindex(): Int {
        var count = 0

        sampleTableRepository
            .findAllByDeletedAtIsNull()
            .buffer(r2dbcBatchProperties.size)
            .onBackpressureBuffer()
            .parallel()
            .runOn(parallel())
            .collect { list ->
                val responses = list.map { sampleTable ->
                    val translations = sampleTableTranslationRepository
                        .findByReferenceId(sampleTable.id)
                        .contextWrite { readOnlyContext(it) }
                        .asFlow()
                        .toSet(mutableSetOf())

                    val response = MAPPER.toResponse(sampleTable)
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
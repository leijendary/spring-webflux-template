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
import com.leijendary.spring.webflux.template.core.extension.readOnlyContext
import com.leijendary.spring.webflux.template.core.factory.SeekFactory
import com.leijendary.spring.webflux.template.core.util.ReactiveUUID
import com.leijendary.spring.webflux.template.entity.SampleTableTranslation
import com.leijendary.spring.webflux.template.repository.SampleTableRepository
import com.leijendary.spring.webflux.template.repository.SampleTableTranslationRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import reactor.core.scheduler.Schedulers.boundedElastic
import reactor.core.scheduler.Schedulers.parallel
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

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
        val flow = SeekFactory
            .from(seekable)
            ?.let { sampleTableRepository.seek(query, it.createdAt, it.rowId, seekable.limit) }
            ?: sampleTableRepository.query(query, seekable.limit)

        return flow
            .asFlux()
            .readOnlyContext()
            .subscribeOn(boundedElastic())
            .asFlow()
            .toList(mutableListOf())
            .let { SeekFactory.create(it, seekable) }
            .transform { MAPPER.toListResponse(it) }
    }

    suspend fun create(sampleRequest: SampleRequest): SampleResponse {
        var sampleTable = MAPPER
            .toEntity(sampleRequest)
            .apply { id = ReactiveUUID.v4() }

        transactionalOperator.executeAndAwait {
            sampleTable = sampleTableRepository.save(sampleTable)
            sampleTable.translations = sampleTableTranslationRepository
                .save(sampleTable.id, sampleTable.translations)
                .toList(mutableListOf())
        }

        val response = MAPPER.toResponse(sampleTable)

        sampleEvent.create(response)

        return response
    }

    suspend fun get(id: UUID): SampleResponse {
        val key = "$CACHE_KEY:$id"
        val cache = reactiveRedisCache.get(key, SampleResponse::class)

        if (cache != null) {
            return cache
        }

        val sampleTable = mono { sampleTableRepository.get(id) }
            .readOnlyContext()
            .switchIfEmpty { throw ResourceNotFoundException(SOURCE, id) }
            .subscribeOn(boundedElastic())
            .awaitSingle()
            .apply { translations = getTranslations(this.id) }

        val response = MAPPER.toResponse(sampleTable)

        this.reactiveRedisCache.set(key, response)

        return response
    }

    suspend fun update(id: UUID, sampleRequest: SampleRequest): SampleResponse {
        var sampleTable = mono { sampleTableRepository.get(id) }
            .readOnlyContext()
            .switchIfEmpty { throw ResourceNotFoundException(SOURCE, id) }
            .subscribeOn(boundedElastic())
            .awaitSingle()
        val oldTranslations = getTranslations(id)

        MAPPER.update(sampleRequest, sampleTable)

        transactionalOperator.executeAndAwait {
            sampleTable = sampleTableRepository.save(sampleTable)
            sampleTable.translations = sampleTableTranslationRepository
                .save(id, oldTranslations, sampleTable.translations)
                .toList(mutableListOf())
        }

        val response = MAPPER.toResponse(sampleTable)

        sampleEvent.update(response)

        return response
    }

    suspend fun delete(id: UUID) {
        val sampleTable = mono { sampleTableRepository.get(id) }
            .readOnlyContext()
            .switchIfEmpty { throw ResourceNotFoundException(SOURCE, id) }
            .subscribeOn(boundedElastic())
            .awaitSingle()
            .apply { translations = getTranslations(this.id) }

        sampleTableRepository.softDelete(sampleTable)

        val response = MAPPER.toResponse(sampleTable)

        sampleEvent.delete(response)
    }

    suspend fun reindex(): Int {
        val count = AtomicInteger(0)

        sampleTableRepository
            .findAllByDeletedAtIsNull()
            .asFlux()
            .readOnlyContext()
            .buffer(r2dbcBatchProperties.size)
            .onBackpressureBuffer()
            .parallel()
            .runOn(parallel())
            .collect { list ->
                val responses = list.map { sampleTable ->
                    sampleTable.translations = getTranslations(sampleTable.id)

                    count.incrementAndGet()

                    MAPPER.toResponse(sampleTable)
                }

                sampleSearch
                    .save(responses)
                    .collect()
            }

        return count.get()
    }

    private suspend fun getTranslations(referenceId: UUID): List<SampleTableTranslation> {
        return sampleTableTranslationRepository
            .findByReferenceId(referenceId)
            .asFlux()
            .readOnlyContext()
            .subscribeOn(boundedElastic())
            .asFlow()
            .toList(mutableListOf())
    }
}
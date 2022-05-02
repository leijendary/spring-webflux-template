package com.leijendary.spring.webflux.template.api.v1.service

import com.leijendary.spring.webflux.template.api.v1.data.SampleRequest
import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.api.v1.search.SampleSearch
import com.leijendary.spring.webflux.template.core.cache.ReactiveRedisCache
import com.leijendary.spring.webflux.template.core.config.properties.R2dbcBatchProperties
import com.leijendary.spring.webflux.template.core.data.Seek
import com.leijendary.spring.webflux.template.core.data.Seekable
import com.leijendary.spring.webflux.template.core.exception.ResourceNotFoundException
import com.leijendary.spring.webflux.template.core.factory.ClusterConnectionFactory.Companion.readOnlyContext
import com.leijendary.spring.webflux.template.core.factory.SeekFactory
import com.leijendary.spring.webflux.template.core.validator.BindingValidator
import com.leijendary.spring.webflux.template.entity.CACHE_KEY
import com.leijendary.spring.webflux.template.entity.SampleTable
import com.leijendary.spring.webflux.template.event.SampleEvent
import com.leijendary.spring.webflux.template.repository.SampleTableRepository
import com.leijendary.spring.webflux.template.repository.SampleTableTranslationRepository
import kotlinx.coroutines.flow.collect
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
import java.util.UUID.randomUUID

@Service
class SampleService(
    private val bindingValidator: BindingValidator,
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

    suspend fun seek(query: String, seekable: Seekable): Seek<SampleTable> {
        val flux = SeekFactory
            .from(seekable)
            ?.let { sampleTableRepository.seek(query, it.createdAt, it.rowId, seekable.limit) }
            ?: sampleTableRepository.query(query, seekable.limit)

        return flux
            .contextWrite { readOnlyContext(it) }
            .collectList()
            .map { SeekFactory.create(it, seekable) }
            .awaitSingle()
    }

    suspend fun create(sampleRequest: SampleRequest): SampleTable {
        bindingValidator.validate(sampleRequest)

        var sampleTable = MAPPER.toEntity(sampleRequest)
        val translations = MAPPER.toEntity(sampleRequest.translations!!)
        val id = randomUUID()

        sampleTable.id = id

        transactionalOperator.executeAndAwait {
            sampleTable = sampleTableRepository
                .save(sampleTable)
                .awaitSingle()
            sampleTable.translations = sampleTableTranslationRepository
                .save(id, translations)
                .asFlow()
                .toSet(mutableSetOf())
        }

        sampleEvent.create(sampleTable)

        return sampleTable
    }

    suspend fun get(id: UUID): SampleTable {
        val key = "$CACHE_KEY$id"
        val cache = reactiveRedisCache.get(key, SampleTable::class)

        if (cache != null) {
            return cache
        }

        val sampleTable = sampleTableRepository
            .get(id)
            .contextWrite { readOnlyContext(it) }
            .switchIfEmpty { throw ResourceNotFoundException(SOURCE, id) }
            .awaitSingle()
        sampleTable.translations = sampleTableTranslationRepository
            .findByReferenceId(id)
            .contextWrite { readOnlyContext(it) }
            .asFlow()
            .toSet(mutableSetOf())

        this.reactiveRedisCache.set(key, sampleTable)

        return sampleTable
    }

    suspend fun update(id: UUID, sampleRequest: SampleRequest): SampleTable {
        bindingValidator.validate(sampleRequest)

        var sampleTable = sampleTableRepository
            .get(id)
            .contextWrite { readOnlyContext(it) }
            .switchIfEmpty { throw ResourceNotFoundException(SOURCE, id) }
            .awaitSingle()

        MAPPER.update(sampleRequest, sampleTable)

        val translations = sampleTable.translations
        val oldTranslations = sampleTableTranslationRepository
            .findByReferenceId(id)
            .contextWrite { readOnlyContext(it) }
            .asFlow()
            .toSet(mutableSetOf())

        transactionalOperator.executeAndAwait {
            sampleTable = sampleTableRepository
                .save(sampleTable)
                .awaitSingle()
            sampleTable.translations = sampleTableTranslationRepository
                .save(id, oldTranslations, translations)
                .asFlow()
                .toSet(mutableSetOf())
        }

        sampleEvent.update(sampleTable)

        return sampleTable
    }

    suspend fun delete(id: UUID) {
        val sampleTable = sampleTableRepository
            .get(id)
            .contextWrite { readOnlyContext(it) }
            .switchIfEmpty { throw ResourceNotFoundException(SOURCE, id) }
            .awaitSingle()
        sampleTable.translations = sampleTableTranslationRepository
            .findByReferenceId(id)
            .contextWrite { readOnlyContext(it) }
            .asFlow()
            .toSet(mutableSetOf())

        sampleTableRepository
            .softDelete(sampleTable)
            .awaitSingle()

        sampleEvent.delete(sampleTable)
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
                    sampleTable.translations = sampleTableTranslationRepository
                        .findByReferenceId(sampleTable.id)
                        .contextWrite { readOnlyContext(it) }
                        .asFlow()
                        .toSet(mutableSetOf())

                    count++

                    sampleTable
                }

                sampleSearch
                    .save(responses)
                    .collect()
            }

        return count
    }
}
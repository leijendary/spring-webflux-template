package com.leijendary.spring.webflux.template.core.repository

import com.leijendary.spring.webflux.template.core.entity.LocaleEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.scheduler.Schedulers.boundedElastic
import java.util.*

@Repository
class LocaleRepositoryImpl<T : LocaleEntity>(private val template: R2dbcEntityTemplate) : LocaleRepository<T> {
    override fun save(referenceId: UUID, translations: List<T>): Flow<T> {
        return insert(referenceId, translations)
    }

    override fun save(referenceId: UUID, oldTranslations: List<T>, newTranslations: List<T>): Flow<T> {
        val isolation = LocaleEntity.isolate(oldTranslations, newTranslations)
        val insert = insert(referenceId, isolation.creates)
        val update = update(isolation.updates)
        val delete = delete(isolation.deletes)

        return merge(insert, update)
            .onCompletion { delete.collect() }
    }

    private fun insert(referenceId: UUID, translations: List<T>): Flow<T> {
        return translations
            .asFlow()
            .map {
                it.referenceId = referenceId

                template
                    .insert(it)
                    .subscribeOn(boundedElastic())
                    .awaitSingle()
            }
    }

    private fun update(translations: List<T>): Flow<T> {
        return translations
            .asFlow()
            .map {
                template
                    .update(it)
                    .subscribeOn(boundedElastic())
                    .awaitSingle()
            }
    }

    private fun delete(translations: List<T>): Flow<T> {
        return translations
            .asFlow()
            .map {
                template
                    .delete(it)
                    .subscribeOn(boundedElastic())
                    .awaitSingle()
            }
    }
}
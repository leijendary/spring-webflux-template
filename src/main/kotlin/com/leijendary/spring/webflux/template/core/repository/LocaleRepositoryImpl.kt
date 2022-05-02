package com.leijendary.spring.webflux.template.core.repository

import com.leijendary.spring.webflux.template.core.entity.LocaleEntity
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import java.util.*

@Repository
class LocaleRepositoryImpl<T : LocaleEntity>(private val template: R2dbcEntityTemplate) : LocaleRepository<T> {
    override fun save(referenceId: UUID, translations: Set<T>): Flux<T> {
        return translations
            .toFlux()
            .flatMap {
                it.referenceId = referenceId

                template.insert(it)
            }
    }

    override fun save(referenceId: UUID, oldTranslations: Set<T>, newTranslations: Set<T>): Flux<T> {
        val isolation = LocaleEntity.isolate(oldTranslations, newTranslations)

        return Flux
            .merge(
                save(referenceId, isolation.creates),
                update(isolation.updates),
            )
            .doOnNext { delete(isolation.deletes) }
    }

    private fun update(translations: Set<T>): Flux<T> {
        return translations
            .toFlux()
            .flatMap { template.update(it) }
    }

    private fun delete(translations: Set<T>): Flux<T> {
        return translations
            .toFlux()
            .flatMap { template.delete(it) }
    }
}
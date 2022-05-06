package com.leijendary.spring.webflux.template.core.repository

import com.leijendary.spring.webflux.template.core.entity.LocaleEntity
import reactor.core.publisher.Flux
import java.util.*

interface LocaleRepository<T : LocaleEntity> {
    fun save(referenceId: UUID, translations: List<T>): Flux<T>

    fun save(referenceId: UUID, oldTranslations: List<T>, newTranslations: List<T>): Flux<T>
}
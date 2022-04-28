package com.leijendary.spring.webflux.template.core.repository

import com.leijendary.spring.webflux.template.core.model.LocaleModel
import reactor.core.publisher.Flux
import java.util.*

interface LocaleRepository<T : LocaleModel> {
    fun save(referenceId: UUID, translations: Set<T>): Flux<T>

    fun save(referenceId: UUID, oldTranslations: Set<T>, newTranslations: Set<T>): Flux<T>
}
package com.leijendary.spring.webflux.template.core.repository

import com.leijendary.spring.webflux.template.core.entity.LocaleEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

interface LocaleRepository<T : LocaleEntity> {
    fun save(referenceId: UUID, translations: List<T>): Flow<T>

    fun save(referenceId: UUID, oldTranslations: List<T>, newTranslations: List<T>): Flow<T>
}
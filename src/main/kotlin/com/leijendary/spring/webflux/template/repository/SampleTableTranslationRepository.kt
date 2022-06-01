package com.leijendary.spring.webflux.template.repository

import com.leijendary.spring.webflux.template.core.repository.LocaleRepository
import com.leijendary.spring.webflux.template.entity.SampleTableTranslation
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface SampleTableTranslationRepository : CoroutineCrudRepository<SampleTableTranslation, Long>,
    LocaleRepository<SampleTableTranslation> {
    fun findByReferenceId(referenceId: UUID): Flow<SampleTableTranslation>
}
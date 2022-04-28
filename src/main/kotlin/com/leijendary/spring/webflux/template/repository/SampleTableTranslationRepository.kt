package com.leijendary.spring.webflux.template.repository

import com.leijendary.spring.webflux.template.core.repository.LocaleRepository
import com.leijendary.spring.webflux.template.model.SampleTableTranslation
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.*

interface SampleTableTranslationRepository : ReactiveCrudRepository<SampleTableTranslation, Long>,
    LocaleRepository<SampleTableTranslation> {
    fun findByReferenceId(referenceId: UUID): Flux<SampleTableTranslation>
}
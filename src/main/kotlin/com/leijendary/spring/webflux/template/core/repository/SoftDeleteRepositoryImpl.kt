package com.leijendary.spring.webflux.template.core.repository

import com.leijendary.spring.webflux.template.core.entity.SoftDeleteEntity
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.scheduler.Schedulers.boundedElastic
import java.time.LocalDateTime

@Repository
class SoftDeleteRepositoryImpl<T : SoftDeleteEntity>(
    private val auditorAware: ReactiveAuditorAware<String>,
    private val dateTimeProvider: DateTimeProvider,
    private val template: R2dbcEntityTemplate
) : SoftDeleteRepository<T> {
    override suspend fun softDelete(entity: T): T {
        val auditor = auditorAware
            .currentAuditor
            .subscribeOn(boundedElastic())
            .awaitSingle()

        entity.deletedBy = auditor
        entity.deletedAt = dateTimeProvider.now.get() as LocalDateTime

        return template
            .update(entity)
            .subscribeOn(boundedElastic())
            .awaitSingle()
    }
}
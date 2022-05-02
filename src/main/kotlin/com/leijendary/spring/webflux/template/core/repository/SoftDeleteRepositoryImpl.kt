package com.leijendary.spring.webflux.template.core.repository

import com.leijendary.spring.webflux.template.core.entity.SoftDeleteEntity
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
class SoftDeleteRepositoryImpl<T : SoftDeleteEntity>(
    private val auditorAware: ReactiveAuditorAware<String>,
    private val dateTimeProvider: DateTimeProvider,
    private val template: R2dbcEntityTemplate
) : SoftDeleteRepository<T> {
    override fun softDelete(entity: T): Mono<T> {
        return auditorAware
            .currentAuditor
            .flatMap {
                entity.deletedBy = it
                entity.deletedAt = dateTimeProvider.now.get() as LocalDateTime

                template.update(entity)
            }
    }
}
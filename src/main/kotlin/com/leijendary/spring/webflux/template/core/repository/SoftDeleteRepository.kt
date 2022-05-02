package com.leijendary.spring.webflux.template.core.repository

import com.leijendary.spring.webflux.template.core.entity.SoftDeleteEntity
import reactor.core.publisher.Mono

interface SoftDeleteRepository<T : SoftDeleteEntity> {
    fun softDelete(entity: T): Mono<T>
}
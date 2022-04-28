package com.leijendary.spring.webflux.template.core.repository

import com.leijendary.spring.webflux.template.core.model.SoftDeleteModel
import reactor.core.publisher.Mono

interface SoftDeleteRepository<T : SoftDeleteModel> {
    fun softDelete(entity: T): Mono<T>
}
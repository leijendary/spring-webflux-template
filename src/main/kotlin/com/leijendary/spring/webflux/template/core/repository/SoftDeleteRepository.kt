package com.leijendary.spring.webflux.template.core.repository

import com.leijendary.spring.webflux.template.core.entity.SoftDeleteEntity

interface SoftDeleteRepository<T : SoftDeleteEntity> {
    suspend fun softDelete(entity: T): T
}
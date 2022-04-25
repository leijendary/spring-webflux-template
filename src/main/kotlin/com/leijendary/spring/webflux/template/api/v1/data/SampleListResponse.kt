package com.leijendary.spring.webflux.template.api.v1.data

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class SampleListResponse(
    val id: UUID,
    val column1: String,
    val column2: Long,
    val amount: BigDecimal,
    val createdAt: LocalDateTime,
    val createdBy: String,
    val lastModifiedAt: LocalDateTime,
    val lastModifiedBy: String,
) : Serializable
package com.leijendary.spring.webflux.template.api.v1.data

import com.leijendary.spring.webflux.template.core.data.LocalizedData
import java.math.BigDecimal
import java.time.LocalDateTime

data class SampleResponse(
    val column1: String,
    val column2: Long,
    val amount: BigDecimal,
    val createdAt: LocalDateTime,
    val createdBy: String,
    val lastModifiedAt: LocalDateTime,
    val lastModifiedBy: String,
) : LocalizedData<SampleTranslationResponse>()
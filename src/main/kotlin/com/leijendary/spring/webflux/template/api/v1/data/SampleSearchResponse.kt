package com.leijendary.spring.webflux.template.api.v1.data

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class SampleSearchResponse(
    val id: UUID,
    val column1: String,
    val column2: String,
    val amount: BigDecimal,
    val name: String,
    val description: String,
    val createdAt: LocalDateTime,
) : Serializable
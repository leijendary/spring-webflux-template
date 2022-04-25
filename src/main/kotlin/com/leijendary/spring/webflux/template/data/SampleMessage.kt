package com.leijendary.spring.webflux.template.data

import com.leijendary.spring.webflux.template.core.data.LocalizedData
import java.math.BigDecimal

data class SampleMessage(
    val column1: String,
    val column2: Int,
    val amount: BigDecimal
) : LocalizedData<SampleTranslationMessage>()
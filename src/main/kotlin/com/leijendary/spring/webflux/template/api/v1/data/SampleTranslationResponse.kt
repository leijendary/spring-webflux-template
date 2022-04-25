package com.leijendary.spring.webflux.template.api.v1.data

import com.leijendary.spring.webflux.template.core.data.LocaleData

data class SampleTranslationResponse(
    val name: String,
    val description: String,
    override val language: String,
    override val ordinal: Int
) : LocaleData(language, ordinal)
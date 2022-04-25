package com.leijendary.spring.webflux.template.data

import com.leijendary.spring.webflux.template.core.data.LocaleData

data class SampleTranslationMessage(
    val name: String,
    val description: String,
    override val language: String,
    override val ordinal: Int
) : LocaleData(language, ordinal)
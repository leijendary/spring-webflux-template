package com.leijendary.spring.webflux.template.core.data

import java.io.Serializable
import java.util.*

abstract class LocalizedData<T : LocaleData> : Serializable {
    lateinit var id: UUID
    var translations: Set<T> = HashSet()

    fun translation(language: String): T {
        val sorted = translations.sortedBy { it.ordinal }

        return sorted.firstOrNull { it.language == language } ?: sorted.first()
    }
}
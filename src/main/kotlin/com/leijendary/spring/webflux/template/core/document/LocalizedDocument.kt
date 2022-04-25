package com.leijendary.spring.webflux.template.core.document

import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType.Nested

abstract class LocalizedDocument<T : LocaleDocument> {
    @Field(type = Nested, includeInParent = true)
    var translations: Set<T> = HashSet()

    fun translation(language: String): T {
        val sorted = translations.sortedBy { it.ordinal }

        return sorted.firstOrNull { it.language == language } ?: sorted.first()
    }
}
package com.leijendary.spring.webflux.template.core.entity

import com.leijendary.spring.webflux.template.core.data.LocaleIsolation
import org.springframework.data.annotation.Id
import java.util.*

abstract class LocaleEntity : AppEntity() {
    @Id
    var id: Long = 0

    lateinit var referenceId: UUID
    lateinit var language: String

    var ordinal: Int = 0

    companion object {
        fun <T : LocaleEntity> isolate(oldTranslations: List<T>, newTranslations: List<T>): LocaleIsolation<T> {
            val creates = mutableListOf<T>()
            val updates = mutableListOf<T>()
            val deletes = mutableListOf<T>()

            oldTranslations.forEach { o ->
                if (o.language !in newTranslations.map { it.language }) {
                    deletes.add(o)

                    return@forEach
                }

                newTranslations
                    .filter { it.language == o.language }
                    .map {
                        it.id = o.id
                        it.referenceId = o.referenceId

                        it
                    }
                    .let { updates.addAll(it) }
            }

            newTranslations
                .filter { it.language !in oldTranslations.map { o -> o.language } }
                .let { creates.addAll(it) }

            return LocaleIsolation(creates, updates, deletes)
        }
    }
}
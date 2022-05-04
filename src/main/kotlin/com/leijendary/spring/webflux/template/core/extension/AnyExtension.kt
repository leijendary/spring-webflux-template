package com.leijendary.spring.webflux.template.core.extension

import com.fasterxml.jackson.databind.ObjectMapper
import com.leijendary.spring.webflux.template.core.util.SpringContext.Companion.getBean
import kotlinx.coroutines.reactor.awaitSingleOrNull
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers.boundedElastic
import java.lang.reflect.Field
import kotlin.reflect.KClass

private val mapper: ObjectMapper = getBean(ObjectMapper::class)

fun <T : Any> Any.toClass(type: KClass<T>): T {
    return mapper.convertValue(this, type.java)
}

fun Any.reflectField(property: String): Field {
    val field = try {
        this.javaClass.getDeclaredField(property)
    } catch (_: NoSuchFieldException) {
        this.javaClass.superclass.getDeclaredField(property)
    }
    field.isAccessible = true

    return field
}

fun Any.reflectGet(property: String): Any? {
    val field = reflectField(property)

    return field.get(this)
}

fun Any.reflectSet(property: String, value: Any?): Any? {
    val field = reflectField(property)

    field.set(this, value)

    return field.get(this)
}

object AnyUtil {
    private val log = logger()

    suspend fun Any.toJson(): String? {
        return Mono
            .fromCallable { mapper.writeValueAsString(this) }
            .subscribeOn(boundedElastic())
            .doOnError { log.warn("Failed to parse object to json", it) }
            .awaitSingleOrNull()
    }
}
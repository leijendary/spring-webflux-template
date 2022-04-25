package com.leijendary.spring.webflux.template.core.cache

import com.leijendary.spring.webflux.template.core.extension.AnyUtil.toJson
import com.leijendary.spring.webflux.template.core.extension.toClass
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.boot.autoconfigure.cache.CacheProperties
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class ReactiveRedisCache(template: ReactiveStringRedisTemplate, private val cacheProperties: CacheProperties) {
    private val operations = template.opsForValue()

    suspend fun <T : Any> get(key: String, type: KClass<T>): T? {
        return operations
            .get(key)
            .map { it.toClass(type) }
            .awaitSingleOrNull()
    }

    suspend fun <T> set(key: String, value: T): Boolean {
        val json = value?.toJson()

        if (json != null) {
            val ttl = cacheProperties.redis.timeToLive

            return operations
                .set(key, json, ttl)
                .awaitSingle()
        }

        return false
    }

    suspend fun delete(key: String): Boolean {
        return operations
            .delete(key)
            .awaitSingle()
    }
}
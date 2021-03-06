package com.leijendary.spring.webflux.template.core.security

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.cloud.bootstrap.encrypt.KeyProperties
import org.springframework.security.crypto.encrypt.Encryptors.delux
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers.boundedElastic

@Component
class Encryption(keyProperties: KeyProperties) {
    private val encryptor = delux(keyProperties.key, keyProperties.salt)

    suspend fun encrypt(raw: String): String = Mono
        .fromCallable { encryptor.encrypt(raw) }
        .subscribeOn(boundedElastic())
        .awaitSingle()

    suspend fun decrypt(encrypted: String): String = Mono
        .fromCallable { encryptor.decrypt(encrypted) }
        .subscribeOn(boundedElastic())
        .awaitSingle()
}
package com.leijendary.spring.webflux.template.core.factory

import com.leijendary.spring.webflux.template.core.data.Seek
import com.leijendary.spring.webflux.template.core.data.SeekToken
import com.leijendary.spring.webflux.template.core.data.Seekable
import com.leijendary.spring.webflux.template.core.extension.AnyUtil.toJson
import com.leijendary.spring.webflux.template.core.extension.logger
import com.leijendary.spring.webflux.template.core.extension.toClass
import com.leijendary.spring.webflux.template.core.model.SeekModel
import com.leijendary.spring.webflux.template.core.security.Encryption
import com.leijendary.spring.webflux.template.core.util.SpringContext.Companion.getBean
import java.util.Base64.getDecoder
import java.util.Base64.getEncoder

private val encryption = getBean(Encryption::class)
private val encoder = getEncoder()
private val decoder = getDecoder()

class SeekFactory {
    companion object {
        private val log = logger()

        fun from(seekable: Seekable): SeekToken? {
            return seekable.nextToken?.let { decode(it) }
        }

        fun <T : SeekModel> create(original: List<T>, seekable: Seekable): Seek<T> {
            var list = original
            var size = list.size
            val limit = seekable.limit
            var nextToken: String? = null

            if (size > limit) {
                list = list.dropLast(1)
                size -= 1

                val last = list.last()
                val seekToken = SeekToken(last.createdAt, last.rowId)

                nextToken = encode(seekToken)
            }

            return Seek(list, nextToken, size, seekable)
        }

        fun encode(seekToken: SeekToken): String {
            val json = seekToken.toJson()!!

            log.debug("Encoding next token {}", json)

            val encrypted = encryption.encrypt(json)
            val bytes = encrypted.encodeToByteArray()

            return encoder.encodeToString(bytes)
        }

        fun decode(value: String): SeekToken {
            val decoded = decoder.decode(value)
            val string = decoded.decodeToString()
            val json = encryption.decrypt(string)

            log.debug("Decoded next token {}", json)

            return json.toClass(SeekToken::class)
        }
    }
}
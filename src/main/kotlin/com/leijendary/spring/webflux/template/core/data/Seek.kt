package com.leijendary.spring.webflux.template.core.data

class Seek<T>(val content: List<T>, val nextToken: String?, val size: Int, val seekable: Seekable) {
    suspend fun <R> transform(transform: suspend (T) -> R): Seek<R> {
        val content = content.map { transform.invoke(it) }

        return Seek(content, nextToken, size, seekable)
    }
}
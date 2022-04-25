package com.leijendary.spring.webflux.template.core.data

class SeekMeta(seek: Seek<*>) {
    val size: Int = seek.size
    val limit: Int = seek.seekable.limit
    val nextToken: String? = seek.nextToken
}
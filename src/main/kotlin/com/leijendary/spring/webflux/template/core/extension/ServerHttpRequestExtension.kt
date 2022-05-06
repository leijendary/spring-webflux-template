package com.leijendary.spring.webflux.template.core.extension

import com.leijendary.spring.webflux.template.core.util.HEADER_USER_ID
import org.springframework.http.server.reactive.ServerHttpRequest

fun ServerHttpRequest.userId(): String? {
    return this.headers.getFirst(HEADER_USER_ID)
}
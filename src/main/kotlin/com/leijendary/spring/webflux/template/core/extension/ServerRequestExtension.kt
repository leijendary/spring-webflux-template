package com.leijendary.spring.webflux.template.core.extension

import com.leijendary.spring.webflux.template.core.util.HEADER_TRACE_ID
import com.leijendary.spring.webflux.template.core.util.HEADER_USER_ID
import org.springframework.web.reactive.function.server.ServerRequest

fun ServerRequest.traceId(): String? {
    return this.headers().firstHeader(HEADER_TRACE_ID)
}

fun ServerRequest.userId(): String? {
    return this.headers().firstHeader(HEADER_USER_ID)
}
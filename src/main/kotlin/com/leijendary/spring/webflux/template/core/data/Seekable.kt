package com.leijendary.spring.webflux.template.core.data

import com.leijendary.spring.webflux.template.core.extension.queryParam
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.queryParamOrNull

private const val DEFAULT_LIMIT = 10

class Seekable(val nextToken: String? = null, val limit: Int = DEFAULT_LIMIT) {
    companion object {
        fun from(request: ServerRequest): Seekable {
            val nextToken = request
                .queryParamOrNull("nextToken")
                ?.ifBlank { null }
            val limit = request.queryParam("limit", DEFAULT_LIMIT, Int::class) { it.toInt() }

            return Seekable(nextToken, limit)
        }
    }
}

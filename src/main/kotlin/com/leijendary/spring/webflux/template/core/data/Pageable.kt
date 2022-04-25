package com.leijendary.spring.webflux.template.core.data

import com.leijendary.spring.webflux.template.core.extension.queryParam
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.web.reactive.function.server.ServerRequest

class Pageable {
    companion object {
        fun from(request: ServerRequest): PageRequest {
            val page = request.queryParam("page", 0, Int::class) { it.toInt() }
            val size = request.queryParam("size", 10, Int::class) { it.toInt() }
            val orders = request
                .queryParams()
                .getOrDefault("sort", emptyList<String>())
                .map {
                    val split = it
                        .split(",")
                        .let { s -> if (s.size == 1) listOf(s[0], "asc") else s }
                    val field = split[0]

                    when (split[1]) {
                        "desc" -> Order.desc(field)
                        else -> Order.asc(field)
                    }
                }

            return PageRequest.of(page, size, Sort.by(orders))
        }
    }
}
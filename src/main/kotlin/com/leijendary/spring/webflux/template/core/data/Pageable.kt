package com.leijendary.spring.webflux.template.core.data

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order

class Pageable(val page: Int = 0, val size: Int = 10, val sort: Array<String> = arrayOf()) {
    fun toRequest(): PageRequest {
        val orders = sort.map {
            when (it[0]) {
                '+' -> Order.asc(it.drop(1))
                '-' -> Order.desc(it.drop(1))
                else -> Order.asc(it)
            }
        }

        return PageRequest.of(page, size, Sort.by(orders))
    }
}
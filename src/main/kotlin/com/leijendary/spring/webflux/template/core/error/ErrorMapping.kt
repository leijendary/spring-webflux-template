package com.leijendary.spring.webflux.template.core.error

import com.leijendary.spring.webflux.template.core.data.ErrorData
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange
import kotlin.reflect.KClass

interface ErrorMapping : Ordered {
    fun supports(): KClass<out Throwable>
    fun <T : Throwable> status(throwable: T): HttpStatus
    fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData>

    override fun getOrder(): Int = 0
}
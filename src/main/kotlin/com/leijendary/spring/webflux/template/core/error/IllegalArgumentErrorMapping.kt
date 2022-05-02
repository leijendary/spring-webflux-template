package com.leijendary.spring.webflux.template.core.error

import com.leijendary.spring.webflux.template.core.data.ErrorData
import com.leijendary.spring.webflux.template.core.extension.locale
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import kotlin.reflect.KClass

@Component
class IllegalArgumentErrorMapping(private val messageSource: MessageSource) : ErrorMapping {
    override fun supports(): KClass<IllegalArgumentException> = IllegalArgumentException::class

    override fun <T : Throwable> status(throwable: T): HttpStatus = BAD_REQUEST

    override fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData> {
        val source = listOf("request")
        val code = "error.illegalArgument"
        val arguments = arrayOf(throwable.message)
        val message = messageSource.getMessage(code, arguments, exchange.locale())

        return listOf(ErrorData(source, code, message))
    }
}
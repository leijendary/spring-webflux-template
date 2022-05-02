package com.leijendary.spring.webflux.template.core.error

import com.leijendary.spring.webflux.template.core.data.ErrorData
import com.leijendary.spring.webflux.template.core.extension.locale
import org.springframework.beans.TypeMismatchException
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import kotlin.reflect.KClass

@Component
class TypeMismatchErrorMapping(private val messageSource: MessageSource) : ErrorMapping {
    override fun supports(): KClass<out Throwable> = TypeMismatchException::class

    override fun <T : Throwable> status(throwable: T): HttpStatus = BAD_REQUEST

    override fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData> {
        val exception = throwable as TypeMismatchException
        val type = exception.requiredType ?: Any::class.java
        val source = listOf("param")
        val code = "error.query.type.invalid"
        val args = arrayOf(exception.value, type.simpleName)
        val message = messageSource.getMessage(code, args, exchange.locale())
        val errorData = ErrorData(source, code, message)

        return listOf(errorData)
    }
}
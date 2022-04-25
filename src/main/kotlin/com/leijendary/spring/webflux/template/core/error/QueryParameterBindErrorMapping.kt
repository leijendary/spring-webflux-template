package com.leijendary.spring.webflux.template.core.error

import com.leijendary.spring.webflux.template.core.data.ErrorData
import com.leijendary.spring.webflux.template.core.exception.QueryParameterBindException
import com.leijendary.spring.webflux.template.core.extension.locale
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import kotlin.reflect.KClass

@Component
class QueryParameterBindErrorMapping(private val messageSource: MessageSource) : ErrorMapping {
    override fun supports(): KClass<out QueryParameterBindException> {
        return QueryParameterBindException::class
    }

    override fun <T : Throwable> status(throwable: T): HttpStatus {
        return (throwable as QueryParameterBindException).httpStatus
    }

    override fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData> {
        val exception = throwable as QueryParameterBindException
        val name = exception.name
        val source = listOf("param", exception.name)
        val code = "error.param.type.invalid"
        val args = arrayOf(name, exception.value, exception.type.simpleName)
        val message = messageSource.getMessage(code, args, exchange.locale())
        val errorData = ErrorData(source, code, message)

        return listOf(errorData)
    }
}
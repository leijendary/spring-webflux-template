package com.leijendary.spring.webflux.template.core.error

import com.leijendary.spring.webflux.template.core.data.ErrorData
import com.leijendary.spring.webflux.template.core.exception.ResourceNotUniqueException
import com.leijendary.spring.webflux.template.core.extension.locale
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import kotlin.reflect.KClass

@Component
class ResourceNotUniqueErrorMapping(private val messageSource: MessageSource) : ErrorMapping {
    override fun supports(): KClass<out ResourceNotUniqueException> = ResourceNotUniqueException::class

    override fun <T : Throwable> status(throwable: T): HttpStatus =
        (throwable as ResourceNotUniqueException).httpStatus

    override fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData> {
        val exception = throwable as ResourceNotUniqueException
        val source = exception.source
        val code = "validation.alreadyExists"
        val args = arrayOf(source.joinToString("."), exception.value)
        val message = messageSource.getMessage(code, args, exchange.locale())
        val errorData = ErrorData(source, code, message)

        return listOf(errorData)
    }
}
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
class NoSuchElementErrorMapping(private val messageSource: MessageSource) : ErrorMapping {
    override fun supports(): KClass<NoSuchElementException> = NoSuchElementException::class

    override fun <T : Throwable> status(throwable: T): HttpStatus = BAD_REQUEST

    override fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData> {
        val exception = throwable as NoSuchElementException
        val source = listOf("body")
        var code = "error.badRequest"
        val args = emptyArray<String>()
        var message = exception.message

        if (message == "No value received via onNext for awaitSingle") {
            code = "error.body.notPresent"
            message = messageSource.getMessage(code, args, exchange.locale())
        }

        val errorData = ErrorData(source, code, message)

        return listOf(errorData)
    }
}
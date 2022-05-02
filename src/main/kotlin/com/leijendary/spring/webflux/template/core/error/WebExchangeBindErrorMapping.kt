package com.leijendary.spring.webflux.template.core.error

import com.leijendary.spring.webflux.template.core.data.ErrorData
import com.leijendary.spring.webflux.template.core.extension.locale
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.stereotype.Component
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange
import kotlin.reflect.KClass

@Component
class WebExchangeBindErrorMapping(private val messageSource: MessageSource) : ErrorMapping {
    override fun supports(): KClass<out Throwable> = WebExchangeBindException::class

    override fun <T : Throwable> status(throwable: T): HttpStatus = BAD_REQUEST

    override fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData> {
        return (throwable as WebExchangeBindException)
            .bindingResult
            .allErrors
            .map { field: ObjectError ->
                val objectName = if (field is FieldError) field.field else field.objectName
                val source = listOf("body") + objectName
                    .split(".", "[")
                    .map {
                        if (it.endsWith("]")) it.replace("]", "").toInt() else it
                    }
                val code = field.defaultMessage ?: ""
                val arguments = field.arguments
                val message = messageSource.getMessage(code, arguments, exchange.locale())

                ErrorData(source, code, message)
            }
    }
}
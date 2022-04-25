package com.leijendary.spring.webflux.template.core.error

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.leijendary.spring.webflux.template.core.data.ErrorData
import com.leijendary.spring.webflux.template.core.extension.locale
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import kotlin.reflect.KClass

@Component
class ServerWebInputErrorMapping(private val messageSource: MessageSource) : ErrorMapping {
    override fun supports(): KClass<ServerWebInputException> {
        return ServerWebInputException::class
    }

    override fun <T : Throwable> status(throwable: T): HttpStatus {
        return BAD_REQUEST
    }

    override fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData> {
        val exception = throwable as ServerWebInputException

        return when (val cause = exception.mostSpecificCause) {
            is InvalidFormatException -> errors(exchange, cause)
            is JsonMappingException -> errors(cause)
            else -> {
                val source = listOf("body")
                val code = "error.badRequest"
                val message = throwable.message.substringAfter("JSON decoding error: ")
                val errorData = ErrorData(source, code, message)

                return listOf(errorData)
            }
        }
    }

    private fun errors(exchange: ServerWebExchange, exception: InvalidFormatException): List<ErrorData> {
        val source = createSource(exception.path)
        val code = "error.body.format.invalid"
        val arguments = arrayOf(source.last(), exception.value, exception.targetType.simpleName)
        val message = messageSource.getMessage(code, arguments, exchange.locale())
        val errorData = ErrorData(source, code, message)

        return listOf(errorData)
    }

    private fun errors(exception: JsonMappingException): List<ErrorData> {
        val source = createSource(exception.path)
        val code = "error.body.format.invalid"
        val message = exception.originalMessage
        val errorData = ErrorData(source, code, message)

        return listOf(errorData)
    }

    private fun createSource(path: List<JsonMappingException.Reference>): List<Any> {
        return listOf("body") + path.map { it.fieldName ?: it.index }
    }
}
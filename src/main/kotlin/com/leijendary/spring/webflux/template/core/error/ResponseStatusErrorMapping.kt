package com.leijendary.spring.webflux.template.core.error

import com.leijendary.spring.webflux.template.core.data.ErrorData
import com.leijendary.spring.webflux.template.core.extension.fullPath
import com.leijendary.spring.webflux.template.core.extension.locale
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import kotlin.reflect.KClass

@Component
class ResponseStatusErrorMapping(private val messageSource: MessageSource) : ErrorMapping {
    override fun supports(): KClass<out ResponseStatusException> = ResponseStatusException::class

    override fun <T : Throwable> status(throwable: T): HttpStatus = (throwable as ResponseStatusException).status

    override fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData> {
        val exception = throwable as ResponseStatusException

        return when (exception.status) {
            NOT_FOUND -> notFound(exchange)
            else -> {
                val source = listOf("request")
                val code = "error.serverError"
                val message = exception.message
                val errorData = ErrorData(source, code, message)

                return listOf(errorData)
            }
        }
    }

    private fun notFound(exchange: ServerWebExchange): List<ErrorData> {
        val request = exchange.request
        val url = request.uri.fullPath()
        val method = request.methodValue
        val source = listOf("path")
        val code = "error.mapping.notFound"
        val args = arrayOf(url, method)
        val message = messageSource.getMessage(code, args, exchange.locale())
        val errorData = ErrorData(source, code, message)

        return listOf(errorData)
    }
}
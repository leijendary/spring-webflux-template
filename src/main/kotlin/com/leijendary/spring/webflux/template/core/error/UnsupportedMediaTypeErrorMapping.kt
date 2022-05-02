package com.leijendary.spring.webflux.template.core.error

import com.leijendary.spring.webflux.template.core.data.ErrorData
import com.leijendary.spring.webflux.template.core.extension.locale
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.UnsupportedMediaTypeStatusException
import kotlin.reflect.KClass

@Component
class UnsupportedMediaTypeErrorMapping(private val messageSource: MessageSource) : ErrorMapping {
    override fun supports(): KClass<UnsupportedMediaTypeStatusException> = UnsupportedMediaTypeStatusException::class

    override fun <T : Throwable> status(throwable: T): HttpStatus = UNSUPPORTED_MEDIA_TYPE

    override fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData> {
        val source = listOf("header", "content-type")
        val code = "error.mediaType.notSupported"
        val contentType = throwable.message!!
            .substringAfter("Content type '")
            .substringBefore(";")
        val args = arrayOf(contentType)
        val message = messageSource.getMessage(code, args, exchange.locale())
        val errorData = ErrorData(source, code, message)

        return listOf(errorData)
    }
}
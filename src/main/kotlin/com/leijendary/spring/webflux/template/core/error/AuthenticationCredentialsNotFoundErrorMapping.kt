package com.leijendary.spring.webflux.template.core.error

import com.leijendary.spring.webflux.template.core.data.ErrorData
import com.leijendary.spring.webflux.template.core.extension.locale
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import kotlin.reflect.KClass

@Component
class AuthenticationCredentialsNotFoundErrorMapping(private val messageSource: MessageSource) : ErrorMapping {
    override fun supports(): KClass<out AuthenticationCredentialsNotFoundException> =
        AuthenticationCredentialsNotFoundException::class

    override fun <T : Throwable> status(throwable: T): HttpStatus = FORBIDDEN

    override fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData> {
        val sources = listOf("header", "authorization", "scope")
        val code = "access.denied"
        val message = messageSource.getMessage(code, emptyArray(), exchange.locale())
        val errorData = ErrorData(sources, code, message)

        return listOf(errorData)
    }
}
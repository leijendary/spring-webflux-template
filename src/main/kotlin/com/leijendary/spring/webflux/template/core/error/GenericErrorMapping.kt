package com.leijendary.spring.webflux.template.core.error

import com.leijendary.spring.webflux.template.core.data.ErrorData
import com.leijendary.spring.webflux.template.core.extension.locale
import com.leijendary.spring.webflux.template.core.extension.logger
import com.leijendary.spring.webflux.template.core.util.SpringContext.Companion.isProd
import org.springframework.context.MessageSource
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import kotlin.reflect.KClass

@Component
class GenericErrorMapping(private val messageSource: MessageSource) : ErrorMapping {
    private val log = logger()

    override fun supports(): KClass<Throwable> = Throwable::class

    override fun <T : Throwable> status(throwable: T): HttpStatus = INTERNAL_SERVER_ERROR

    override fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData> {
        log.error("Generic uncaught exception", throwable)

        val source = listOf("server", "internal")
        val code = "error.serverError"
        val message = if (isProd()) {
            messageSource.getMessage(code, emptyArray(), exchange.locale())
        } else {
            throwable.message
        }
        val errorData = ErrorData(source, code, message)

        return listOf(errorData)
    }

    override fun getOrder(): Int {
        return LOWEST_PRECEDENCE
    }
}
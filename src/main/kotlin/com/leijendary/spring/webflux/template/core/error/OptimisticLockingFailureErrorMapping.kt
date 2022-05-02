package com.leijendary.spring.webflux.template.core.error

import com.leijendary.spring.webflux.template.core.data.ErrorData
import com.leijendary.spring.webflux.template.core.extension.locale
import com.leijendary.spring.webflux.template.core.extension.snakeCaseToCamelCase
import org.springframework.context.MessageSource
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import kotlin.reflect.KClass

@Component
class OptimisticLockingFailureErrorMapping(private val messageSource: MessageSource) : ErrorMapping {
    override fun supports(): KClass<out OptimisticLockingFailureException> = OptimisticLockingFailureException::class

    override fun <T : Throwable> status(throwable: T): HttpStatus = CONFLICT

    override fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData> {
        val exception = throwable as OptimisticLockingFailureException
        val exceptionMessage = exception.message!!
        val table = exceptionMessage
            .substringAfter("table [")
            .substringBefore("].")
            .snakeCaseToCamelCase(true)
        val source = listOf("data") + table + "version"
        val code = "error.data.version.conflict"
        val message = messageSource.getMessage(code, emptyArray(), exchange.locale())
        val errorData = ErrorData(source, code, message)

        return listOf(errorData)
    }
}
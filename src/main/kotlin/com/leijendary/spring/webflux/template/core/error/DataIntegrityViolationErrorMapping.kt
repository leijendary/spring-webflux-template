package com.leijendary.spring.webflux.template.core.error

import com.leijendary.spring.webflux.template.core.data.ErrorData
import com.leijendary.spring.webflux.template.core.extension.locale
import com.leijendary.spring.webflux.template.core.extension.reflectGet
import com.leijendary.spring.webflux.template.core.extension.snakeCaseToCamelCase
import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import org.springframework.context.MessageSource
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import kotlin.reflect.KClass

@Component
class DataIntegrityViolationErrorMapping(private val messageSource: MessageSource) : ErrorMapping {
    override fun supports(): KClass<out DataIntegrityViolationException> {
        return DataIntegrityViolationException::class
    }

    override fun <T : Throwable> status(throwable: T): HttpStatus {
        return when (throwable.cause) {
            is R2dbcDataIntegrityViolationException -> CONFLICT
            else -> INTERNAL_SERVER_ERROR
        }
    }

    override fun <T : Throwable> getErrors(exchange: ServerWebExchange, throwable: T): List<ErrorData> {
        val exception = throwable as DataIntegrityViolationException

        return when (val cause = exception.cause) {
            is R2dbcDataIntegrityViolationException -> errors(exchange, cause)
            else -> {
                val source = listOf("data", "entity")
                val code = "error.data.integrity"
                val args = arrayOf(exception.message ?: "")
                val message = messageSource.getMessage(code, args, exchange.locale())
                val errorData = ErrorData(source, code, message)

                return listOf(errorData)
            }
        }
    }

    private fun errors(exchange: ServerWebExchange, exception: R2dbcDataIntegrityViolationException): List<ErrorData> {
        val errorDetails = exception.reflectGet("errorDetails")!!
        val tableName = errorDetails
            .reflectGet("tableName")
            .toString()
            .snakeCaseToCamelCase(true)
        val detail = errorDetails
            .reflectGet("detail")
            .toString()
        val field = detail
            .substringAfter("Key (")
            .substringBefore(")=")
            .snakeCaseToCamelCase()
        val value = detail
            .substringAfter("=(")
            .substringBefore(") ")
        val source = listOf("data", tableName, field)
        val code = "validation.alreadyExists"
        val args = arrayOf(field, value)
        val message = messageSource.getMessage(code, args, exchange.locale())
        val errorData = ErrorData(source, code, message)

        return listOf(errorData)
    }
}
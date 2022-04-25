package com.leijendary.spring.webflux.template.core.handler

import com.leijendary.spring.webflux.template.core.data.ErrorResponse
import com.leijendary.spring.webflux.template.core.error.ErrorMapping
import com.leijendary.spring.webflux.template.core.extension.AnyUtil.toJson
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders.CONTENT_LENGTH
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.nio.charset.StandardCharsets.UTF_8

@Component
@Order(-2)
class GlobalExceptionHandler(errorMappings: List<ErrorMapping>) : ErrorWebExceptionHandler {
    private val errors = errorMappings
        .associateBy { it.supports().java.canonicalName }
        .withDefault { errorMappings.last() }

    override fun handle(exchange: ServerWebExchange, throwable: Throwable): Mono<Void> {
        val name = throwable::class.java.canonicalName
        val mapping = errors.getValue(name)
        val errors = mapping.getErrors(exchange, throwable)
        val status = mapping.status(throwable)
        val json = ErrorResponse
            .builder(exchange.request)
            .addErrors(errors)
            .status(status)
            .build()
            .toJson()!!
        val response = exchange.response
        response.headers[CONTENT_TYPE] = APPLICATION_JSON_VALUE
        response.headers[CONTENT_LENGTH] = json.length.toString()
        response.statusCode = status

        val body = response
            .bufferFactory()
            .wrap(json.toByteArray(UTF_8))
            .toMono()

        return response.writeWith(body)
    }
}
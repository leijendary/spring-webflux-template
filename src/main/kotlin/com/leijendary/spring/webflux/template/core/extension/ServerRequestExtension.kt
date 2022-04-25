package com.leijendary.spring.webflux.template.core.extension

import com.leijendary.spring.webflux.template.core.exception.PathVariableBindException
import com.leijendary.spring.webflux.template.core.exception.QueryParameterBindException
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.queryParamOrNull
import java.util.function.Function
import kotlin.reflect.KClass

fun <T : Any> ServerRequest.pathVariable(name: String, type: KClass<T>, strategy: Function<String, T>): T {
    val value = this.pathVariable(name)

    return try {
        strategy.apply(value)
    } catch (exception: IllegalArgumentException) {
        throw PathVariableBindException(name, value, type)
    } catch (ignored: Throwable) {
        throw PathVariableBindException(name, value, type)
    }
}

fun <T : Any> ServerRequest.queryParam(name: String, default: T, type: KClass<T>, strategy: Function<String, T>): T {
    val value = this.queryParamOrNull(name) ?: return default

    return try {
        strategy.apply(value)
    } catch (throwable: Throwable) {
        throw QueryParameterBindException(name, value, type)
    }
}
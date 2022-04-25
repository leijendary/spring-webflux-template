package com.leijendary.spring.webflux.template.core.data

import com.leijendary.spring.webflux.template.core.extension.fullPath
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.server.reactive.ServerHttpRequest
import java.time.LocalDateTime.now

class ErrorResponse(
    val errors: List<ErrorData> = emptyList(),
    val meta: Map<String, Any> = emptyMap(),
    val links: Map<String, String?> = emptyMap()
) : Response {
    companion object {
        fun builder(request: ServerHttpRequest): ErrorResponseBuilder {
            return ErrorResponseBuilder(request)
                .status(INTERNAL_SERVER_ERROR)
                .meta("requestId", request.id)
                .selfLink()
        }
    }

    class ErrorResponseBuilder(private val request: ServerHttpRequest) {
        private val errors: MutableList<ErrorData> = ArrayList()
        private val meta: MutableMap<String, Any> = HashMap()
        private val links: MutableMap<String, String?> = HashMap()

        fun build(): ErrorResponse {
            meta["timestamp"] = now()

            return ErrorResponse(errors, meta, links)
        }

        fun addError(source: List<Any>, code: String, message: String?): ErrorResponseBuilder {
            errors.add(ErrorData(source, code, message))

            return this
        }

        fun addErrors(errors: List<ErrorData>): ErrorResponseBuilder {
            this.errors.addAll(errors)

            return this
        }

        fun meta(key: String, value: Any): ErrorResponseBuilder {
            meta[key] = value

            return this
        }

        fun status(httpStatus: HttpStatus): ErrorResponseBuilder {
            meta["status"] = httpStatus.value()

            return this
        }

        fun selfLink(): ErrorResponseBuilder {
            links["self"] = request.uri.fullPath()

            return this
        }
    }
}
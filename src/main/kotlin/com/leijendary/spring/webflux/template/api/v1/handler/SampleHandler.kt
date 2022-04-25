package com.leijendary.spring.webflux.template.api.v1.handler

import com.leijendary.spring.webflux.template.api.v1.data.SampleRequest
import com.leijendary.spring.webflux.template.api.v1.service.SampleTableService
import com.leijendary.spring.webflux.template.client.SampleClient
import com.leijendary.spring.webflux.template.core.data.DataResponse
import com.leijendary.spring.webflux.template.core.data.Seekable
import com.leijendary.spring.webflux.template.core.extension.language
import com.leijendary.spring.webflux.template.core.extension.locale
import com.leijendary.spring.webflux.template.core.extension.pathVariable
import com.leijendary.spring.webflux.template.core.extension.timeZone
import com.leijendary.spring.webflux.template.core.validator.BindingValidator
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.TEXT_HTML_VALUE
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import java.util.*
import java.util.UUID.fromString

@Component
class SampleHandler(
    private val bindingValidator: BindingValidator,
    private val sampleClient: SampleClient,
    private val sampleTableService: SampleTableService
) {
    suspend fun seek(request: ServerRequest): ServerResponse {
        val query = request.queryParamOrNull("query") ?: ""
        val seekable = Seekable.from(request)
        val result = sampleTableService.seek(query, seekable)

        return DataResponse.from(request, result)
    }

    suspend fun create(request: ServerRequest): ServerResponse {
        val body = request.awaitBody<SampleRequest>()

        bindingValidator.validate(body)

        val result = sampleTableService.create(body)

        return DataResponse.of(request, result, CREATED)
    }

    suspend fun get(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id", UUID::class) { fromString(it) }
        val result = sampleTableService.get(id)

        return DataResponse.of(request, result)
    }

    suspend fun update(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id", UUID::class) { fromString(it) }
        val body = request.awaitBody<SampleRequest>()

        bindingValidator.validate(body)

        val result = sampleTableService.update(id, body)

        return DataResponse.of(request, result)
    }

    suspend fun delete(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id", UUID::class) { fromString(it) }

        sampleTableService.delete(id)

        return ServerResponse
            .noContent()
            .build()
            .awaitSingle()
    }

    suspend fun client(request: ServerRequest): ServerResponse {
        return sampleClient
            .homepage()
            .flatMap {
                ServerResponse
                    .ok()
                    .header(CONTENT_TYPE, TEXT_HTML_VALUE)
                    .bodyValue(it)
            }
            .awaitSingle()
    }

    suspend fun locale(request: ServerRequest): ServerResponse {
        return ServerResponse
            .ok()
            .bodyValueAndAwait(request.exchange().locale())
    }

    suspend fun language(request: ServerRequest): ServerResponse {
        return ServerResponse
            .ok()
            .bodyValueAndAwait(request.exchange().language())
    }

    suspend fun timeZone(request: ServerRequest): ServerResponse {
        return ServerResponse
            .ok()
            .bodyValueAndAwait(request.exchange().timeZone())
    }
}
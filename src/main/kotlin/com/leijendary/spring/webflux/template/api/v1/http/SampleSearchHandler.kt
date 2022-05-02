package com.leijendary.spring.webflux.template.api.v1.http

import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.api.v1.search.SampleSearch
import com.leijendary.spring.webflux.template.api.v1.service.SampleService
import com.leijendary.spring.webflux.template.core.data.DataResponse
import com.leijendary.spring.webflux.template.core.data.Pageable
import com.leijendary.spring.webflux.template.core.extension.pathVariable
import com.leijendary.spring.webflux.template.core.util.RequestContext.language
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.queryParamOrNull
import java.util.*
import java.util.UUID.fromString
import kotlin.system.measureTimeMillis

@Component
class SampleSearchHandler(private val sampleService: SampleService, private val sampleSearch: SampleSearch) {
    companion object {
        private val MAPPER = SampleMapper.INSTANCE
    }

    suspend fun page(request: ServerRequest): ServerResponse {
        val query = request.queryParamOrNull("query") ?: ""
        val seekable = Pageable.from(request)
        val language = language.awaitSingle()
        val result = sampleSearch
            .page(query, seekable)
            .map { page ->
                val translation = page.translation(language)

                MAPPER.toSearchResponse(page, translation)
            }

        return DataResponse.from(request, result)
    }

    suspend fun get(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id", UUID::class) { fromString(it) }
        val result = sampleSearch.get(id)

        return DataResponse.of(request, result)
    }

    suspend fun reindex(request: ServerRequest): ServerResponse {
        var count: Int
        val time = measureTimeMillis {
            count = sampleService.reindex()
        }

        return ServerResponse
            .ok()
            .bodyValueAndAwait("Re-indexed $count records to Sample Search, completed in ${time}ms")
    }
}
package com.leijendary.spring.webflux.template.api.v1.grpc

import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.api.v1.search.SampleSearch
import com.leijendary.spring.webflux.template.core.data.Pageable
import com.leijendary.spring.webflux.template.core.util.RequestContext.language
import kotlinx.coroutines.reactor.awaitSingle
import net.devh.boot.grpc.server.service.GrpcService
import java.util.UUID.fromString

/*
@GrpcService
class SampleSearchGrpc(private val sampleSearch: SampleSearch) : SampleSearchServiceCoroutineImplBase() {
    companion object {
        private val MAPPER = SampleMapper.INSTANCE
    }

    override suspend fun page(request: SampleSearchPageRequest): SampleSearchPageResponse {
        val query = request.query
        val pageable = Pageable.from(request.page, request.size, request.sortList)
        val language = language.awaitSingle()
        val result = sampleSearch
            .page(query, pageable)
            .map {
                val translation = it.translation(language)

                MAPPER.toGrpcSearchResponse(it, translation)
            }

        return SampleSearchPageResponse.newBuilder()
            .addAllSamples(result.content)
            .setPage(result.number)
            .setSize(result.size)
            .setTotalPages(result.totalPages)
            .setTotalElements(result.totalElements)
            .build()
    }

    override suspend fun get(request: SampleGetRequest): SampleSearchResponse {
        val id = fromString(request.id)
        val result = sampleSearch.get(id)
        val language = language.awaitSingle()
        val translation = result.translation(language)

        return MAPPER.toGrpcSearchResponse(result, translation)
    }
}*/

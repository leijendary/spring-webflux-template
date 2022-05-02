package com.leijendary.spring.webflux.template.api.v1.grpc

import com.google.protobuf.Empty
import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.api.v1.service.SampleService
import com.leijendary.spring.webflux.template.core.data.Seekable
import com.leijendary.spring.webflux.template.v1.sample.SampleServiceGrpcKt.SampleServiceCoroutineImplBase
import com.leijendary.spring.webflux.template.v1.sample.SampleV1.*
import net.devh.boot.grpc.server.service.GrpcService
import java.util.UUID.fromString

@GrpcService
class SampleGrpc(private val sampleService: SampleService) : SampleServiceCoroutineImplBase() {
    companion object {
        private val MAPPER = SampleMapper.INSTANCE
    }

    override suspend fun seek(request: SampleSeekRequest): SampleSeekResponse {
        val query = request.query
        val seekable = Seekable(request.nextToken, request.limit)
        val result = sampleService
            .seek(query, seekable)
            .transform { MAPPER.toGrpcListResponse(it) }

        return SampleSeekResponse.newBuilder()
            .addAllSamples(result.content)
            .setSize(result.size)
            .setLimit(seekable.limit)
            .setNextToken(result.nextToken)
            .build()
    }

    override suspend fun create(request: SampleRequest): SampleResponse {
        val sampleRequest = MAPPER.toRequest(request)
        val result = sampleService.create(sampleRequest)
        val response = MAPPER.toGrpcResponse(result)
        val translations = MAPPER.toGrpcResponse(result.translations)

        return response
            .toBuilder()
            .addAllTranslations(translations)
            .build()
    }

    override suspend fun get(request: SampleGetRequest): SampleResponse {
        val id = fromString(request.id)
        val result = sampleService.get(id)
        val response = MAPPER.toGrpcResponse(result)
        val translations = MAPPER.toGrpcResponse(result.translations)

        return response
            .toBuilder()
            .addAllTranslations(translations)
            .build()
    }

    override suspend fun update(request: SampleUpdateRequest): SampleResponse {
        val id = fromString(request.id)
        val body = MAPPER.toRequest(request)
        val result = sampleService.update(id, body)
        val response = MAPPER.toGrpcResponse(result)
        val translations = MAPPER.toGrpcResponse(result.translations)

        return response
            .toBuilder()
            .addAllTranslations(translations)
            .build()
    }

    override suspend fun delete(request: SampleGetRequest): Empty {
        val id = fromString(request.id)

        sampleService.delete(id)

        return Empty.getDefaultInstance()
    }
}
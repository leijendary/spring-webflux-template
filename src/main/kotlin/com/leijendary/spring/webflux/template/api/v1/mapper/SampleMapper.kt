package com.leijendary.spring.webflux.template.api.v1.mapper

import com.leijendary.spring.webflux.template.api.v1.data.*
import com.leijendary.spring.webflux.template.core.mapper.StringMapper
import com.leijendary.spring.webflux.template.data.SampleMessage
import com.leijendary.spring.webflux.template.document.SampleDocument
import com.leijendary.spring.webflux.template.document.SampleTranslationDocument
import com.leijendary.spring.webflux.template.entity.SampleTable
import com.leijendary.spring.webflux.template.entity.SampleTableTranslation
import com.leijendary.spring.webflux.template.v1.sample.SampleV1
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Mappings
import org.mapstruct.factory.Mappers.getMapper

@Mapper
interface SampleMapper : StringMapper {
    companion object {
        val INSTANCE: SampleMapper = getMapper(SampleMapper::class.java)
    }

    fun toListResponse(sampleTable: SampleTable): SampleListResponse

    fun toGrpcListResponse(sampleTable: SampleTable): SampleV1.SampleListResponse

    @Mapping(source = "translationsList", target = "translations")
    fun toRequest(sampleRequest: SampleV1.SampleRequest): SampleRequest

    @Mapping(source = "translationsList", target = "translations")
    fun toRequest(sampleUpdateRequest: SampleV1.SampleUpdateRequest): SampleRequest

    fun toResponse(sampleTable: SampleTable): SampleResponse

    fun toGrpcResponse(sampleTable: SampleTable): SampleV1.SampleResponse

    fun toGrpcResponse(translations: Set<SampleTableTranslation>): List<SampleV1.SampleTranslation>

    @Mappings(
        Mapping(source = "field1", target = "column1"),
        Mapping(source = "field2", target = "column2")
    )
    fun toEntity(sampleRequest: SampleRequest): SampleTable

    fun toEntity(translations: List<SampleTranslationRequest>): Set<SampleTableTranslation>

    @Mappings(
        Mapping(source = "translation.name", target = "name"),
        Mapping(source = "translation.description", target = "description")
    )
    fun toSearchResponse(sampleDocument: SampleDocument, translation: SampleTranslationDocument): SampleSearchResponse

    fun toDocument(sampleTable: SampleTable): SampleDocument

    @Mappings(
        Mapping(source = "translation.name", target = "name"),
        Mapping(source = "translation.description", target = "description")
    )
    fun toGrpcSearchResponse(
        sampleDocument: SampleDocument,
        translation: SampleTranslationDocument
    ): SampleV1.SampleSearchResponse

    @Mappings(
        Mapping(source = "field1", target = "column1"),
        Mapping(source = "field2", target = "column2")
    )
    fun update(sampleRequest: SampleRequest, @MappingTarget sampleTable: SampleTable)

    fun update(sampleTable: SampleTable, @MappingTarget sampleDocument: SampleDocument)

    fun toMessage(sampleTable: SampleTable): SampleMessage
}
package com.leijendary.spring.webflux.template.api.v1.mapper

import com.leijendary.spring.webflux.template.api.v1.data.*
import com.leijendary.spring.webflux.template.data.SampleMessage
import com.leijendary.spring.webflux.template.document.SampleDocument
import com.leijendary.spring.webflux.template.document.SampleTranslationDocument
import com.leijendary.spring.webflux.template.entity.SampleTable
import com.leijendary.spring.webflux.template.entity.SampleTableTranslation
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Mappings
import org.mapstruct.factory.Mappers.getMapper

@Mapper
interface SampleMapper {
    companion object {
        val INSTANCE: SampleMapper = getMapper(SampleMapper::class.java)
    }

    fun toListResponse(sampleTable: SampleTable): SampleListResponse

    fun toResponse(sampleTable: SampleTable): SampleResponse

    fun toResponse(translations: Set<SampleTableTranslation>): Set<SampleTranslationResponse>

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

    fun toDocument(sampleResponse: SampleResponse): SampleDocument

    @Mappings(
        Mapping(source = "field1", target = "column1"),
        Mapping(source = "field2", target = "column2")
    )
    fun update(sampleRequest: SampleRequest, @MappingTarget sampleTable: SampleTable)

    fun update(sampleResponse: SampleResponse, @MappingTarget sampleDocument: SampleDocument)

    fun toMessage(sampleResponse: SampleResponse): SampleMessage
}
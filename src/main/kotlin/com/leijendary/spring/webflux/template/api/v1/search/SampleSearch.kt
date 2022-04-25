package com.leijendary.spring.webflux.template.api.v1.search

import com.leijendary.spring.webflux.template.api.v1.data.SampleResponse
import com.leijendary.spring.webflux.template.api.v1.data.SampleSearchResponse
import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.core.exception.ResourceNotFoundException
import com.leijendary.spring.webflux.template.core.util.RequestContext.language
import com.leijendary.spring.webflux.template.core.util.SearchUtil.match
import com.leijendary.spring.webflux.template.core.util.SearchUtil.sortBuilders
import com.leijendary.spring.webflux.template.document.SampleDocument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Service
import java.util.*

@Service
class SampleSearch(private val template: ReactiveElasticsearchTemplate) {
    companion object {
        private val MAPPER: SampleMapper = SampleMapper.INSTANCE
        private val SOURCE = listOf("search", "SampleSearch")
    }

    suspend fun page(query: String, pageable: Pageable): Page<SampleSearchResponse> {
        val searchBuilder = NativeSearchQueryBuilder()
        // Add the pagination to the search builder
        searchBuilder.withPageable(pageable)

        if (query.isNotEmpty()) {
            // Query for translations.name and translations.description
            val boolQuery = match(query, "translations.name", "translations.description")
            // Add the query for the actual search
            searchBuilder.withQuery(boolQuery)
        }

        // Each sort builder should be added into the search builder's sort
        val sortBuilders = sortBuilders(pageable.sort)
        // Add sort to the search builder
        searchBuilder.withSorts(sortBuilders)

        val searchQuery = searchBuilder.build()
        val language = language.awaitSingle()

        return template
            .searchForPage(searchQuery, SampleDocument::class.java)
            .map {
                val list = it.content.map { c -> c.content }
                val total = it.searchHits.totalHits

                PageImpl(list, pageable, total).map { page ->
                    val translation = page.translation(language)

                    MAPPER.toSearchResponse(page, translation)
                }
            }
            .awaitSingle()
    }

    suspend fun save(sampleResponse: SampleResponse): SampleDocument {
        val document = MAPPER.toDocument(sampleResponse)

        return template
            .save(document)
            .awaitSingle()
    }

    suspend fun save(sampleResponses: List<SampleResponse>): Flow<SampleDocument> {
        val list = sampleResponses.map { MAPPER.toDocument(it) }

        return template
            .saveAll(list, SampleDocument::class.java)
            .asFlow()
    }

    suspend fun get(id: UUID): SampleSearchResponse {
        val document = template
            .get(id.toString(), SampleDocument::class.java)
            .awaitSingleOrNull()
            ?: throw ResourceNotFoundException(SOURCE, id)
        val language = language.awaitSingle()
        val translation = document.translation(language)

        return MAPPER.toSearchResponse(document, translation)
    }

    suspend fun update(sampleResponse: SampleResponse): SampleSearchResponse {
        var document = template
            .get(sampleResponse.id.toString(), SampleDocument::class.java)
            .awaitSingleOrNull()
            ?: throw ResourceNotFoundException(SOURCE, sampleResponse.id)

        MAPPER.update(sampleResponse, document)

        document = template
            .save(document)
            .awaitSingle()

        val language = language.awaitSingle()
        val translation = document.translation(language)

        return MAPPER.toSearchResponse(document, translation)
    }

    suspend fun delete(id: UUID) {
        val document = template
            .get(id.toString(), SampleDocument::class.java)
            .awaitSingleOrNull()
            ?: throw ResourceNotFoundException(SOURCE, id)

        template
            .delete(document)
            .awaitSingle()
    }
}
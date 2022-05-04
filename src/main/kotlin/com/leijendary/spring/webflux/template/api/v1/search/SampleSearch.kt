package com.leijendary.spring.webflux.template.api.v1.search

import com.leijendary.spring.webflux.template.api.v1.data.SampleResponse
import com.leijendary.spring.webflux.template.api.v1.data.SampleSearchResponse
import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.core.data.Pageable
import com.leijendary.spring.webflux.template.core.exception.ResourceNotFoundException
import com.leijendary.spring.webflux.template.core.util.RequestContext.language
import com.leijendary.spring.webflux.template.core.util.SearchQuery.match
import com.leijendary.spring.webflux.template.core.util.SearchQuery.sortBuilders
import com.leijendary.spring.webflux.template.document.SampleDocument
import com.leijendary.spring.webflux.template.repository.SampleSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers.boundedElastic
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.*

@Service
class SampleSearch(
    private val sampleSearchRepository: SampleSearchRepository,
    private val template: ReactiveElasticsearchTemplate
) {
    companion object {
        private val MAPPER: SampleMapper = SampleMapper.INSTANCE
        private val SOURCE = listOf("search", "SampleSearch", "id")
    }

    suspend fun page(query: String, pageable: Pageable): Page<SampleSearchResponse> {
        val pageRequest = pageable.toRequest()
        val searchBuilder = NativeSearchQueryBuilder()
        // Add the pagination to the search builder
        searchBuilder.withPageable(pageRequest)

        if (query.isNotEmpty()) {
            // Query for translations.name and translations.description
            val boolQuery = match(query, "translations.name", "translations.description")
            // Add the query for the actual search
            searchBuilder.withQuery(boolQuery)
        }

        // Each sort builder should be added into the search builder's sort
        val sortBuilders = sortBuilders(pageRequest.sort)
        // Add sort to the search builder
        searchBuilder.withSorts(sortBuilders)

        val searchQuery = searchBuilder.build()
        val language = language()

        return template
            .searchForPage(searchQuery, SampleDocument::class.java)
            .map {
                val list = it.content.map { c -> c.content }
                val total = it.searchHits.totalHits

                PageImpl(list, pageRequest, total).map { page ->
                    val translation = page.translation(language)

                    MAPPER.toSearchResponse(page, translation)
                }
            }
            .subscribeOn(boundedElastic())
            .awaitSingle()
    }

    suspend fun save(sampleResponse: SampleResponse): SampleDocument {
        val document = Mono
            .just(MAPPER.toDocument(sampleResponse))
            .subscribeOn(boundedElastic())
            .awaitSingle()

        return sampleSearchRepository
            .save(document)
            .subscribeOn(boundedElastic())
            .awaitSingle()
    }

    suspend fun save(sampleResponses: List<SampleResponse>): Flow<SampleDocument> {
        return Mono
            .just(sampleResponses)
            .subscribeOn(boundedElastic())
            .map { responses -> responses.map { MAPPER.toDocument(it) } }
            .flatMapMany { sampleSearchRepository.saveAll(it) }
            .asFlow()
    }

    suspend fun get(id: UUID): SampleSearchResponse {
        val document = sampleSearchRepository
            .findById(id)
            .subscribeOn(boundedElastic())
            .switchIfEmpty { throw ResourceNotFoundException(SOURCE, id) }
            .awaitSingle()
        val language = language()

        return Mono
            .just(language)
            .subscribeOn(boundedElastic())
            .map { document.translation(it) }
            .map { MAPPER.toSearchResponse(document, it) }
            .awaitSingle()
    }

    suspend fun update(sampleResponse: SampleResponse): SampleSearchResponse {
        val id = sampleResponse.id
        val document = sampleSearchRepository
            .findById(id)
            .subscribeOn(boundedElastic())
            .switchIfEmpty { throw ResourceNotFoundException(SOURCE, id) }
            .doOnNext { MAPPER.update(sampleResponse, it) }
            .flatMap { sampleSearchRepository.save(it) }
            .awaitSingle()
        val language = language()

        return Mono
            .just(language)
            .subscribeOn(boundedElastic())
            .map { document.translation(it) }
            .map { MAPPER.toSearchResponse(document, it) }
            .awaitSingle()
    }

    suspend fun delete(id: UUID) {
        sampleSearchRepository
            .deleteById(id)
            .subscribeOn(boundedElastic())
            .awaitSingleOrNull()
    }
}
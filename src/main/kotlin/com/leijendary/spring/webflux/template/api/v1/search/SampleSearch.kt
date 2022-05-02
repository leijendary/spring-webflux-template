package com.leijendary.spring.webflux.template.api.v1.search

import com.leijendary.spring.webflux.template.api.v1.mapper.SampleMapper
import com.leijendary.spring.webflux.template.core.exception.ResourceNotFoundException
import com.leijendary.spring.webflux.template.core.util.RequestContext.language
import com.leijendary.spring.webflux.template.core.util.SearchUtil.match
import com.leijendary.spring.webflux.template.core.util.SearchUtil.sortBuilders
import com.leijendary.spring.webflux.template.document.SampleDocument
import com.leijendary.spring.webflux.template.entity.SampleTable
import com.leijendary.spring.webflux.template.repository.SampleSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.*

@Service
class SampleSearch(
    private val sampleSearchRepository: SampleSearchRepository,
    private val template: ReactiveElasticsearchTemplate
) {
    companion object {
        private val MAPPER: SampleMapper = SampleMapper.INSTANCE
        private val SOURCE = listOf("search", "SampleSearch")
    }

    suspend fun page(query: String, pageable: Pageable): Page<SampleDocument> {
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

        return template
            .searchForPage(searchQuery, SampleDocument::class.java)
            .map {
                val list = it.content.map { c -> c.content }
                val total = it.searchHits.totalHits

                PageImpl(list, pageable, total)
            }
            .awaitSingle()
    }

    suspend fun save(sampleTable: SampleTable): SampleDocument {
        val document = MAPPER.toDocument(sampleTable)

        return sampleSearchRepository
            .save(document)
            .awaitSingle()
    }

    suspend fun save(sampleTables: List<SampleTable>): Flow<SampleDocument> {
        val list = sampleTables.map { MAPPER.toDocument(it) }

        return sampleSearchRepository
            .saveAll(list)
            .asFlow()
    }

    suspend fun get(id: UUID): SampleDocument {
        return sampleSearchRepository
            .findById(id)
            .switchIfEmpty { throw ResourceNotFoundException(SOURCE, id) }
            .awaitSingle()
    }

    suspend fun update(sampleTable: SampleTable): SampleDocument {
        val id = sampleTable.id

        return sampleSearchRepository
            .findById(id)
            .switchIfEmpty { throw ResourceNotFoundException(SOURCE, id) }
            .doOnNext { MAPPER.update(sampleTable, it) }
            .flatMap { sampleSearchRepository.save(it) }
            .awaitSingle()
    }

    suspend fun delete(id: UUID) {
        sampleSearchRepository.deleteById(id)
    }
}
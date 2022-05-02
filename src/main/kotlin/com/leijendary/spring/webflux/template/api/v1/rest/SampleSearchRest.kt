package com.leijendary.spring.webflux.template.api.v1.rest

import com.leijendary.spring.webflux.template.api.v1.data.SampleSearchResponse
import com.leijendary.spring.webflux.template.api.v1.search.SampleSearch
import com.leijendary.spring.webflux.template.api.v1.service.SampleTableService
import com.leijendary.spring.webflux.template.core.data.DataResponse
import com.leijendary.spring.webflux.template.core.data.Pageable
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.system.measureTimeMillis

@RestController
@RequestMapping("/api/v1/samples/search")
@Tag(name = "Sample Search")
class SampleSearchRest(
    private val sampleSearch: SampleSearch,
    private val sampleTableService: SampleTableService
) {
    @GetMapping
    @Operation(summary = "List all the objects based on the query parameter")
    suspend fun list(query: String, pageable: Pageable): DataResponse<List<SampleSearchResponse>> = sampleSearch
        .page(query, pageable)
        .let { DataResponse.from(it) }

    @GetMapping("{id}")
    @Operation(summary = "Get the specific object using the ID in elasticsearch")
    suspend fun get(@PathVariable id: UUID): DataResponse<SampleSearchResponse> = sampleSearch
        .get(id)
        .let { DataResponse.of(it) }

    @PostMapping("reindex")
    @Operation(summary = "Reindex all objects")
    suspend fun reindex(): String {
        var count: Int
        val time = measureTimeMillis {
            count = sampleTableService.reindex()
        }

        return "Re-indexed $count records to Sample Search, completed in ${time}ms"
    }
}
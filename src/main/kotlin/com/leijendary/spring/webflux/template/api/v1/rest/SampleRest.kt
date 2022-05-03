package com.leijendary.spring.webflux.template.api.v1.rest

import com.leijendary.spring.webflux.template.api.v1.data.SampleListResponse
import com.leijendary.spring.webflux.template.api.v1.data.SampleRequest
import com.leijendary.spring.webflux.template.api.v1.data.SampleResponse
import com.leijendary.spring.webflux.template.api.v1.service.SampleTableService
import com.leijendary.spring.webflux.template.client.SampleClient
import com.leijendary.spring.webflux.template.core.data.DataResponse
import com.leijendary.spring.webflux.template.core.data.Seekable
import com.leijendary.spring.webflux.template.core.util.RequestContext
import com.leijendary.spring.webflux.template.core.util.RequestContext.zoneId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.TEXT_HTML_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.TextStyle.FULL
import java.util.*
import javax.validation.Valid

/**
 * This is an example of a controller that will be created in microservices.
 *
 * There are 3 parts of the [RequestMapping] url that we need to take note of:
 *      1. The api prefix ("api")
 *      2. The version ("v1")
 *      3. The parent path of this API ("/") which can be anything that this specific controller should be doing.
 *
 * Since this microservice uses a context path, the result of the url should be
 * "/sample/api/v1"
 *
 * The url paths should be in kebab-case except for the query parameters, body,
 * and other URL parts in which they should be in camelCase.
 *
 * For headers, I would recommend that the Header keys should be in
 * Pascal-Kebab-Case
 */
@RestController
@RequestMapping("/api/v1/samples")
@Tag(name = "Sample")
class SampleRest(private val sampleClient: SampleClient, private val sampleTableService: SampleTableService) {

    /**
     * This is a sample RequestMapping (Only GET method, that is why I used
     * [GetMapping])
     *
     * @param seekable The seek request. Since this API has seekable results, it is
     * recommended that the request parameters contains [Seekable]
     */
    @GetMapping
    @Operation(summary = "Sample implementation of swagger in a api")
    suspend fun seek(query: String? = "", seekable: Seekable): DataResponse<List<SampleListResponse>> =
        sampleTableService
            .seek(query, seekable)
            .let { DataResponse.from(it) }

    @PostMapping
    @ResponseStatus(CREATED)
    @Operation(summary = "Saves a sample record into the database")
    suspend fun create(@Valid @RequestBody request: SampleRequest): DataResponse<SampleResponse> = sampleTableService
        .create(request)
        .let { DataResponse.of(it, CREATED) }

    @GetMapping("{id}")
    @Operation(summary = "Retrieves the sample record from the database")
    suspend fun get(@PathVariable id: UUID): DataResponse<SampleResponse> = sampleTableService
        .get(id)
        .let { DataResponse.of(it) }

    @PutMapping("{id}")
    @Operation(summary = "Updates the sample record into the database")
    suspend fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: SampleRequest
    ): DataResponse<SampleResponse> = sampleTableService
        .update(id, request)
        .let { DataResponse.of(it) }

    @DeleteMapping("{id}")
    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Removes the sample record from the database")
    suspend fun delete(@PathVariable id: UUID) = sampleTableService.delete(id)

    @GetMapping(value = ["client"], produces = [TEXT_HTML_VALUE])
    suspend fun client(): String = sampleClient.homepage()

    @GetMapping(value = ["timezone"], produces = [TEXT_PLAIN_VALUE])
    suspend fun timezone(): String {
        val zoneId = zoneId()
        val displayName = zoneId.getDisplayName(FULL, RequestContext.locale())
        val id: String = zoneId.id

        return String.format("%s %s", displayName, id)
    }

    @GetMapping(value = ["locale"], produces = [TEXT_PLAIN_VALUE])
    suspend fun locale(): String = RequestContext.locale().toString()

    @GetMapping(value = ["language"], produces = [TEXT_PLAIN_VALUE])
    suspend fun language(): String = RequestContext.language()

    @GetMapping("timestamp")
    suspend fun timestamp(): Map<String, LocalDateTime> {
        val map: HashMap<String, LocalDateTime> = HashMap<String, LocalDateTime>()
        map["current"] = RequestContext.now()

        return map
    }
}
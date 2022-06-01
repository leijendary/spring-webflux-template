package com.leijendary.spring.webflux.template.rest

import com.leijendary.spring.webflux.template.ApplicationTests
import com.leijendary.spring.webflux.template.api.v1.data.SampleRequest
import com.leijendary.spring.webflux.template.api.v1.data.SampleResponse
import com.leijendary.spring.webflux.template.api.v1.data.SampleTranslationRequest
import com.leijendary.spring.webflux.template.core.data.DataResponse
import com.leijendary.spring.webflux.template.core.extension.scaled
import com.leijendary.spring.webflux.template.core.extension.toClass
import com.leijendary.spring.webflux.template.core.util.HEADER_SCOPE
import com.leijendary.spring.webflux.template.core.util.HEADER_USER_ID
import liquibase.repackaged.org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import java.lang.Thread.sleep
import java.math.BigDecimal
import java.nio.charset.StandardCharsets.UTF_8
import java.security.SecureRandom
import java.text.DecimalFormat
import java.util.Locale.getDefault
import kotlin.math.abs
import kotlin.math.ceil

class SampleSearchRestTest(
    @Autowired
    private val client: WebTestClient,

    @Autowired
    private val messageSource: MessageSource
) : ApplicationTests() {
    private val sampleUrl = "/api/v1/samples"
    private val url = "/api/v1/samples/search"
    private val random = SecureRandom()
    private val userId = "junit-testing"
    private val scopeCreate = "urn:sample:create:v1"
    private val scopeDelete = "urn:sample:delete:v1"
    private val decimalFormat = DecimalFormat("0.0#")
    private val languages = arrayOf("en", "jp")
    private val listTotal = 21
    private val listSize = 10
    private val detailMemberSize = 7
    private val detailMetaSize = 3
    private val detailLinksSize = 1
    private val listMemberSize = 7
    private val listMetaSize = 4
    private val metaPageSize = 5

    @Test
    fun `Sample Search Page - Should return the search page based on the limit and query`() {
        val suffix = RandomStringUtils.randomAlphabetic(8)
        val uri = "$url?query=$suffix&limit=$listSize&sort=-createdAt"
        val requests = (1..listTotal).map { createRequest(suffix) }
        requests.forEach {
            client
                .post()
                .uri(sampleUrl)
                .bodyValue(it)
                .header(HEADER_USER_ID, this.userId)
                .header(HEADER_SCOPE, this.scopeCreate)
                .exchange()
                .expectBody(DataResponse::class.java)
                .returnResult()
                .responseBody!!
                .data!!
                .toClass(SampleResponse::class)
        }

        sleep(2000)

        val size = requests.size
        val totalPages = ceil(size.toDouble() / listSize.toDouble()).toInt()
        val lastPage = totalPages - 1

        languages.forEach { language ->
            var page = 0
            var nextIndex = size - 1

            while (nextIndex > 0) {
                val pageUri = "$uri&page=$page"
                val selfUri = "$url?query=$suffix&limit=$listSize&page=$page&size=$listSize&sort=createdAt,DESC"
                val lastUri = "$url?query=$suffix&limit=$listSize&page=$lastPage&size=$listSize&sort=createdAt,DESC"
                val pageSize = if (nextIndex > listSize) listSize else nextIndex
                val linkSize = if (page in arrayOf(0, lastPage)) 3 else 4
                val bodyContentSpec = client
                    .get()
                    .uri(pageUri)
                    .header("Accept-Language", language)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.data").isArray
                    .jsonPath("$.data.length()").isEqualTo(listSize)
                    .jsonPath("$.meta").isMap
                    .jsonPath("$.meta.length()").isEqualTo(listMetaSize)
                    .jsonPath("$.meta.traceId").isNotEmpty
                    .jsonPath("$.meta.timestamp").isNumber
                    .jsonPath("$.meta.page").isMap
                    .jsonPath("$.meta.page.length()").isEqualTo(metaPageSize)
                    .jsonPath("$.meta.page.numberOfElements").isEqualTo(pageSize)
                    .jsonPath("$.meta.page.totalPages").isEqualTo(totalPages)
                    .jsonPath("$.meta.page.totalElements").isEqualTo(size)
                    .jsonPath("$.meta.page.size").isEqualTo(listSize)
                    .jsonPath("$.meta.page.number").isEqualTo(page)
                    .jsonPath("$.meta.status").isEqualTo(OK.value())
                    .jsonPath("$.links").isMap
                    .jsonPath("$.links.length()").isEqualTo(linkSize)
                    .jsonPath("$.links.self").isEqualTo(selfUri)
                    .jsonPath("$.links.last").isEqualTo(lastUri)

                if (page > 0) {
                    val previousUri =
                        "$url?query=$suffix&limit=$listSize&page=${page - 1}&size=$listSize&sort=createdAt,DESC"

                    bodyContentSpec.jsonPath("$.links.previous").isEqualTo(previousUri)
                }

                if (page < lastPage) {
                    val nextUri =
                        "$url?query=$suffix&limit=$listSize&page=${page + 1}&size=$listSize&sort=createdAt,DESC"

                    bodyContentSpec.jsonPath("$.links.next").isEqualTo(nextUri)
                }

                val lastIndex = nextIndex - pageSize + 1

                for ((listIndex, i) in (nextIndex downTo lastIndex).withIndex()) {
                    bodyContentSpec.jsonPath("$.data[$listIndex].length()").isEqualTo(listMemberSize)

                    assertResponse(bodyContentSpec, "$.data[$listIndex]", requests[i], language)

                    nextIndex--
                }

                page++
            }
        }
    }

    @Test
    fun `Sample Search Get - Should return the created search record`() {
        val suffix = RandomStringUtils.randomAlphabetic(8)
        val request = createRequest(suffix)
        val createResponse = client
            .post()
            .uri(sampleUrl)
            .bodyValue(request)
            .header(HEADER_USER_ID, this.userId)
            .header(HEADER_SCOPE, this.scopeCreate)
            .exchange()
            .expectBody()
            .returnResult()
            .responseBody!!
            .let { String(it, UTF_8) }
            .toClass(DataResponse::class)
            .data!!.toClass(SampleResponse::class)
        val uri = "$url/${createResponse.id}"

        sleep(2000)

        languages.forEach { language ->
            val bodyContentSpec = client
                .get()
                .uri(uri)
                .header("Accept-Language", language)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.data").isNotEmpty
                .jsonPath("$.data.length()").isEqualTo(detailMemberSize)
                .jsonPath("$.meta").isMap
                .jsonPath("$.meta.length()").isEqualTo(detailMetaSize)
                .jsonPath("$.meta.traceId").isNotEmpty
                .jsonPath("$.meta.timestamp").isNumber
                .jsonPath("$.meta.status").isEqualTo(OK.value())
                .jsonPath("$.links").isMap
                .jsonPath("$.links.length()").isEqualTo(detailLinksSize)
                .jsonPath("$.links.self").isEqualTo(uri)

            assertResponse(bodyContentSpec, "$.data", request, language)
        }
    }

    @Test
    fun `Sample Delete - Should return empty then 404 after`() {
        val suffix = RandomStringUtils.randomAlphabetic(8)
        val request = createRequest(suffix)
        val createResponse = client
            .post()
            .uri(sampleUrl)
            .bodyValue(request)
            .header(HEADER_USER_ID, this.userId)
            .header(HEADER_SCOPE, this.scopeCreate)
            .exchange()
            .expectBody()
            .returnResult()
            .responseBody!!
            .let { String(it, UTF_8) }
            .toClass(DataResponse::class)
            .data!!.toClass(SampleResponse::class)
        val sampleUri = "$sampleUrl/${createResponse.id}"

        sleep(2000)

        client
            .delete()
            .uri(sampleUri)
            .header(HEADER_USER_ID, this.userId)
            .header(HEADER_SCOPE, this.scopeDelete)
            .exchange()
            .expectStatus().isNoContent
            .expectBody().isEmpty

        sleep(1000)

        val args = arrayOf("search.SampleSearch.id", createResponse.id)
        val message = messageSource.getMessage("error.resource.notFound", args, getDefault())
        val uri = "$url/${createResponse.id}"

        sleep(2000)

        client
            .get()
            .uri(uri)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.errors").isArray
            .jsonPath("$.errors.length()").isEqualTo(1)
            .jsonPath("$.errors[0].source").isArray
            .jsonPath("$.errors[0].source.length()").isEqualTo(3)
            .jsonPath("$.errors[0].source[0]").isEqualTo("search")
            .jsonPath("$.errors[0].source[1]").isEqualTo("SampleSearch")
            .jsonPath("$.errors[0].source[2]").isEqualTo("id")
            .jsonPath("$.errors[0].code").isEqualTo("error.resource.notFound")
            .jsonPath("$.errors[0].message").isEqualTo(message)
            .jsonPath("$.meta").isMap
            .jsonPath("$.meta.length()").isEqualTo(detailMetaSize)
            .jsonPath("$.meta.traceId").isNotEmpty
            .jsonPath("$.meta.timestamp").isNumber
            .jsonPath("$.meta.status").isEqualTo(NOT_FOUND.value())
            .jsonPath("$.links").isMap
            .jsonPath("$.links.length()").isEqualTo(detailLinksSize)
            .jsonPath("$.links.self").isEqualTo(uri)
    }

    private fun createRequest(suffix: String): SampleRequest {
        val field1 = "JUnit Test $suffix - ${abs(random.nextInt())}"
        val field2 = abs(random.nextLong())
        val amount = abs(random.nextDouble())
            .let { if (it == 0.00) "0.01".toDouble() else it }
            .toBigDecimal()
            .multiply(BigDecimal.valueOf(100000))
            .scaled()

        val englishTranslationName = "Test English - $suffix"
        val englishTranslationDescription = "Test English Description - $suffix"
        val englishTranslationLanguage = "en"
        val englishTranslationOrdinal = 1
        val englishRequest = SampleTranslationRequest(englishTranslationName, englishTranslationDescription)
        englishRequest.language = englishTranslationLanguage
        englishRequest.ordinal = englishTranslationOrdinal

        val japaneseTranslationName = "Test Japanese - $suffix"
        val japaneseTranslationDescription = "Test Japanese Description - $suffix"
        val japaneseTranslationLanguage = "jp"
        val japaneseTranslationOrdinal = 2
        val japaneseRequest = SampleTranslationRequest(japaneseTranslationName, japaneseTranslationDescription)
        japaneseRequest.language = japaneseTranslationLanguage
        japaneseRequest.ordinal = japaneseTranslationOrdinal

        return SampleRequest(field1, field2, amount, arrayListOf(englishRequest, japaneseRequest))
    }

    private fun assertResponse(
        bodyContentSpec: BodyContentSpec,
        path: String,
        request: SampleRequest,
        language: String
    ): BodyContentSpec {
        val translated = request.translations!!.first { it.language == language }

        return bodyContentSpec
            .jsonPath("$path.id").isNotEmpty
            .jsonPath("$path.column1").isEqualTo(request.field1!!)
            .jsonPath("$path.column2").isEqualTo(request.field2!!)
            .jsonPath("$path.amount").isEqualTo(decimalFormat.format(request.amount!!))
            .jsonPath("$path.name").isEqualTo(translated.name!!)
            .jsonPath("$path.description").isEqualTo(translated.description!!)
            .jsonPath("$path.createdAt").isNumber
    }
}
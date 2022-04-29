package com.leijendary.spring.webflux.template.router

import com.leijendary.spring.webflux.template.ApplicationTests
import com.leijendary.spring.webflux.template.api.v1.data.SampleRequest
import com.leijendary.spring.webflux.template.api.v1.data.SampleResponse
import com.leijendary.spring.webflux.template.api.v1.data.SampleTranslationRequest
import com.leijendary.spring.webflux.template.core.data.DataResponse
import com.leijendary.spring.webflux.template.core.extension.scaled
import com.leijendary.spring.webflux.template.core.extension.toClass
import com.leijendary.spring.webflux.template.core.util.HEADER_TRACE_ID
import com.leijendary.spring.webflux.template.core.util.HEADER_USER_ID
import liquibase.repackaged.org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus.*
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import java.lang.Thread.sleep
import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8
import java.security.SecureRandom
import java.text.DecimalFormat
import java.util.Locale.getDefault
import kotlin.math.abs

class SampleRouterTest(
    @Autowired
    private val client: WebTestClient,

    @Autowired
    private val messageSource: MessageSource
) : ApplicationTests() {
    private val url = "/api/v1/samples"
    private val random = SecureRandom()
    private val userId = "junit-testing"
    private val decimalFormat = DecimalFormat("0.0#")
    private val listSize = 21
    private val listLimit = 10
    private val detailMemberSize = 9
    private val detailMetaSize = 3
    private val detailLinksSize = 1
    private val listMemberSize = 8
    private val listMetaSize = 4
    private val metaSeekSize = 3
    private val listLinksSize = 2

    @Test
    fun `Sample Create - Should create multiple and return created records`() {
        val request = createRequest()
        val bodyContentSpec = client
            .post()
            .uri(url)
            .bodyValue(request)
            .header(HEADER_USER_ID, this.userId)
            .header(HEADER_TRACE_ID, random.nextLong().toString())
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.data").isNotEmpty
            .jsonPath("$.data.length()").isEqualTo(detailMemberSize)
            .jsonPath("$.meta").isMap
            .jsonPath("$.meta.length()").isEqualTo(detailMetaSize)
            .jsonPath("$.meta.timestamp").isNumber
            .jsonPath("$.meta.requestId").isNotEmpty
            .jsonPath("$.meta.status").isEqualTo(CREATED.value())
            .jsonPath("$.links").isMap
            .jsonPath("$.links.length()").isEqualTo(detailLinksSize)
            .jsonPath("$.links.self").isEqualTo(url)

        assertResponse(bodyContentSpec, "$.data", request)

        for (translation in request.translations!!) {
            val translationPath = languagePath("$.data.translations", translation.language!!)

            assertTranslations(bodyContentSpec, translationPath, translation)
        }
    }

    @Test
    fun `Sample List - Should return the list based on the limit`() {
        val suffix = RandomStringUtils.randomAlphabetic(8)
        val query = "junit test $suffix"
        val queryUrl = URLEncoder.encode(query, UTF_8)
        val uri = "$url?query=$queryUrl&limit=$listLimit"
        val requests = (1..listSize).map { createRequest(suffix) }
        requests.forEach {
            client
                .post()
                .uri(url)
                .bodyValue(it)
                .header(HEADER_USER_ID, this.userId)
                .header(HEADER_TRACE_ID, random.nextLong().toString())
                .exchange()
                .expectBody(DataResponse::class.java)
                .returnResult()
                .responseBody!!
                .data!!
                .toClass(SampleResponse::class)
        }

        val size = requests.size
        var nextIndex = size - 1
        var nextToken = ""

        while (nextIndex > 0) {
            val seekUri = "$uri&nextToken=$nextToken"
            val selfUri = "$uri&nextToken=${URLEncoder.encode(nextToken, UTF_8)}"
            val seekSize = if (nextIndex > listLimit) listLimit else nextIndex
            val bodyContentSpec = client
                .get()
                .uri(seekUri)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.data").isArray
                .jsonPath("$.data.length()").isEqualTo(listLimit)
                .jsonPath("$.meta").isMap
                .jsonPath("$.meta.length()").isEqualTo(listMetaSize)
                .jsonPath("$.meta.timestamp").isNumber
                .jsonPath("$.meta.requestId").isNotEmpty
                .jsonPath("$.meta.seek").isMap
                .jsonPath("$.meta.seek.length()").isEqualTo(metaSeekSize)
                .jsonPath("$.meta.seek.size").isEqualTo(seekSize)
                .jsonPath("$.meta.seek.limit").isEqualTo(listLimit)
                .jsonPath("$.meta.status").isEqualTo(OK.value())
                .jsonPath("$.links").isMap
                .jsonPath("$.links.length()").isEqualTo(listLinksSize)
                .jsonPath("$.links.self").isEqualTo(selfUri)

            val lastIndex = nextIndex - seekSize + 1

            for ((listIndex, i) in (nextIndex downTo lastIndex).withIndex()) {
                bodyContentSpec.jsonPath("$.data[$listIndex].length()").isEqualTo(listMemberSize)

                assertResponse(bodyContentSpec, "$.data[$listIndex]", requests[i], false)

                nextIndex--
            }

            val response = bodyContentSpec
                .returnResult()
                .responseBody!!
                .let { String(it, UTF_8) }
                .toClass(DataResponse::class)

            nextToken = if (nextIndex >= 0) {
                bodyContentSpec
                    .jsonPath("$.meta.seek.nextToken").isNotEmpty
                    .jsonPath("$.links.next").isNotEmpty

                response
                    .meta["seek"]
                    ?.let { it as MutableMap<*, *> }
                    ?.let { it["nextToken"] }
                    ?.let { it as String }
                    ?: ""
            } else {
                bodyContentSpec
                    .jsonPath("$.meta.seek.nextToken").isEmpty
                    .jsonPath("$.links.next").isEmpty

                ""
            }
        }
    }

    @Test
    fun `Sample Get - Should return the created record`() {
        val request = createRequest()
        val createResponse = client
            .post()
            .uri(url)
            .bodyValue(request)
            .header(HEADER_USER_ID, this.userId)
            .header(HEADER_TRACE_ID, random.nextLong().toString())
            .exchange()
            .expectBody()
            .returnResult()
            .responseBody!!
            .let { String(it, UTF_8) }
            .toClass(DataResponse::class)
            .data!!.toClass(SampleResponse::class)
        val uri = "$url/${createResponse.id}"
        val bodyContentSpec = client
            .get()
            .uri(uri)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data").isNotEmpty
            .jsonPath("$.data.length()").isEqualTo(detailMemberSize)
            .jsonPath("$.meta").isMap
            .jsonPath("$.meta.length()").isEqualTo(detailMetaSize)
            .jsonPath("$.meta.timestamp").isNumber
            .jsonPath("$.meta.requestId").isNotEmpty
            .jsonPath("$.meta.status").isEqualTo(OK.value())
            .jsonPath("$.links").isMap
            .jsonPath("$.links.length()").isEqualTo(detailLinksSize)
            .jsonPath("$.links.self").isEqualTo(uri)

        assertResponse(bodyContentSpec, "$.data", request)

        for (translation in request.translations!!) {
            val translationPath = languagePath("$.data.translations", translation.language!!)

            assertTranslations(bodyContentSpec, translationPath, translation)
        }
    }

    @Test
    fun `Sample Update - Should return the updated record`() {
        val request = createRequest()
        val createResponse = client
            .post()
            .uri(url)
            .bodyValue(request)
            .header(HEADER_USER_ID, this.userId)
            .header(HEADER_TRACE_ID, random.nextLong().toString())
            .exchange()
            .expectBody()
            .returnResult()
            .responseBody!!
            .let { String(it, UTF_8) }
            .toClass(DataResponse::class)
            .data!!.toClass(SampleResponse::class)
        val uri = "$url/${createResponse.id}"

        val dutch = SampleTranslationRequest(
            "Test Dutch - Updated",
            "Test Dutch Description - Updated"
        )
        dutch.language = "nl"
        dutch.ordinal = 1

        val english = SampleTranslationRequest(
            "Test English - Updated",
            "Test English Description - Updated"
        )
        english.language = "en"
        english.ordinal = 2

        val german = SampleTranslationRequest(
            "Test German - Updated",
            "Test German Description - Updated"
        )
        german.language = "de"
        german.ordinal = 3

        val french = SampleTranslationRequest(
            "Test French - Updated",
            "Test French Description - Updated"
        )
        french.language = "fr"
        french.ordinal = 4

        val updatedTranslations = listOf(dutch, english, german, french)
        val updatedRequest = SampleRequest(
            "${request.field1} - Updated",
            request.field2!!.plus(200),
            request.amount!!.add(BigDecimal.valueOf(200)),
            updatedTranslations
        )
        val bodyContentSpec = client
            .put()
            .uri(uri)
            .bodyValue(updatedRequest)
            .header(HEADER_USER_ID, this.userId)
            .header(HEADER_TRACE_ID, random.nextLong().toString())
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data").isNotEmpty
            .jsonPath("$.data.length()").isEqualTo(detailMemberSize)
            .jsonPath("$.meta").isMap
            .jsonPath("$.meta.length()").isEqualTo(detailMetaSize)
            .jsonPath("$.meta.timestamp").isNumber
            .jsonPath("$.meta.requestId").isNotEmpty
            .jsonPath("$.meta.status").isEqualTo(OK.value())
            .jsonPath("$.links").isMap
            .jsonPath("$.links.length()").isEqualTo(detailLinksSize)
            .jsonPath("$.links.self").isEqualTo(uri)

        assertResponse(bodyContentSpec, "$.data", updatedRequest)

        for (translation in updatedRequest.translations!!) {
            val translationPath = languagePath("$.data.translations", translation.language!!)

            assertTranslations(bodyContentSpec, translationPath, translation)
        }
    }

    @Test
    fun `Sample Delete - Should return empty then 404 after`() {
        val request = createRequest()
        val createResponse = client
            .post()
            .uri(url)
            .bodyValue(request)
            .header(HEADER_USER_ID, this.userId)
            .header(HEADER_TRACE_ID, random.nextLong().toString())
            .exchange()
            .expectBody()
            .returnResult()
            .responseBody!!
            .let { String(it, UTF_8) }
            .toClass(DataResponse::class)
            .data!!.toClass(SampleResponse::class)
        val uri = "$url/${createResponse.id}"

        client
            .delete()
            .uri(uri)
            .header(HEADER_USER_ID, this.userId)
            .header(HEADER_TRACE_ID, random.nextLong().toString())
            .exchange()
            .expectStatus().isNoContent
            .expectBody().isEmpty

        sleep(1000)

        val args = arrayOf("data.SampleTable.id", createResponse.id)
        val message = messageSource.getMessage("error.resource.notFound", args, getDefault())

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
            .jsonPath("$.errors[0].source[0]").isEqualTo("data")
            .jsonPath("$.errors[0].source[1]").isEqualTo("SampleTable")
            .jsonPath("$.errors[0].source[2]").isEqualTo("id")
            .jsonPath("$.errors[0].code").isEqualTo("error.resource.notFound")
            .jsonPath("$.errors[0].message").isEqualTo(message)
            .jsonPath("$.meta").isMap
            .jsonPath("$.meta.length()").isEqualTo(detailMetaSize)
            .jsonPath("$.meta.timestamp").isNumber
            .jsonPath("$.meta.requestId").isNotEmpty
            .jsonPath("$.meta.status").isEqualTo(NOT_FOUND.value())
            .jsonPath("$.links").isMap
            .jsonPath("$.links.length()").isEqualTo(detailLinksSize)
            .jsonPath("$.links.self").isEqualTo(uri)
    }

    private fun createRequest(suffix: String = ""): SampleRequest {
        val field1 = "JUnit Test $suffix - ${abs(random.nextInt())}"
        val field2 = abs(random.nextLong())
        val amount = abs(random.nextDouble())
            .let { if (it == 0.00) "0.01".toDouble() else it }
            .toBigDecimal()
            .multiply(BigDecimal.valueOf(100000))
            .scaled()

        val englishTranslationName = "Test English"
        val englishTranslationDescription = "Test English Description"
        val englishTranslationLanguage = "en"
        val englishTranslationOrdinal = 1
        val englishRequest = SampleTranslationRequest(englishTranslationName, englishTranslationDescription)
        englishRequest.language = englishTranslationLanguage
        englishRequest.ordinal = englishTranslationOrdinal

        val japaneseTranslationName = "Test Japanese"
        val japaneseTranslationDescription = "Test Japanese Description"
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
        includeTranslations: Boolean = true
    ): BodyContentSpec {
        bodyContentSpec
            .jsonPath("$path.id").isNotEmpty
            .jsonPath("$path.column1").isEqualTo(request.field1!!)
            .jsonPath("$path.column2").isEqualTo(request.field2!!)
            .jsonPath("$path.amount").isEqualTo(decimalFormat.format(request.amount!!))
            .jsonPath("$path.createdAt").isNumber
            .jsonPath("$path.createdBy").isEqualTo(this.userId)
            .jsonPath("$path.lastModifiedAt").isNumber
            .jsonPath("$path.lastModifiedBy").isEqualTo(this.userId)

        if (includeTranslations) {
            bodyContentSpec
                .jsonPath("$path.translations").isArray
                .jsonPath("$path.translations.length()").isEqualTo(request.translations!!.size)
        }

        return bodyContentSpec
    }

    private fun assertTranslations(
        bodyContentSpec: BodyContentSpec,
        path: String,
        translation: SampleTranslationRequest
    ): BodyContentSpec {
        return bodyContentSpec
            .jsonPath("$path.name").isEqualTo(translation.name!!)
            .jsonPath("$path.description").isEqualTo(translation.description!!)
            .jsonPath("$path.ordinal").isEqualTo(translation.ordinal)
    }

    private fun languagePath(path: String, language: String): String = "$path[?(@.language=='$language')]"
}
package com.leijendary.spring.webflux.template.core.data

import com.leijendary.spring.webflux.template.core.extension.fullPath
import com.leijendary.spring.webflux.template.core.util.RequestContext.now
import com.leijendary.spring.webflux.template.core.util.RequestContext.request
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.util.UriComponentsBuilder.fromUriString

class DataResponse<T>(
    val data: T? = null,
    val meta: Map<String, Any> = emptyMap(),
    val links: Map<String, String?> = emptyMap()
) : Response {
    companion object {
        suspend fun <T> builder(): DataResponseBuilder<T> {
            val request = request()!!

            return DataResponseBuilder<T>(request)
                .status(OK)
                .meta("requestId", request.id)
                .selfLink()
        }

        suspend fun <T> of(body: T, httpStatus: HttpStatus = OK): DataResponse<T> {
            return builder<T>()
                .data(body)
                .status(httpStatus)
                .build()
        }

        suspend fun <T> from(seek: Seek<T>): DataResponse<List<T>> {
            return builder<List<T>>()
                .data(seek.content)
                .links(seek)
                .meta(seek)
                .build()
        }

        suspend fun <T> from(page: Page<T>): DataResponse<List<T>> {
            return builder<List<T>>()
                .data(page.content)
                .links(page)
                .meta(page)
                .build()
        }
    }

    class DataResponseBuilder<T>(private val request: ServerHttpRequest) {
        private var data: T? = null
        private val meta: MutableMap<String, Any> = HashMap()
        private val links: MutableMap<String, String?> = HashMap()

        suspend fun build(): DataResponse<T> {
            meta["timestamp"] = now()

            return DataResponse(data, meta, links)
        }

        fun data(data: T): DataResponseBuilder<T> {
            this.data = data

            return this
        }

        fun meta(key: String, value: Any): DataResponseBuilder<T> {
            meta[key] = value

            return this
        }

        fun status(httpStatus: HttpStatus): DataResponseBuilder<T> {
            meta["status"] = httpStatus.value()

            return this
        }

        fun meta(page: Page<*>): DataResponseBuilder<T> {
            meta["page"] = PageMeta(page)

            return this
        }

        fun meta(seek: Seek<*>): DataResponseBuilder<T> {
            meta["seek"] = SeekMeta(seek)

            return this
        }

        fun selfLink(): DataResponseBuilder<T> {
            links["self"] = request.uri.fullPath()

            return this
        }

        fun links(page: Page<*>): DataResponseBuilder<T> {
            val size = page.size
            val sort = page.sort

            links["self"] = createLink(page.pageable.pageNumber, size, sort)

            if (page.hasPrevious()) {
                val previousPageable = page.previousOrFirstPageable()

                links["previous"] = createLink(previousPageable.pageNumber, size, sort)
            }

            if (page.hasNext()) {
                val nextPageable = page.nextOrLastPageable()

                links["next"] = createLink(nextPageable.pageNumber, size, sort)
            }

            links["last"] = createLink(page.totalPages - 1, size, sort)

            return this
        }

        fun links(seek: Seek<*>): DataResponseBuilder<T> {
            val nextToken = seek.nextToken
            val limit = seek.seekable.limit

            if (nextToken != null) {
                links["next"] = createLink(nextToken, limit)
            }

            return this
        }

        private fun createLink(page: Int, size: Int, sort: Sort): String {
            val path: String = request.uri.fullPath()
            val builder = fromUriString(path)
                .replaceQueryParam("page", page)
                .replaceQueryParam("size", size)

            if (sort.isSorted) {
                val sortString = sort
                    .toSet()
                    .map { "${it.property},${it.direction}" }

                builder.replaceQueryParam("sort", sortString)
            }

            return builder.build().toString()
        }

        private fun createLink(nextToken: String, limit: Int): String {
            val path = request.uri.fullPath()
            val builder = fromUriString(path)
                .replaceQueryParam("limit", limit)
                .replaceQueryParam("nextToken", nextToken)

            return builder.build().toString()
        }
    }
}
package com.leijendary.spring.webflux.template.api.router

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter
import com.leijendary.spring.webflux.template.api.v1.http.SampleSearchHandler as SampleSearchHandlerV1

@Configuration
class SampleSearchRouter(private val sampleSearchHandlerV1: SampleSearchHandlerV1) {
    @Bean
    @Order(1)
    fun sampleSearchRoutes() = coRouter {
        (accept(APPLICATION_JSON) and "/api/v1/samples/search").nest {
            POST("reindex", sampleSearchHandlerV1::reindex)
            GET("", sampleSearchHandlerV1::page)
            GET("{id}", sampleSearchHandlerV1::get)
        }
    }
}
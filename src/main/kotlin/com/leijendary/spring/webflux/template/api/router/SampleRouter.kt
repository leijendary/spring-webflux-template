package com.leijendary.spring.webflux.template.api.router

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter
import com.leijendary.spring.webflux.template.api.v1.http.SampleHandler as SampleHandlerV1

@Configuration
class SampleRouter(private val sampleHandlerV1: SampleHandlerV1) {
    @Bean
    @Order(2)
    fun sampleRoutes() = coRouter {
        (accept(APPLICATION_JSON) and "/api/v1/samples").nest {
            GET("client", sampleHandlerV1::client)
            GET("locale", sampleHandlerV1::locale)
            GET("language", sampleHandlerV1::language)
            GET("timezone", sampleHandlerV1::timeZone)
            GET("", sampleHandlerV1::seek)
            GET("{id}", sampleHandlerV1::get)
            POST("", sampleHandlerV1::create)
            PUT("{id}", sampleHandlerV1::update)
            DELETE("{id}", sampleHandlerV1::delete)
        }
    }
}
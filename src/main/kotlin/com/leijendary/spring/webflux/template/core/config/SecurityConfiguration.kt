package com.leijendary.spring.webflux.template.core.config

import com.leijendary.spring.webflux.template.core.config.properties.AuthProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration(private val authProperties: AuthProperties) {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange()
            .anyExchange().permitAll()
            .and()
            .anonymous { it.principal(authProperties.anonymousUser.principal) }
            .httpBasic().disable()
            .formLogin().disable()
            .csrf().disable()
            .exceptionHandling()
            .authenticationEntryPoint { _, ex -> throw ex }
            .accessDeniedHandler { _, ex -> throw ex }
            .and()
            .build()
    }
}
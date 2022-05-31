package com.leijendary.spring.webflux.template.core.config

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange()
            .anyExchange().permitAll()
            .and()
            .anonymous().disable()
            .httpBasic().disable()
            .formLogin().disable()
            .logout().disable()
            .csrf().disable()
            .exceptionHandling()
            .authenticationEntryPoint { _, ex -> throw ex }
            .accessDeniedHandler { _, ex -> throw ex }
            .and()
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .build()
    }
}